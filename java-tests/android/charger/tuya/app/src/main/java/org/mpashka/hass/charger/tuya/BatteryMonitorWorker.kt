package org.mpashka.hass.charger.tuya

import android.content.Context
import android.os.BatteryManager
import android.util.Log
import androidx.work.*
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.*
import java.util.concurrent.TimeUnit

class BatteryMonitorWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "BatteryMonitorWorker"
        internal const val WORK_NAME = "BatteryMonitorWork"
        
        fun startPeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val periodicWorkRequest = PeriodicWorkRequestBuilder<BatteryMonitorWorker>(
                MqttConfig.BATTERY_UPDATE_INTERVAL_MS, TimeUnit.MILLISECONDS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWorkRequest
            )
            
            Log.d(TAG, "Periodic battery monitoring work scheduled")
        }
        
        fun stopPeriodicWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Periodic battery monitoring work cancelled")
        }
    }
    
    private var mqttClient: MqttClient? = null
    private var isConnected = false
    private val mqttClientId = "${MqttConfig.MQTT_CLIENT_ID_PREFIX}_${UUID.randomUUID()}"
    private val tuyaDeviceManager = TuyaDeviceManager.getInstance()
    
    override suspend fun doWork(): Result {
        Log.d(TAG, "Battery monitoring work started")
        
        try {
            // Initialize Tuya IoT if not already done
            if (!tuyaDeviceManager.isDeviceOnline()) {
                Log.d(TAG, "Initializing Tuya IoT...")
                val tuyaInitialized = tuyaDeviceManager.initialize(applicationContext)
                if (!tuyaInitialized) {
                    Log.w(TAG, "Failed to initialize Tuya IoT, continuing with MQTT only")
                }
            }
            
            // Connect to MQTT
            connectToMqtt()
            
            // Get current battery level
            val batteryManager = applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val batteryPct = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).toFloat()
            
            Log.d(TAG, "Battery level: $batteryPct%")
            
            // Publish battery level to MQTT
            publishBatteryLevel(batteryPct)
            
            // Control Tuya switch based on battery level
            controlTuyaSwitch(batteryPct)
            
            // Disconnect from MQTT
            disconnectFromMqtt()
            
            Log.d(TAG, "Battery monitoring work completed successfully")
            return Result.success()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in battery monitoring work", e)
            disconnectFromMqtt()
            return Result.retry()
        }
    }
    
    /**
     * Control Tuya switch based on battery level
     */
    private suspend fun controlTuyaSwitch(batteryLevel: Float) {
        if (!tuyaDeviceManager.isDeviceOnline()) {
            Log.w(TAG, "Tuya device not online, skipping switch control")
            return
        }
        
        try {
            val currentSwitchStatus = tuyaDeviceManager.getSwitchStatus()
            Log.d(TAG, "Current switch status: $currentSwitchStatus")
            
            when {
                batteryLevel <= TuyaConfig.BATTERY_LOW_THRESHOLD -> {
                    // Battery is low, turn on switch if it's not already on
                    if (currentSwitchStatus != true) {
                        Log.d(TAG, "Battery level $batteryLevel% is below threshold ${TuyaConfig.BATTERY_LOW_THRESHOLD}%, turning on switch")
                        val success = tuyaDeviceManager.turnOnSwitch()
                        if (success) {
                            Log.d(TAG, "Successfully turned on Tuya switch")
                        } else {
                            Log.e(TAG, "Failed to turn on Tuya switch")
                        }
                    } else {
                        Log.d(TAG, "Switch is already on, no action needed")
                    }
                }
                
                batteryLevel >= TuyaConfig.BATTERY_HIGH_THRESHOLD -> {
                    // Battery is high, turn off switch if it's not already off
                    if (currentSwitchStatus != false) {
                        Log.d(TAG, "Battery level $batteryLevel% is above threshold ${TuyaConfig.BATTERY_HIGH_THRESHOLD}%, turning off switch")
                        val success = tuyaDeviceManager.turnOffSwitch()
                        if (success) {
                            Log.d(TAG, "Successfully turned off Tuya switch")
                        } else {
                            Log.e(TAG, "Failed to turn off Tuya switch")
                        }
                    } else {
                        Log.d(TAG, "Switch is already off, no action needed")
                    }
                }
                
                else -> {
                    // Battery level is between thresholds, no action needed
                    Log.d(TAG, "Battery level $batteryLevel% is between thresholds, no switch action needed")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error controlling Tuya switch", e)
        }
    }
    
    private fun connectToMqtt() {
        try {
            mqttClient = MqttClient(MqttConfig.MQTT_BROKER_URL, mqttClientId, MemoryPersistence())
            
            val options = MqttConnectOptions().apply {
                isCleanSession = MqttConfig.MQTT_CLEAN_SESSION
                connectionTimeout = MqttConfig.MQTT_CONNECTION_TIMEOUT
                keepAliveInterval = MqttConfig.MQTT_KEEP_ALIVE_INTERVAL
                
                // Uncomment if your MQTT broker requires authentication
                // userName = MqttConfig.MQTT_USERNAME
                // password = MqttConfig.MQTT_PASSWORD.toCharArray()
            }
            
            mqttClient?.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.e(TAG, "MQTT connection lost", cause)
                    isConnected = false
                }
                
                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    Log.d(TAG, "Message received: ${message?.toString()}")
                }
                
                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    Log.d(TAG, "Message delivered")
                }
            })
            
            mqttClient?.connect(options)
            isConnected = true
            Log.d(TAG, "Connected to MQTT broker: ${MqttConfig.MQTT_BROKER_URL}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to MQTT broker", e)
            isConnected = false
            throw e
        }
    }
    
    private fun disconnectFromMqtt() {
        try {
            mqttClient?.disconnect()
            mqttClient?.close()
            isConnected = false
            Log.d(TAG, "Disconnected from MQTT broker")
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting from MQTT broker", e)
        }
    }
    
    private fun publishBatteryLevel(level: Float) {
        if (!isConnected || mqttClient == null) {
            Log.w(TAG, "MQTT not connected, cannot publish battery level")
            return
        }
        
        try {
            val message = MqttMessage(level.toString().toByteArray())
            mqttClient?.publish(MqttConfig.MQTT_TOPIC, message)
            Log.d(TAG, "Published battery level: $level% to topic: ${MqttConfig.MQTT_TOPIC}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to publish battery level", e)
            throw e
        }
    }
} 