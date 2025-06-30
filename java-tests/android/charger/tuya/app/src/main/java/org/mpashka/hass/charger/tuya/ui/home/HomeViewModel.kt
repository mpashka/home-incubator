package org.mpashka.hass.charger.tuya.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel : ViewModel() {

    private val _batteryLevel = MutableLiveData<Int>().apply {
        value = 0
    }
    val batteryLevel: LiveData<Int> = _batteryLevel

    private val _mqttStatus = MutableLiveData<String>().apply {
        value = "Disconnected"
    }
    val mqttStatus: LiveData<String> = _mqttStatus

    private val _lastUpdate = MutableLiveData<String>().apply {
        value = "Never"
    }
    val lastUpdate: LiveData<String> = _lastUpdate

    private val _isMonitoring = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isMonitoring: LiveData<Boolean> = _isMonitoring

    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun updateBatteryLevel(level: Int) {
        _batteryLevel.postValue(level)
        _lastUpdate.postValue("Last update: ${dateFormat.format(Date())}")
    }

    fun updateMqttStatus(status: String) {
        _mqttStatus.postValue(status)
    }

    fun setMonitoring(monitoring: Boolean) {
        _isMonitoring.postValue(monitoring)
    }

    fun getBatteryReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val batteryPct = level * 100 / scale
                    updateBatteryLevel(batteryPct)
                }
            }
        }
    }
}