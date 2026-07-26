package il.org.iluy.zmanim

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object AlarmScheduler {

    const val EXTRA_TYPE = "alarm_type"
    const val TYPE_PRECALC_SHACHARIT = "precalc_shacharit"
    const val TYPE_CHECK_SHACHARIT = "check_shacharit"
    const val TYPE_PRECALC_MINCHA = "precalc_mincha"
    const val TYPE_CHECK_MINCHA = "check_mincha"

    // Safety margin: the "check" wake fires this many minutes before the
    // EARLIEST region's zman, so a GPS fix + region match + reschedule always
    // completes before the real 10-minutes-before alert is due anywhere in the grid.
    private const val CHECK_MARGIN_MINUTES = 25
    private const val ALERT_LEAD_MINUTES = 10

    fun scheduleDailyPrecalcAlarms(context: Context) {
        // Shacharit-window precalc: well before sunrise-driven zmanim, e.g. 04:30.
        scheduleAt(context, TYPE_PRECALC_SHACHARIT, hour = 4, minute = 30, requestCode = 1)
        // Mincha-window precalc: once past halachic midday for the whole country, e.g. 11:30.
        scheduleAt(context, TYPE_PRECALC_MINCHA, hour = 11, minute = 30, requestCode = 2)
    }

    fun scheduleCheckAlarm(context: Context, type: String, earliestEpochMillis: Long, requestCode: Int) {
        val triggerAt = earliestEpochMillis - CHECK_MARGIN_MINUTES * 60_000L
        scheduleExactAt(context, type, triggerAt, requestCode)
    }

    fun scheduleAlertAlarm(context: Context, alertType: String, regionEpochMillis: Long, requestCode: Int, regionName: String) {
        val triggerAt = regionEpochMillis - ALERT_LEAD_MINUTES * 60_000L
        val intent = Intent(context, ZmanimAlarmReceiver::class.java).apply {
            putExtra(EXTRA_TYPE, alertType)
            putExtra("region_name", regionName)
            putExtra("region_epoch", regionEpochMillis)
        }
        val pending = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
    }

    private fun scheduleAt(context: Context, type: String, hour: Int, minute: Int, requestCode: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }
        scheduleExactAt(context, type, cal.timeInMillis, requestCode)
    }

    private fun scheduleExactAt(context: Context, type: String, triggerAt: Long, requestCode: Int) {
        val intent = Intent(context, ZmanimAlarmReceiver::class.java).apply {
            putExtra(EXTRA_TYPE, type)
        }
        val pending = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
    }
}
