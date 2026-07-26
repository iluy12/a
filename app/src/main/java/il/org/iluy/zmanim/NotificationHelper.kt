package il.org.iluy.zmanim

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NotificationHelper {
    private const val CHANNEL_ID = "iluy_zmanim_alerts"
    private val timeFormat = SimpleDateFormat("HH:mm", Locale("iw", "IL"))

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "התראות זמנים",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /** title e.g. "עוד 10 דק' סוף זמן קר\"ש"; region + sync time shown as the small line. */
    fun showZmanAlert(context: Context, title: String, regionName: String, syncedAtMillis: Long, notificationId: Int) {
        ensureChannel(context)
        val syncedTime = timeFormat.format(Date(syncedAtMillis))
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText("אזור: $regionName · סונכרן לאחרונה בשעה $syncedTime")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }
}
