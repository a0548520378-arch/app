package il.co.drivingscreenguard

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.*
import com.google.android.gms.location.*

class DrivingService : Service() {
    private lateinit var client: FusedLocationProviderClient

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.locations.forEach { update(it.speed) }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startAsForeground()
        client = LocationServices.getFusedLocationProviderClient(this)
        requestUpdates()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(
                    "drive_monitor",
                    "ניטור נסיעה",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    private fun startAsForeground() {
        val notification = Notification.Builder(this, "drive_monitor")
            .setContentTitle("מגן מסך בנסיעה")
            .setContentText("הניטור פועל ברקע")
            .setSmallIcon(R.drawable.ic_launcher)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(7, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(7, notification)
        }
    }

    private fun requestUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L
        )
            .setMinUpdateIntervalMillis(1000L)
            .setMinUpdateDistanceMeters(3f)
            .build()

        try {
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        } catch (_: SecurityException) {
            // Permission not yet granted; MainActivity handles the permission flow.
        }
    }

    private fun update(speedMetersPerSecond: Float) {
        DrivingState.driving = speedMetersPerSecond >= 15f / 3.6f

        if (DrivingState.driving && !Prefs.disabled(this)) {
            BlockAccessibilityService.block(this)
        } else {
            BlockAccessibilityService.unblock(this)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        if (::client.isInitialized) client.removeLocationUpdates(callback)
        DrivingState.driving = false
        BlockAccessibilityService.unblock(this)
        super.onDestroy()
    }
}