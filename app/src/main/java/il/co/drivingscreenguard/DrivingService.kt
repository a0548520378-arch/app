package il.co.drivingscreenguard
import android.app.*
import android.content.*
import android.os.*
import com.google.android.gms.location.*
import android.location.Location

class DrivingService:Service(){
    private lateinit var client:FusedLocationProviderClient
    private val cb=object:LocationCallback(){override fun onLocationResult(r:LocationResult){r.locations.forEach{update(it)}}}
    override fun onCreate(){super.onCreate(); startForeground(7,notification()); client=LocationServices.getFusedLocationProviderClient(this);request()}
    private fun request(){val req=LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,2000).setMinUpdateIntervalMillis(1000).setMinUpdateDistanceMeters(3f).build();try{client.requestLocationUpdates(req,cb,Looper.getMainLooper())}catch(_:SecurityException){}}
    private fun update(l:Location){
        DrivingState.driving=l.speed>=15f/3.6f
        if(DrivingState.driving && !Prefs.disabled(this)) BlockAccessibilityService.block(this) else BlockAccessibilityService.unblock(this)
    }
    private fun notification():Notification{val ch="drive";if(Build.VERSION.SDK_INT>=26)getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(ch,"ניטור נסיעה",NotificationManager.IMPORTANCE_LOW));return Notification.Builder(this,ch).setContentTitle("מגן מסך בנסיעה").setContentText("ניטור מהירות פעיל").setSmallIcon(il.co.drivingscreenguard.R.drawable.ic_launcher).build()}
    override fun onBind(i:Intent?)=null
    override fun onDestroy(){client.removeLocationUpdates(cb);DrivingState.driving=false;BlockAccessibilityService.unblock(this);super.onDestroy()}
}
