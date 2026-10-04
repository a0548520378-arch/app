package il.co.drivingscreenguard
import android.content.Context
object Prefs {
    private const val P="guard"
    fun pauseUntil(c:Context)=c.getSharedPreferences(P,0).getLong("pause_until",0)
    fun setPause(c:Context, minutes:Long)=c.getSharedPreferences(P,0).edit().putLong("pause_until",System.currentTimeMillis()+minutes*60_000L).apply()
    fun code(c:Context)=c.getSharedPreferences(P,0).getString("code","1234") ?: "1234"
    fun setCode(c:Context,s:String)=c.getSharedPreferences(P,0).edit().putString("code",s).apply()
    fun disabled(c:Context)=System.currentTimeMillis()<pauseUntil(c)
}
