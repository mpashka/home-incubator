# Migration from Service to WorkManager

This document describes the migration from using Android Service to WorkManager for periodic battery monitoring tasks.

## Changes Made

### 1. Dependencies Added
- Added `androidx.work:work-runtime-ktx:2.9.1` dependency for WorkManager support

### 2. New Files Created
- `BatteryMonitorWorker.kt` - Replaces `BatteryMonitorService.kt`
- `BatteryMonitorManager.kt` - Utility class for managing WorkManager lifecycle

### 3. Files Modified
- `MainActivity.kt` - Updated to use WorkManager instead of Service
- `MqttConfig.kt` - Updated battery update interval to 15 minutes (WorkManager minimum)
- `AndroidManifest.xml` - Removed Service declaration
- `build.gradle.kts` - Added WorkManager dependency
- `gradle/libs.versions.toml` - Added WorkManager version

### 4. Files Removed
- `BatteryMonitorService.kt` - No longer needed

## Key Differences

### Service vs WorkManager

| Aspect | Service | WorkManager |
|--------|---------|-------------|
| **Lifecycle** | Runs continuously until stopped | Runs periodically with constraints |
| **Battery Impact** | High (keeps app alive) | Low (optimized by system) |
| **Minimum Interval** | No limit | 15 minutes for periodic work |
| **System Kills** | Can be killed by system | Survives system kills |
| **Constraints** | Manual implementation | Built-in support |
| **Foreground Service** | Required for long-running tasks | Not needed |

### Benefits of WorkManager

1. **Better Battery Life**: WorkManager is optimized by the system and doesn't keep the app alive unnecessarily
2. **Automatic Retry**: Built-in retry mechanism with backoff policies
3. **Constraints**: Easy to set network, battery, and other constraints
4. **System Integration**: Survives app updates, reboots, and system kills
5. **No Foreground Service**: No need for persistent notification

## Usage

### Starting Battery Monitoring

```kotlin
// Start periodic battery monitoring
BatteryMonitorManager.startMonitoring(context)
```

### Stopping Battery Monitoring

```kotlin
// Stop periodic battery monitoring
BatteryMonitorManager.stopMonitoring(context)
```

### Checking Status

```kotlin
// Check if monitoring is active
val isActive = BatteryMonitorManager.isMonitoringActive(context)

// Get current work status
val status = BatteryMonitorManager.getMonitoringStatus(context)
```

## Configuration

### Update Interval
The battery monitoring interval is set to 15 minutes (minimum for WorkManager periodic work) in `MqttConfig.kt`:

```kotlin
const val BATTERY_UPDATE_INTERVAL_MS = 15 * 60 * 1000L // 15 minutes
```

### Constraints
The WorkManager is configured with network constraints to ensure MQTT connectivity:

```kotlin
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .build()
```

### Retry Policy
If the work fails, it will retry with linear backoff:

```kotlin
.setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
```

## Migration Notes

1. **Interval Change**: The monitoring interval changed from 30 seconds to 15 minutes due to WorkManager limitations
2. **No Persistent Notification**: WorkManager doesn't require a foreground service notification
3. **Automatic Restart**: WorkManager automatically restarts after system reboots
4. **Better Resource Management**: The system can better manage when the work runs

## Testing

To test the WorkManager implementation:

1. Build and install the app
2. Start battery monitoring via the FAB button
3. Check logs for "Battery monitoring work started" messages
4. Monitor MQTT broker for battery level updates every 15 minutes
5. Test app restart - monitoring should continue automatically 