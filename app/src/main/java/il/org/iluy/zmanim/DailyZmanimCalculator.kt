package il.org.iluy.zmanim

import com.kosherjava.zmanim.ComplexZmanimCalendar
import com.kosherjava.zmanim.util.GeoLocation
import java.util.Calendar
import java.util.TimeZone

data class RegionZman(val region: Region, val epochMillis: Long)

/**
 * Runs entirely offline (astronomical formulas, no network). Cheap enough to
 * run for every region every morning — nothing here needs to be cached to disk.
 */
object DailyZmanimCalculator {

    private val ISRAEL_TZ: TimeZone = TimeZone.getTimeZone("Asia/Jerusalem")

    /** Sof zman Krias Shema (GRA) for every region, for the given date. */
    fun sofZmanShemaForAllRegions(date: Calendar = Calendar.getInstance(ISRAEL_TZ)): List<RegionZman> =
        RegionTable.regions.map { region ->
            val cal = calendarFor(region, date)
            RegionZman(region, cal.sofZmanShmaGRA.time)
        }.sortedBy { it.epochMillis }

    /** Sof zman Mincha (GRA / mincha ketana as the stricter/earlier boundary) for every region. */
    fun sofZmanMinchaForAllRegions(date: Calendar = Calendar.getInstance(ISRAEL_TZ)): List<RegionZman> =
        RegionTable.regions.map { region ->
            val cal = calendarFor(region, date)
            RegionZman(region, cal.minchaKetana.time)
        }.sortedBy { it.epochMillis }

    private fun calendarFor(region: Region, date: Calendar): ComplexZmanimCalendar {
        val geo = GeoLocation(
            region.displayName,
            region.lat,
            region.lon,
            region.elevationMeters,
            ISRAEL_TZ
        )
        val zmanimCalendar = ComplexZmanimCalendar(geo)
        zmanimCalendar.calendar = date
        return zmanimCalendar
    }
}
