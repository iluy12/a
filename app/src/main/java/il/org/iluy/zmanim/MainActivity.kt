package il.org.iluy.zmanim

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Minimal UI — this is a proof-of-concept to test sideload + alarm chain on the
 * S10PRO, not the final product screen. A real settings screen (region override,
 * etc.) can replace this once the platform questions are resolved.
 *
 * Plain Activity (not AppCompatActivity) on purpose: the manifest uses the
 * device's built-in Theme.DeviceDefault, not a Theme.AppCompat theme. Mixing
 * AppCompatActivity with a non-AppCompat theme crashes immediately on launch.
 */
class MainActivity : Activity() {

    private lateinit var statusView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            buildUi()
        } catch (t: Throwable) {
            // No ADB/logcat access on this device — show the real error on screen
            // instead of a silent crash, so it can be photographed and reported.
            val errorView = TextView(this).apply {
                text = "שגיאה באתחול:\n\n${t.javaClass.simpleName}: ${t.message}\n\n${t.stackTraceToString().take(2000)}"
                setPadding(24, 24, 24, 24)
                textSize = 12f
            }
            setContentView(errorView)
        }
    }

    private fun buildUi() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 64, 32, 32)
        }

        statusView = TextView(this).apply { text = "עילוי — בדיקת זמנים (POC)" }
        root.addView(statusView)

        val requestPermButton = Button(this).apply {
            text = "בקש הרשאת מיקום"
            setOnClickListener { requestLocationPermission() }
        }
        root.addView(requestPermButton)

        val scheduleButton = Button(this).apply {
            text = "תזמן התראות יומיות"
            setOnClickListener {
                AlarmScheduler.scheduleDailyPrecalcAlarms(this@MainActivity)
                statusView.text = "תוזמן: חישוב שחרית 04:30, חישוב מנחה 11:30"
            }
        }
        root.addView(scheduleButton)

        val testNowButton = Button(this).apply {
            text = "בדיקה מיידית (מדמה עכשיו את שרשרת קר\"ש)"
            setOnClickListener { runImmediateShemaTest() }
        }
        root.addView(testNowButton)

        setContentView(root)
        NotificationHelper.ensureChannel(this)
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION
            )
        } else {
            statusView.text = "הרשאת מיקום כבר קיימת"
        }
    }

    /** For today's bring-up test: runs the precalc -> GPS check -> notification chain immediately. */
    private fun runImmediateShemaTest() {
        val table = DailyZmanimCalculator.sofZmanShemaForAllRegions()
        ZmanimStore.saveShema(this, table)
        LocationOneShot.requestSingleFix(this) { location ->
            val region = if (location != null) {
                RegionTable.nearestTo(location.latitude, location.longitude)
            } else {
                table.first().region
            }
            val match = table.find { it.region.displayName == region.displayName } ?: table.first()
            NotificationHelper.showZmanAlert(
                context = this,
                title = "בדיקה — עוד 10 דק' סוף זמן קר\"ש (מדומה)",
                regionName = match.region.displayName,
                syncedAtMillis = System.currentTimeMillis(),
                notificationId = 999
            )
            runOnUiThread {
                statusView.text = "נשלחה התראת בדיקה. אזור שזוהה: ${match.region.displayName}"
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION = 1
    }
}
