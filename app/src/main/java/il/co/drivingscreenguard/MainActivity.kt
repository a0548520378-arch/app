package il.co.drivingscreenguard
import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.os.*
import android.provider.Settings
import android.text.InputType
import android.view.*
import android.widget.*
import androidx.activity.ComponentActivity

class MainActivity: ComponentActivity() {
    private lateinit var status: TextView
    private lateinit var root: LinearLayout
    override fun onCreate(b:Bundle?) { super.onCreate(b); buildUi() }
    private fun buildUi() {
        root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(28,28,28,28);layoutDirection=View.LAYOUT_DIRECTION_RTL}
        val title=TextView(this).apply{text="מגן מסך בנסיעה";textSize=28f;setPadding(0,0,0,24)}
        status=TextView(this).apply{textSize=17f;setPadding(0,0,0,18)}
        root.addView(title); root.addView(status)
        addButton("הפעלת שירות החסימה"){startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))}
        addButton("הפעל ניטור נסיעה"){requestLocation(); startService(Intent(this,DrivingService::class.java))}
        addButton("השהיית החסימה"){pauseDialog()}
        addButton("הגדרות וקוד ביטול"){codeDialog()}
        addButton("פתח Waze"){launch("com.waze")}
        addButton("פתח Google Maps"){launch("com.google.android.apps.maps")}
        setContentView(root); refresh()
    }
    private fun addButton(t:String,f:()->Unit){root.addView(Button(this).apply{text=t;setOnClickListener{f()}})}
    private fun refresh(){status.text=if(Prefs.disabled(this))"החסימה מושהית כרגע." else "הגנה פעילה: חסימה מ־15 קמ״ש ומעלה."}
    private fun requestLocation(){
        if(Build.VERSION.SDK_INT>=33) requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.POST_NOTIFICATIONS),10)
        else requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),10)
    }
    private fun pauseDialog(){
        val e=EditText(this).apply{inputType=InputType.TYPE_CLASS_NUMBER;hint="1–100000 דקות"}
        AlertDialog.Builder(this).setTitle("השהיית החסימה").setMessage("בחר לכמה דקות להשהות את החסימה").setView(e)
            .setPositiveButton("השהה"){_,_->val n=e.text.toString().toLongOrNull()?:0;if(n in 1..100000){Prefs.setPause(this,n);refresh()}}
            .setNegativeButton("ביטול",null).show()
    }
    private fun codeDialog(){
        val e=EditText(this).apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD;hint="קוד חדש (לפחות 4 ספרות)"}
        AlertDialog.Builder(this).setTitle("קוד ביטול חסימה").setMessage("ברירת המחדל: 1234").setView(e)
            .setPositiveButton("שמירה"){_,_->if(e.text.length>=4)Prefs.setCode(this,e.text.toString())}.setNegativeButton("ביטול",null).show()
    }
    private fun launch(pkg:String){try{startActivity(packageManager.getLaunchIntentForPackage(pkg))}catch(_:Exception){Toast.makeText(this,"האפליקציה אינה מותקנת",Toast.LENGTH_SHORT).show()}}
}
