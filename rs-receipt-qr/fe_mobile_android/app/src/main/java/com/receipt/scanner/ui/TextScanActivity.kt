package com.receipt.scanner.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.receipt.scanner.R
import com.receipt.scanner.api.ApiClient
import com.receipt.scanner.data.SettingsRepository
import com.receipt.scanner.databinding.ActivityTextScanBinding
import com.receipt.scanner.model.ReceiptTextData
import com.receipt.scanner.model.ReceiptTextRequest
import com.receipt.scanner.ocr.TextRecognizerProvider
import com.receipt.scanner.util.TextReceiptAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Activity for scanning receipt text using OCR.
 * Supports two recognition modes:
 * - ML Kit offline (Latin only) - fast, works offline
 * - Firebase ML cloud (Cyrillic + Latin) - requires internet
 *
 * Scans PFR (Poreska fiskalna registracija) data from Serbian fiscal receipts.
 */
class TextScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTextScanBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var settingsRepository: SettingsRepository
    private var textAnalyzer: TextReceiptAnalyzer? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null

    private var currentMode = TextRecognizerProvider.Mode.MLKIT_OFFLINE_LATIN
    private var isProcessing = false
    private var lastScannedData: ReceiptTextData? = null
    private var lastScanTime: Long = 0

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            showPermissionRequired()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsRepository = SettingsRepository(this)
        cameraExecutor = Executors.newSingleThreadExecutor()

        setupUI()
        checkCameraPermission()
    }

    private fun setupUI() {
        binding.fabBack.setOnClickListener {
            finish()
        }

        binding.grantPermissionButton.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        // OCR mode selection
        binding.chipOffline.setOnClickListener {
            switchOcrMode(TextRecognizerProvider.Mode.MLKIT_OFFLINE_LATIN)
        }

        binding.chipCloud.setOnClickListener {
            switchOcrMode(TextRecognizerProvider.Mode.FIREBASE_CLOUD)
        }
    }

    private fun switchOcrMode(mode: TextRecognizerProvider.Mode) {
        if (currentMode == mode) return

        currentMode = mode
        Toast.makeText(
            this,
            if (mode == TextRecognizerProvider.Mode.MLKIT_OFFLINE_LATIN)
                getString(R.string.ocr_mode_offline_desc)
            else
                getString(R.string.ocr_mode_cloud_desc),
            Toast.LENGTH_SHORT
        ).show()

        // Restart camera with new analyzer
        restartCameraWithNewMode()
    }

    private fun restartCameraWithNewMode() {
        // Close existing analyzer
        textAnalyzer?.close()
        textAnalyzer = null

        // Unbind and rebind camera
        cameraProvider?.let { provider ->
            imageAnalysis?.let { analysis ->
                provider.unbind(analysis)
            }

            // Create new analyzer with selected mode
            textAnalyzer = TextReceiptAnalyzer(
                currentMode,
                ::onReceiptTextDetected,
                ::onTextError
            )

            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, textAnalyzer!!)
                }

            try {
                provider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showPermissionRequired() {
        binding.cameraPreview.visibility = View.GONE
        binding.scannerOverlay.visibility = View.GONE
        binding.permissionLayout.visibility = View.VISIBLE
    }

    private fun startCamera() {
        binding.permissionLayout.visibility = View.GONE
        binding.cameraPreview.visibility = View.VISIBLE
        binding.scannerOverlay.visibility = View.VISIBLE

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = binding.cameraPreview.surfaceProvider
                }

            textAnalyzer = TextReceiptAnalyzer(
                currentMode,
                ::onReceiptTextDetected,
                ::onTextError
            )

            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, textAnalyzer!!)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun onTextError(errorMessage: String) {
        runOnUiThread {
            showStatus(getString(R.string.receipt_text_failed) + ": " + errorMessage, false)
        }
    }

    private fun onReceiptTextDetected(data: ReceiptTextData) {
        val currentTime = System.currentTimeMillis()

        // Prevent duplicate scans within 5 seconds
        if (isProcessing || (data == lastScannedData && currentTime - lastScanTime < 5000)) {
            return
        }

        isProcessing = true
        lastScannedData = data
        lastScanTime = currentTime

        // Disable analyzer while processing
        textAnalyzer?.setEnabled(false)

        runOnUiThread {
            vibrate()
            sendReceiptTextToBackend(data)
        }
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun sendReceiptTextToBackend(data: ReceiptTextData) {
        lifecycleScope.launch {
            val backendUrl = settingsRepository.getBackendUrl()
            if (backendUrl.isEmpty()) {
                showStatus(getString(R.string.no_backend_url), false)
                isProcessing = false
                textAnalyzer?.setEnabled(true)
                return@launch
            }

            showLoading(true)

            try {
                val userId = settingsRepository.getUserId()
                val api = ApiClient.getReceiptTextApi(backendUrl)
                val request = ReceiptTextRequest.fromData(userId, data)

                val response = withContext(Dispatchers.IO) {
                    api.sendReceiptText(request).execute()
                }

                if (response.isSuccessful && response.body() != null) {
                    showStatus(getString(R.string.receipt_text_sent), true)
                } else {
                    showStatus(getString(R.string.receipt_text_failed), false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showStatus(getString(R.string.receipt_text_failed) + ": " + e.message, false)
            } finally {
                showLoading(false)
                delay(3000)
                isProcessing = false
                textAnalyzer?.setEnabled(true)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.hintText.text = if (show) getString(R.string.sending) else getString(R.string.scan_text_hint)
    }

    private fun showStatus(message: String, success: Boolean) {
        binding.statusCard.visibility = View.VISIBLE
        binding.statusText.text = message
        binding.statusCard.setCardBackgroundColor(
            ContextCompat.getColor(
                this,
                if (success) R.color.success else R.color.error
            )
        )

        lifecycleScope.launch {
            delay(3000)
            binding.statusCard.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        textAnalyzer?.close()
        cameraExecutor.shutdown()
    }
}
