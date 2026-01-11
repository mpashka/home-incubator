package com.receipt.scanner.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.receipt.scanner.databinding.ActivityHomeBinding

/**
 * Home screen activity with scan mode selection buttons.
 * Provides navigation to:
 * - QR Code scanning (MainActivity)
 * - Receipt text scanning (TextScanActivity)
 * - Settings
 * - History
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.cardScanQr.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.cardScanText.setOnClickListener {
            startActivity(Intent(this, TextScanActivity::class.java))
        }

        binding.fabSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.fabHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }
}
