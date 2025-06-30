package org.mpashka.hass.charger.tuya

object MqttConfig {
    // MQTT Broker Configuration
    const val MQTT_BROKER_URL = "tcp://localhost:1883" // Change this to your MQTT broker URL
    const val MQTT_CLIENT_ID_PREFIX = "AndroidBatteryMonitor"
    const val MQTT_TOPIC = "phone/battery/level"
    
    // MQTT Connection Options
    const val MQTT_CONNECTION_TIMEOUT = 30
    const val MQTT_KEEP_ALIVE_INTERVAL = 60
    const val MQTT_CLEAN_SESSION = true
    
    // Battery Monitoring Configuration
    // WorkManager requires minimum 15 minutes for periodic work
    const val BATTERY_UPDATE_INTERVAL_MS = 15 * 60 * 1000L // 15 minutes
    
    // MQTT Authentication (uncomment and set if your broker requires authentication)
    // const val MQTT_USERNAME = "your_username"
    // const val MQTT_PASSWORD = "your_password"
    
    // MQTT SSL/TLS Configuration (uncomment if using SSL)
    // const val MQTT_BROKER_URL_SSL = "ssl://localhost:8883"
    // const val MQTT_SSL_ENABLED = true
} 