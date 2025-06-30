# Android Battery Monitor with MQTT and Tuya IoT Integration

This Android application monitors the phone's battery level, reports it to an MQTT server, and automatically controls Tuya smart switches based on battery levels. It provides a user-friendly interface to start/stop monitoring and displays real-time battery status with Tuya device control.

## Features

- Real-time battery level monitoring
- MQTT integration for reporting battery data
- **Tuya IoT integration for automatic switch control**
- **Automatic charging control based on battery thresholds**
- **Manual Tuya switch control via UI**
- Foreground service for continuous monitoring
- User-friendly UI with battery level visualization
- Automatic reconnection to MQTT broker
- Configurable update intervals

## Automatic Battery Control

The app automatically controls your Tuya smart switch based on battery levels:

- **Battery ≤ 20%**: Turns ON the switch (starts charging)
- **Battery ≥ 80%**: Turns OFF the switch (stops charging)
- **Battery 20-80%**: No action (maintains current state)

You can adjust these thresholds in the configuration.

## Configuration

### MQTT Settings

Edit the `MqttConfig.kt` file to configure your MQTT broker settings:

```kotlin
object MqttConfig {
    // Change this to your MQTT broker URL
    const val MQTT_BROKER_URL = "tcp://your-mqtt-broker:1883"
    
    // MQTT topic where battery data will be published
    const val MQTT_TOPIC = "phone/battery/level"
    
    // Update interval (default: 15 minutes - WorkManager minimum)
    const val BATTERY_UPDATE_INTERVAL_MS = 15 * 60 * 1000L
    
    // Authentication (uncomment and set if required)
    // const val MQTT_USERNAME = "your_username"
    // const val MQTT_PASSWORD = "your_password"
}
```

### Tuya IoT Settings

Edit the `TuyaConfig.kt` file to configure your Tuya IoT integration:

```kotlin
object TuyaConfig {
    // Replace with your actual Tuya IoT credentials
    const val TUYA_ACCESS_ID = "your_actual_access_id"
    const val TUYA_ACCESS_KEY = "your_actual_access_key"
    const val TUYA_DEVICE_ID = "your_actual_device_id"
    
    // Battery thresholds for automatic control
    const val BATTERY_LOW_THRESHOLD = 20 // Turn on switch when battery < 20%
    const val BATTERY_HIGH_THRESHOLD = 80 // Turn off switch when battery > 80%
    
    // Tuya IoT API Configuration
    const val TUYA_REGION = "eu" // Change to your region: eu, us, cn, in, etc.
    const val TUYA_ENVIRONMENT = "PROD" // PROD or TEST
}
```

For detailed Tuya setup instructions, see [TUYA_SETUP.md](TUYA_SETUP.md).

### Common MQTT Broker URLs

- **Mosquitto (local)**: `tcp://localhost:1883`
- **Mosquitto (remote)**: `tcp://your-server-ip:1883`
- **HiveMQ**: `tcp://broker.hivemq.com:1883`
- **Eclipse IoT**: `tcp://mqtt.eclipseprojects.io:1883`

## Building and Running

1. Open the project in Android Studio
2. Update the MQTT configuration in `MqttConfig.kt`
3. **Update the Tuya IoT configuration in `TuyaConfig.kt`**
4. Build and install the app on your device
5. Grant necessary permissions when prompted
6. Use the "Start Monitoring" button to begin battery monitoring

## Permissions

The app requires the following permissions:
- `INTERNET` - To connect to MQTT broker and Tuya IoT
- `ACCESS_NETWORK_STATE` - To check network connectivity
- `WAKE_LOCK` - To keep the service running
- `FOREGROUND_SERVICE` - To run the monitoring service in the background

## MQTT Message Format

The app publishes battery level data to the configured MQTT topic in the following format:

- **Topic**: `phone/battery/level` (configurable)
- **Payload**: Battery percentage as a string (e.g., "85.0")
- **QoS**: 0 (at most once delivery)

## Usage

1. **Start Monitoring**: Tap the "Start Monitoring" button to begin battery monitoring and MQTT reporting
2. **Stop Monitoring**: Tap the "Stop Monitoring" button to stop the service
3. **View Status**: The UI shows current battery level, MQTT connection status, and Tuya device status
4. **Manual Tuya Control**: Use the "Turn On" and "Turn Off" buttons to manually control your Tuya switch
5. **Background Operation**: The service continues running in the background even when the app is closed
6. **Automatic Control**: The app automatically controls your Tuya switch based on battery levels

## Troubleshooting

### MQTT Connection Issues

1. **Check broker URL**: Ensure the MQTT broker URL is correct and accessible
2. **Network connectivity**: Verify the device has internet access
3. **Authentication**: If your broker requires authentication, uncomment and set the username/password in `MqttConfig.kt`
4. **Firewall**: Ensure port 1883 (or your broker's port) is not blocked

### Tuya IoT Issues

1. **Check credentials**: Verify your Tuya Access ID, Access Key, and Device ID
2. **Device connectivity**: Ensure your Tuya device is online and connected to the internet
3. **Region setting**: Make sure the region setting matches your Tuya IoT project
4. **Device permissions**: Verify your device has the necessary permissions in Tuya IoT platform

### Battery Monitoring Issues

1. **Permissions**: Ensure all required permissions are granted
2. **Battery optimization**: Disable battery optimization for the app to prevent the service from being killed
3. **Service restart**: The service automatically restarts if killed by the system

## Customization

### Change Update Interval

Modify `BATTERY_UPDATE_INTERVAL_MS` in `MqttConfig.kt`:

```kotlin
const val BATTERY_UPDATE_INTERVAL_MS = 60000L // 1 minute (minimum 15 minutes for WorkManager)
```

### Adjust Battery Thresholds

Modify the battery thresholds in `TuyaConfig.kt`:

```kotlin
const val BATTERY_LOW_THRESHOLD = 15  // More conservative
const val BATTERY_HIGH_THRESHOLD = 90 // More aggressive charging
```

### Add Authentication

Uncomment and set authentication credentials in `MqttConfig.kt`:

```kotlin
const val MQTT_USERNAME = "your_username"
const val MQTT_PASSWORD = "your_password"
```

Then uncomment the corresponding lines in `BatteryMonitorWorker.kt`.

### Use SSL/TLS

For secure connections, use the SSL broker URL:

```kotlin
const val MQTT_BROKER_URL = "ssl://your-mqtt-broker:8883"
```

## Dependencies

- **MQTT Client**: Eclipse Paho MQTT Client v1.3.1
- **Tuya IoT SDK**: Tuya Smart SDK v3.25.0
- **Android Architecture Components**: ViewModel, LiveData
- **Material Design Components**: For modern UI elements
- **WorkManager**: For background task scheduling

## License

This project is open source and available under the MIT License. 