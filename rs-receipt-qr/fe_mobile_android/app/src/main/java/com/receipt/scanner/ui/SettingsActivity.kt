package com.receipt.scanner.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.receipt.scanner.R
import com.receipt.scanner.data.SettingsRepository
import com.receipt.scanner.databinding.ActivitySettingsBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingsRepository = SettingsRepository(this)

        setupToolbar()
        loadSettings()
        setupSaveButton()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadSettings() {
        lifecycleScope.launch {
            binding.backendUrlInput.setText(settingsRepository.backendUrl.first())
            binding.userIdInput.setText(settingsRepository.userId.first().toString())
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            val backendUrl = binding.backendUrlInput.text.toString().trim()
            val userIdText = binding.userIdInput.text.toString().trim()

            if (backendUrl.isEmpty()) {
                binding.backendUrlLayout.error = getString(R.string.backend_url) + " required"
                return@setOnClickListener
            }

            val userId = userIdText.toIntOrNull() ?: 1

            lifecycleScope.launch {
                settingsRepository.setBackendUrl(backendUrl)
                settingsRepository.setUserId(userId)
                Toast.makeText(this@SettingsActivity, "Settings saved", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
