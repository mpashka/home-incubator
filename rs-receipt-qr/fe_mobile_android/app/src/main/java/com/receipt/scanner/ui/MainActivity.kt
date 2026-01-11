package com.receipt.scanner.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
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
import com.receipt.scanner.api.QrRequest
import com.receipt.scanner.data.ScanHistoryRepository
import com.receipt.scanner.data.ScanRecord
import com.receipt.scanner.data.SettingsRepository
import com.receipt.scanner.databinding.ActivityMainBinding
import com.receipt.scanner.util.QrCodeAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var historyRepository: ScanHistoryRepository

    private var isProcessingQr = false
    private var lastScannedQr: String? = null
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsRepository = SettingsRepository(this)
        historyRepository = ScanHistoryRepository(this)
        cameraExecutor = Executors.newSingleThreadExecutor()

        setupUI()
        checkCameraPermission()
    }

    private fun setupUI() {
        binding.fabSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.fabHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        binding.grantPermissionButton.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
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
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = binding.cameraPreview.surfaceProvider
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, QrCodeAnalyzer(::onQrCodeDetected, ::onQrCodeError))
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun onQrCodeError(qrValue: String) {
        runOnUiThread {
            vibrate()
            showStatus(getString(R.string.send_failed) + ": " + qrValue.subSequence(0, 10), false)
        }
    }

    private fun onQrCodeDetected(qrValue: String) {
        val currentTime = System.currentTimeMillis()

        // Prevent duplicate scans within 3 seconds
        if (isProcessingQr || (qrValue == lastScannedQr && currentTime - lastScanTime < 3000)) {
            return
        }

        isProcessingQr = true
        lastScannedQr = qrValue
        lastScanTime = currentTime

        runOnUiThread {
            vibrate()
            sendQrToBackend(qrValue)
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

    private fun sendQrToBackend(qrUrl: String) {
        lifecycleScope.launch {
            val backendUrl = settingsRepository.getBackendUrl()
            if (backendUrl.isEmpty()) {
                showStatus(getString(R.string.no_backend_url), false)
                isProcessingQr = false
                return@launch
            }

            showLoading(true)

            try {
                val userId = settingsRepository.getUserId()
                val api = ApiClient.getApi(backendUrl)
                val response = withContext(Dispatchers.IO) {
                    api.sendQrCode(QrRequest(userId, qrUrl))
                }

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    historyRepository.addRecord(
                        ScanRecord(
                            id = body.receiptRawId,
                            url = qrUrl,
                            status = body.status,
                            receiptId = null,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    showStatus(getString(R.string.sent_successfully), true)
                } else {
                    showStatus(getString(R.string.send_failed), false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showStatus(getString(R.string.send_failed) + ": " + e.message, false)
            } finally {
                showLoading(false)
                delay(2000)
                isProcessingQr = false
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.hintText.text = if (show) getString(R.string.sending) else getString(R.string.scan_qr_hint)
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
        cameraExecutor.shutdown()
    }
}
