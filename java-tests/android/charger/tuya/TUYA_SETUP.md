# Tuya IoT Integration Setup Guide

This guide will help you set up Tuya IoT integration for automatic battery monitoring and switch control.

## Prerequisites

1. A Tuya IoT account
2. A Tuya smart switch device
3. Tuya IoT Cloud access credentials

## Setup Steps

### 1. Get Tuya IoT Credentials

1. Go to [Tuya IoT Platform](https://iot.tuya.com/)
2. Create an account or sign in
3. Create a new cloud project
4. Get your Access ID and Access Key from the project settings

### 2. Get Your Device ID

1. In your Tuya IoT project, go to "Devices" section
2. Find your smart switch device
3. Copy the Device ID

### 3. Configure the App

1. Open `app/src/main/java/org/mpashka/hass/charger/tuya/TuyaConfig.kt`
2. Replace the placeholder values with your actual credentials:

```kotlin
object TuyaConfig {
    // Replace with your actual Tuya IoT credentials
    const val TUYA_ACCESS_ID = "your_actual_access_id"
    const val TUYA_ACCESS_KEY = "your_actual_access_key"
    const val TUYA_DEVICE_ID = "your_actual_device_id"
    
    // Battery thresholds (you can adjust these)
    const val BATTERY_LOW_THRESHOLD = 20 // Turn on switch when battery < 20%
    const val BATTERY_HIGH_THRESHOLD = 80 // Turn off switch when battery > 80%
    
    // Set your region
    const val TUYA_REGION = "eu" // Change to your region: eu, us, cn, in, etc.
    const val TUYA_ENVIRONMENT = "PROD"
}
```

### 4. Device Authentication

The current implementation assumes your device is already authenticated. If you need to implement device authentication:

1. You may need to implement a proper login flow in `TuyaDeviceManager.kt`
2. This could involve user authentication or device token management
3. Refer to Tuya IoT SDK documentation for authentication methods

### 5. Build and Test

1. Build the app: `./gradlew build`
2. Install on your device: `./gradlew installDebug`
3. Open the app and check the Tuya status in the home screen

## How It Works

### Automatic Control Logic

The app automatically controls your Tuya switch based on battery levels:

- **Battery ≤ 20%**: Turns ON the switch (starts charging)
- **Battery ≥ 80%**: Turns OFF the switch (stops charging)
- **Battery 20-80%**: No action (maintains current state)

### Monitoring Frequency

- Battery level is checked every 15 minutes (minimum allowed by WorkManager)
- MQTT messages are published with battery level updates
- Tuya switch status is checked before each control action

### Manual Control

You can also manually control the switch using the buttons in the app:
- "Turn On" button: Manually turn on the switch
- "Turn Off" button: Manually turn off the switch

## Troubleshooting

### Common Issues

1. **"Tuya device not online"**
   - Check your device ID is correct
   - Ensure your device is connected to the internet
   - Verify your Tuya IoT credentials

2. **"Failed to initialize Tuya IoT SDK"**
   - Check your Access ID and Access Key
   - Verify your region setting
   - Check internet connectivity

3. **Switch not responding**
   - Check device is online in Tuya app
   - Verify device ID matches your switch
   - Check device permissions in Tuya IoT platform

### Debug Information

Enable debug logging by checking the logs with:
```bash
adb logcat | grep -E "(TuyaDeviceManager|BatteryMonitorWorker)"
```

## Security Notes

- Never commit your actual Tuya credentials to version control
- Consider using encrypted storage for credentials
- Regularly rotate your Access Key
- Use environment-specific credentials (dev/prod)

## Customization

### Adjusting Battery Thresholds

You can modify the battery thresholds in `TuyaConfig.kt`:

```kotlin
const val BATTERY_LOW_THRESHOLD = 15  // More conservative
const val BATTERY_HIGH_THRESHOLD = 90 // More aggressive charging
```

### Adding More Devices

To control multiple Tuya devices:

1. Add additional device IDs to `TuyaConfig.kt`
2. Create multiple `TuyaDeviceManager` instances
3. Implement device-specific control logic

### Custom Control Logic

You can modify the control logic in `BatteryMonitorWorker.kt`:

```kotlin
private suspend fun controlTuyaSwitch(batteryLevel: Float) {
    // Add your custom logic here
    when {
        batteryLevel <= 10 -> {
            // Emergency charging
        }
        batteryLevel <= 20 -> {
            // Normal charging
        }
        // ... more conditions
    }
}
```

## Support

For issues with:
- Tuya IoT Platform: Check [Tuya IoT Documentation](https://developer.tuya.com/en/docs/iot/)
- App functionality: Check the logs and verify configuration
- Device connectivity: Use the Tuya Smart app to test device connectivity 