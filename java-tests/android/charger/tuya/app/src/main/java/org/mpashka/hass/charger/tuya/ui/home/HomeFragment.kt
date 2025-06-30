package org.mpashka.hass.charger.tuya.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.mpashka.hass.charger.tuya.BatteryMonitorManager
import org.mpashka.hass.charger.tuya.BatteryMonitorService
import org.mpashka.hass.charger.tuya.R
import org.mpashka.hass.charger.tuya.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private var batteryReceiver: BroadcastReceiver? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupObservers()
        setupClickListeners()
        setupBatteryReceiver()
        getCurrentBatteryLevel()
        updateTuyaStatus()

        return root
    }

    private fun setupObservers() {
        homeViewModel.batteryLevel.observe(viewLifecycleOwner) { level ->
            binding.textBatteryLevel.text = "$level%"
            binding.progressBattery.progress = level
            
            // Update color based on battery level
            val color = when {
                level <= 20 -> ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
                level <= 50 -> ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark)
                else -> ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
            }
            binding.textBatteryLevel.setTextColor(color)
        }

        homeViewModel.mqttStatus.observe(viewLifecycleOwner) { status ->
            binding.textMqttStatus.text = status
            val color = if (status == getString(R.string.connected)) {
                ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
            } else {
                ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
            }
            binding.textMqttStatus.setTextColor(color)
        }

        homeViewModel.lastUpdate.observe(viewLifecycleOwner) { update ->
            binding.textLastUpdate.text = update
        }

        homeViewModel.isMonitoring.observe(viewLifecycleOwner) { isMonitoring ->
            binding.buttonStart.isEnabled = !isMonitoring
            binding.buttonStop.isEnabled = isMonitoring
        }
    }

    private fun setupClickListeners() {
        binding.buttonStart.setOnClickListener {
            startBatteryMonitoring()
        }

        binding.buttonStop.setOnClickListener {
            stopBatteryMonitoring()
        }
        
        binding.buttonTuyaOn.setOnClickListener {
            turnOnTuyaSwitch()
        }
        
        binding.buttonTuyaOff.setOnClickListener {
            turnOffTuyaSwitch()
        }
    }
    
    private fun turnOnTuyaSwitch() {
        lifecycleScope.launch {
            val success = BatteryMonitorManager.turnOnTuyaSwitch()
            if (success) {
                updateTuyaStatus()
            }
        }
    }
    
    private fun turnOffTuyaSwitch() {
        lifecycleScope.launch {
            val success = BatteryMonitorManager.turnOffTuyaSwitch()
            if (success) {
                updateTuyaStatus()
            }
        }
    }
    
    private fun updateTuyaStatus() {
        lifecycleScope.launch {
            val isOnline = BatteryMonitorManager.isTuyaDeviceOnline()
            val switchStatus = BatteryMonitorManager.getTuyaSwitchStatus()
            
            // Update Tuya status
            binding.textTuyaStatus.text = if (isOnline) "Online" else "Offline"
            binding.textTuyaStatus.setTextColor(
                if (isOnline) ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
                else ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
            )
            
            // Update switch status
            val switchText = when (switchStatus) {
                true -> "Switch: ON"
                false -> "Switch: OFF"
                null -> "Switch: Unknown"
            }
            binding.textTuyaSwitchStatus.text = switchText
            
            // Enable/disable buttons based on online status
            binding.buttonTuyaOn.isEnabled = isOnline
            binding.buttonTuyaOff.isEnabled = isOnline
        }
    }

    private fun setupBatteryReceiver() {
        batteryReceiver = homeViewModel.getBatteryReceiver()
        // registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    private fun getCurrentBatteryLevel() {
        val batteryManager = requireContext().getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        homeViewModel.updateBatteryLevel(level)
    }

    private fun startBatteryMonitoring() {
        val serviceIntent = Intent(requireContext(), BatteryMonitorService::class.java)
        requireContext().startForegroundService(serviceIntent)
        homeViewModel.setMonitoring(true)
        homeViewModel.updateMqttStatus(getString(R.string.connecting))
    }

    private fun stopBatteryMonitoring() {
        val serviceIntent = Intent(requireContext(), BatteryMonitorService::class.java)
        requireContext().stopService(serviceIntent)
        homeViewModel.setMonitoring(false)
        homeViewModel.updateMqttStatus(getString(R.string.disconnected))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        batteryReceiver?.let {
            try {
                requireContext().unregisterReceiver(it)
            } catch (e: Exception) {
                // Receiver might not be registered
            }
        }
        _binding = null
    }
}