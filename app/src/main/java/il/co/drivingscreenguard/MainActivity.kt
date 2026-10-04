package il.co.drivingscreenguard
import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.provider.Settings
import android.text.InputType
import android.view.*
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat

class MainActivity: ComponentActivity() {
    private lateinit var status: TextView
    override fun onCreate(b:Bundle?) { super.onCreate(b); buildUi() }
    private fun buildUi() {
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;padding(28);layoutDirection=View.LAYOUT_DIRECTION_RTL}
        val title=TextView(this).apply{text="מגן מסך בנסיעה";textSize=28f;setPadding(0,0,0,24)}
        status=TextView(this).apply{textSize=17f}
        root.addView(title); root.addView(status)
        button("הפעלת שירות החסימה"){startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))}
        button("הפעל ניטור נסיעה"){requestLocation(); startService(Intent(this,DrivingService::class.java))}
        button("השהיה"){pauseDialog()}
        button("הגדרות וקוד ביטול"){codeDialog()}
        button("פתח Waze"){launch("com.waze")}
        setContentView(root); refresh()
    }
    private fun button(t:String, f:()->Unit){findViewById<LinearLayout>(android.R.id.content)?.let{}; val v=Button(this).apply{text=t;setOnClickListener{f()}};(window.decorView.findViewById<ViewGroup>(android.R.id.content).getChildAt(0) as? LinearLayout)?.addView(v)}
    private fun refresh(){status.text=if(Prefs.disabled(this))"החסימה מושהית כרגע." else "הגנה פעילה: חסימה מ־15 קמ״ש ומעלה."}
    private fun requestLocation(){
        if(Build.VERSION.SDK_INT>=33) requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.POST_NOTIFICATIONS),10)
        else requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),10)
    }
    private fun pauseDialog(){
        val e=EditText(this).apply{inputType=InputType.TYPE_CLASS_NUMBER;hint="דקות (עד 100000)"}
        AlertDialog.Builder(this).setTitle("השהיית החסימה").setMessage("הזן מספר דקות").setView(e).setPositiveButton("השהה"){_,_->val n=e.text.toString().toLongOrNull()?:0; if(n in 1..100000){Prefs.setPause(this,n);refresh()}}.setNegativeButton("ביטול",null).show()
    }
    private fun codeDialog(){
        val e=EditText(this).apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD;hint="קוד חדש"}
        AlertDialog.Builder(this).setTitle("קוד ביטול חסימה").setMessage("קוד ברירת המחדל הוא 1234").setView(e).setPositiveButton("שמירה"){_,_->if(e.text.length>=4)Prefs.setCode(this,e.text.toString())}.setNegativeButton("ביטול",null).show()
    }
    private fun launch(pkg:String){try{startActivity(packageManager.getLaunchIntentForPackage(pkg))}catch(_:Exception){}}
}
