package il.co.drivingscreenguard

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private lateinit var status: TextView
    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
    }

    private fun buildUi() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 28, 28, 28)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        root.addView(TextView(this).apply {
            text = "מגן מסך בנסיעה"
            textSize = 28f
            setPadding(0, 0, 0, 16)
        })

        status = TextView(this).apply {
            textSize = 17f
            setPadding(0, 0, 0, 18)
        }
        root.addView(status)

        addButton("הפעלת שירות החסימה") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        addButton("הפעל ניטור נסיעה") {
            if (hasForegroundLocation()) {
                startForegroundServiceCompat()
            } else {
                requestLocation()
            }
        }

        addButton("אפשר מיקום תמיד") {
            openLocationSettings()
        }

        addButton("הפעל אוטומטית לאחר הפעלה מחדש") {
            startupDialog()
        }

        addButton("השהיית החסימה") {
            pauseDialog()
        }

        addButton("הגדרות וקוד ביטול") {
            codeDialog()
        }

        addButton("פתח Waze") { launch("com.waze") }
        addButton("פתח Google Maps") { launch("com.google.android.apps.maps") }

        setContentView(root)
        refresh()
    }

    private fun addButton(label: String, action: () -> Unit) {
        root.addView(Button(this).apply {
            text = label
            setOnClickListener { action() }
        })
    }

    private fun refresh() {
        status.text = when {
            Prefs.disabled(this) ->
                "החסימה מושהית כרגע."

            hasBackgroundLocation() ->
                "הגנה פעילה: חסימה מ־15 קמ״ש ומעלה. ניטור אוטומטי לאחר אתחול מוכן."

            else ->
                "הגנה פעילה, אך כדי לעבוד גם אחרי אתחול יש לאשר "מיקום תמיד"."
        }
    }

    private fun requestLocation() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        ActivityCompat.requestPermissions(this, permissions, 10)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != 10) return

        refresh()

        if (hasForegroundLocation()) {
            startForegroundServiceCompat()
        }

        if (hasForegroundLocation() &&
            Build.VERSION.SDK_INT >= 29 &&
            !hasBackgroundLocation()
        ) {
            Toast.makeText(
                this,
                "כדי שהניטור יעבוד לאחר אתחול וברקע: לחץ על "אפשר מיקום תמיד".",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun hasForegroundLocation(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun hasBackgroundLocation(): Boolean {
        if (Build.VERSION.SDK_INT < 29) return hasForegroundLocation()

        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun startForegroundServiceCompat() {
        if (!hasForegroundLocation()) {
            Toast.makeText(
                this,
                "יש לאשר הרשאת מיקום תחילה.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val intent = Intent(this, DrivingService::class.java)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (_: Exception) {
            Toast.makeText(
                this,
                "לא ניתן להפעיל כרגע את שירות הניטור.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun openLocationSettings() {
        try {
            startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:$packageName")
                )
            )
        } catch (_: Exception) {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    private fun startupDialog() {
        val msg = buildString {
            append("האפליקציה כוללת הפעלה אוטומטית לאחר BOOT_COMPLETED ועדכון האפליקציה.\n\n")
            append("כדי שהניטור יוכל לפעול גם כשהאפליקציה אינה פתוחה, יש לאשר הרשאת "מיקום תמיד".\n\n")
            append("בחלק מהמכשירים יש גם הגדרת יצרן בשם "הפעלה אוטומטית" או "ללא הגבלת סוללה".")
        }

        AlertDialog.Builder(this)
            .setTitle("הפעלה אוטומטית ברקע")
            .setMessage(msg)
            .setPositiveButton("הגדרות סוללה") { _, _ ->
                openBatterySettings()
            }
            .setNeutralButton("מיקום תמיד") { _, _ ->
                openLocationSettings()
            }
            .setNegativeButton("נגישות") { _, _ ->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            .show()
    }

    private fun openBatterySettings() {
        try {
            startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
            )
        } catch (_: Exception) {
            startActivity(
                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            )
        }
    }

    private fun pauseDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "1–100000 דקות"
        }

        AlertDialog.Builder(this)
            .setTitle("השהיית החסימה")
            .setMessage("בחר לכמה דקות להשהות את החסימה")
            .setView(input)
            .setPositiveButton("השהה") { _, _ ->
                val minutes = input.text.toString().toLongOrNull() ?: 0
                if (minutes in 1..100000) {
                    Prefs.setPause(this, minutes)
                    refresh()
                }
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    private fun codeDialog() {
        val input = EditText(this).apply {
            inputType =
                InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "קוד חדש (לפחות 4 ספרות)"
        }

        AlertDialog.Builder(this)
            .setTitle("קוד ביטול חסימה")
            .setMessage("ברירת המחדל: 1234")
            .setView(input)
            .setPositiveButton("שמירה") { _, _ ->
                if (input.text.length >= 4) {
                    Prefs.setCode(this, input.text.toString())
                }
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    private fun launch(packageName: String) {
        try {
            startActivity(packageManager.getLaunchIntentForPackage(packageName))
        } catch (_: Exception) {
            Toast.makeText(
                this,
                "האפליקציה אינה מותקנת",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}