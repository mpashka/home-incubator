package org.mpashka.hass.charger.tuya

import android.content.Context
import android.util.Log
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object BatteryMonitorManager {
    private const val TAG = "BatteryMonitorManager"
    private val tuyaDeviceManager = TuyaDeviceManager.getInstance()
    
    /**
     * Start periodic battery monitoring
     */
    fun startMonitoring(context: Context) {
        BatteryMonitorWorker.startPeriodicWork(context)
        Log.d(TAG, "Battery monitoring started")
    }
    
    /**
     * Stop periodic battery monitoring
     */
    fun stopMonitoring(context: Context) {
        BatteryMonitorWorker.stopPeriodicWork(context)
        Log.d(TAG, "Battery monitoring stopped")
    }
    
    /**
     * Check if battery monitoring is currently active
     */
    fun isMonitoringActive(context: Context): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(BatteryMonitorWorker.WORK_NAME)
            .get()
        
        return workInfos.any { workInfo ->
            workInfo.state == WorkInfo.State.ENQUEUED || 
            workInfo.state == WorkInfo.State.RUNNING
        }
    }
    
    /**
     * Get the current status of battery monitoring work
     */
    fun getMonitoringStatus(context: Context): WorkInfo.State? {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(BatteryMonitorWorker.WORK_NAME)
            .get()
        
        return workInfos.firstOrNull()?.state
    }
    
    /**
     * Initialize Tuya IoT integration
     */
    suspend fun initializeTuya(context: Context): Boolean {
        return try {
            Log.d(TAG, "Initializing Tuya IoT integration...")
            val success = tuyaDeviceManager.initialize(context)
            if (success) {
                Log.d(TAG, "Tuya IoT integration initialized successfully")
            } else {
                Log.e(TAG, "Failed to initialize Tuya IoT integration")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Tuya IoT integration", e)
            false
        }
    }
    
    /**
     * Check if Tuya device is online
     */
    fun isTuyaDeviceOnline(): Boolean {
        return tuyaDeviceManager.isDeviceOnline()
    }
    
    /**
     * Get current Tuya switch status
     */
    suspend fun getTuyaSwitchStatus(): Boolean? {
        return try {
            tuyaDeviceManager.getSwitchStatus()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting Tuya switch status", e)
            null
        }
    }
    
    /**
     * Manually turn on Tuya switch
     */
    suspend fun turnOnTuyaSwitch(): Boolean {
        return try {
            Log.d(TAG, "Manually turning on Tuya switch...")
            val success = tuyaDeviceManager.turnOnSwitch()
            if (success) {
                Log.d(TAG, "Successfully turned on Tuya switch manually")
            } else {
                Log.e(TAG, "Failed to turn on Tuya switch manually")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error turning on Tuya switch manually", e)
            false
        }
    }
    
    /**
     * Manually turn off Tuya switch
     */
    suspend fun turnOffTuyaSwitch(): Boolean {
        return try {
            Log.d(TAG, "Manually turning off Tuya switch...")
            val success = tuyaDeviceManager.turnOffSwitch()
            if (success) {
                Log.d(TAG, "Successfully turned off Tuya switch manually")
            } else {
                Log.e(TAG, "Failed to turn off Tuya switch manually")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error turning off Tuya switch manually", e)
            false
        }
    }
    
    /**
     * Cleanup Tuya IoT resources
     */
    fun cleanupTuya() {
        try {
            tuyaDeviceManager.cleanup()
            Log.d(TAG, "Tuya IoT resources cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up Tuya IoT resources", e)
        }
    }
} 