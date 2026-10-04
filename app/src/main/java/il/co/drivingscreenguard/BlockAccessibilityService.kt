package il.co.drivingscreenguard
import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.view.*
import android.widget.*
import android.content.*
import android.text.InputType
import android.os.Build
import android.view.accessibility.AccessibilityEvent

class BlockAccessibilityService:AccessibilityService(){
    companion object { var instance:BlockAccessibilityService?=null; fun block(c:Context){instance?.showIfNeeded()}; fun unblock(c:Context){instance?.hide()} }
    private var overlay:View?=null
    private var currentPackage=""
    override fun onServiceConnected(){instance=this}
    override fun onAccessibilityEvent(e:AccessibilityEvent?){
        currentPackage=e?.packageName?.toString() ?: currentPackage
        if(isExempt()) hide() else if(DrivingState.driving && !Prefs.disabled(this)) showIfNeeded()
    }
    private fun isExempt():Boolean = currentPackage=="com.waze" || currentPackage=="com.google.android.apps.maps" || currentPackage.contains("inputmethod",true) || currentPackage.contains("keyboard",true)
    fun showIfNeeded(){
        if(isExempt() || Prefs.disabled(this) || !DrivingState.driving || overlay!=null)return
        val frame=FrameLayout(this).apply{setBackgroundColor(0x01000000);isClickable=true;isFocusable=true}
        val bar=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setBackgroundColor(0xEE202124.toInt());padding=14;layoutDirection=View.LAYOUT_DIRECTION_RTL}
        val msg=TextView(this).apply{text="המסך חסום בעת נסיעה";setTextColor(0xFFFFFFFF.toInt());textSize=16f;layoutParams=LinearLayout.LayoutParams(0,-2,1f)}
        val w=Button(this).apply{text="מעבר ל־Waze";setOnClickListener{try{startActivity(packageManager.getLaunchIntentForPackage("com.waze"))}catch(_:Exception){}}}
        val unlock=Button(this).apply{text="ביטול";setOnClickListener{unlockDialog(frame)}}
        bar.addView(msg);bar.addView(w);bar.addView(unlock);frame.addView(bar,FrameLayout.LayoutParams(-1,dp(64)).apply{gravity=Gravity.TOP})
        val lp=WindowManager.LayoutParams(-1,-1,if(Build.VERSION.SDK_INT>=26)WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,PixelFormat.TRANSLUCENT)
        (getSystemService(WINDOW_SERVICE) as WindowManager).addView(frame,lp);overlay=frame
    }
    private fun unlockDialog(parent:FrameLayout){
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(30,25,30,25);setBackgroundColor(0xF5FFFFFF.toInt());layoutDirection=View.LAYOUT_DIRECTION_RTL}
        val input=EditText(this).apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD;hint="קוד ביטול"}
        val ok=Button(this).apply{text="אישור"}
        val cancel=Button(this).apply{text="ביטול"}
        box.addView(TextView(this).apply{text="הזן קוד לביטול החסימה";textSize=18f})
        box.addView(input);box.addView(ok);box.addView(cancel)
        parent.addView(box,FrameLayout.LayoutParams(dp(320),-2).apply{gravity=Gravity.CENTER})
        ok.setOnClickListener{if(input.text.toString()==Prefs.code(this))hide();else input.error="קוד שגוי"}
        cancel.setOnClickListener{parent.removeView(box)}
    }
    fun hide(){overlay?.let{(getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it)};overlay=null}
    private fun dp(v:Int)= (v*resources.displayMetrics.density).toInt()
    override fun onInterrupt(){}
}
