package il.org.iluy.zmanim

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Wake chain per cycle (shacharit / mincha):
 *   PRECALC (no GPS)  -> compute all-region table, save it, schedule CHECK
 *   CHECK    (1x GPS) -> match region, schedule ALERT for that region's exact time
 *   ALERT              -> show the notification
 *
 * Each step is a few seconds of work; the process is free to be killed between them.
 */
class ZmanimAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getStringExtra(AlarmScheduler.EXTRA_TYPE)) {
            AlarmScheduler.TYPE_PRECALC_SHACHARIT -> handlePrecalcShacharit(context)
            AlarmScheduler.TYPE_CHECK_SHACHARIT -> handleCheck(context, isShacharit = true)
            AlarmScheduler.TYPE_PRECALC_MINCHA -> handlePrecalcMincha(context)
            AlarmScheduler.TYPE_CHECK_MINCHA -> handleCheck(context, isShacharit = false)
            "alert_shacharit" -> showAlert(context, intent, "עוד 10 דק' סוף זמן קר\"ש")
            "alert_mincha" -> showAlert(context, intent, "עוד 10 דק' סוף זמן מנחה")
        }
    }

    private fun handlePrecalcShacharit(context: Context) {
        val table = DailyZmanimCalculator.sofZmanShemaForAllRegions()
        ZmanimStore.saveShema(context, table)
        val earliest = table.first().epochMillis
        AlarmScheduler.scheduleCheckAlarm(context, AlarmScheduler.TYPE_CHECK_SHACHARIT, earliest, requestCode = 11)
    }

    private fun handlePrecalcMincha(context: Context) {
        val table = DailyZmanimCalculator.sofZmanMinchaForAllRegions()
        ZmanimStore.saveMincha(context, table)
        val earliest = table.first().epochMillis
        AlarmScheduler.scheduleCheckAlarm(context, AlarmScheduler.TYPE_CHECK_MINCHA, earliest, requestCode = 12)
    }

    private fun handleCheck(context: Context, isShacharit: Boolean) {
        val pendingResult = goAsync()
        LocationOneShot.requestSingleFix(context) { location ->
            try {
                val table = if (isShacharit) ZmanimStore.loadShema(context) else ZmanimStore.loadMincha(context)
                if (table.isNotEmpty()) {
                    val region = if (location != null) {
                        RegionTable.nearestTo(location.latitude, location.longitude)
                    } else {
                        // No fix available — fall back to the earliest (safest/most conservative) region.
                        table.first().region
                    }
                    val match = table.find { it.region.displayName == region.displayName } ?: table.first()
                    val alertType = if (isShacharit) "alert_shacharit" else "alert_mincha"
                    val requestCode = if (isShacharit) 101 else 102
                    AlarmScheduler.scheduleAlertAlarm(context, alertType, match.epochMillis, requestCode, region.displayName)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showAlert(context: Context, intent: Intent, title: String) {
        val regionName = intent.getStringExtra("region_name") ?: "לא ידוע"
        val id = if (title.contains("קר\"ש")) 1 else 2
        NotificationHelper.showZmanAlert(
            context = context,
            title = title,
            regionName = regionName,
            syncedAtMillis = System.currentTimeMillis(),
            notificationId = id
        )
    }
}
