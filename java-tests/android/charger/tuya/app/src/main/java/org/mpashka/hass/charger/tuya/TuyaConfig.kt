package org.mpashka.hass.charger.tuya

object TuyaConfig {
    // Tuya IoT Configuration
    const val TUYA_ACCESS_ID = "your_access_id" // Replace with your Tuya IoT access ID
    const val TUYA_ACCESS_KEY = "your_access_key" // Replace with your Tuya IoT access key
    const val TUYA_DEVICE_ID = "your_device_id" // Replace with your Tuya switch device ID
    
    // Battery thresholds for automatic control
    const val BATTERY_LOW_THRESHOLD = 20 // Turn on switch when battery < 20%
    const val BATTERY_HIGH_THRESHOLD = 80 // Turn off switch when battery > 80%
    
    // Tuya IoT API Configuration
    const val TUYA_REGION = "eu" // Change to your region: eu, us, cn, in, etc.
    const val TUYA_ENVIRONMENT = "PROD" // PROD or TEST
} 