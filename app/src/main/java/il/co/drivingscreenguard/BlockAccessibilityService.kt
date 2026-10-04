package il.co.drivingscreenguard
import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.view.*
import android.widget.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
class BlockAccessibilityService:AccessibilityService(){
    companion object { var instance:BlockAccessibilityService?=null; fun block(c:Context){instance?.show(c)}; fun unblock(c:Context){instance?.hide()} }
    private var overlay:View?=null
    override fun onServiceConnected(){instance=this}
    fun show(c:Context){
        val pkg=rootInActiveWindow?.packageName?.toString()?:""
        if(pkg=="com.waze" || pkg=="com.google.android.apps.maps" || pkg.contains("inputmethod")) {hide();return}
        if(overlay!=null)return
        val frame=FrameLayout(this).apply{setBackgroundColor(0x01000000);isClickable=true;isFocusable=true}
        val bar=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setBackgroundColor(0xEE202124.toInt());padding=18;layoutDirection=View.LAYOUT_DIRECTION_RTL}
        val msg=TextView(this).apply{text="המסך חסום בעת נסיעה";setTextColor(0xFFFFFFFF.toInt());textSize=16f;layoutParams=LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f)}
        val w=Button(this).apply{text="מעבר ל־Waze";setOnClickListener{try{startActivity(packageManager.getLaunchIntentForPackage("com.waze"))}catch(_:Exception){}}}
        val unlock=Button(this).apply{text="ביטול";setOnClickListener{codeDialog()}}
        bar.addView(msg);bar.addView(w);bar.addView(unlock);frame.addView(bar,FrameLayout.LayoutParams(-1,dp(64)).apply{gravity=Gravity.TOP})
        val lp=WindowManager.LayoutParams(-1,-1,if(Build.VERSION.SDK_INT>=26)WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,PixelFormat.TRANSLUCENT)
        getSystemService(WINDOW_SERVICE).let{it as WindowManager}.addView(frame,lp);overlay=frame
    }
    private fun codeDialog(){
        val e=EditText(this).apply{inputType=2 or 16}
        AlertDialog.Builder(this).setTitle("קוד ביטול").setView(e).setPositiveButton("אישור"){_,_->if(e.text.toString()==Prefs.code(this))hide()}.setNegativeButton("ביטול",null).show()
    }
    fun hide(){overlay?.let{(getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it)};overlay=null}
    private fun dp(v:Int)= (v*resources.displayMetrics.density).toInt()
    override fun onAccessibilityEvent(e:android.view.accessibility.AccessibilityEvent?) {}
    override fun onInterrupt(){}
}
