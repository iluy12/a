package il.org.iluy.zmanim

/**
 * Fixed coverage grid for Israel. Zman differences between north/south can reach
 * tens of minutes, east/west far less — this resolution is intentionally coarse.
 * Add rows here to raise resolution; no other code needs to change.
 */
data class Region(
    val displayName: String,
    val lat: Double,
    val lon: Double,
    val elevationMeters: Double = 0.0
)

object RegionTable {
    val regions = listOf(
        Region("ירושלים", 31.7683, 35.2137, 754.0),
        Region("תל אביב", 32.0853, 34.7818),
        Region("חיפה", 32.7940, 34.9896),
        Region("באר שבע", 31.2530, 34.7915),
        Region("אילת", 29.5581, 34.9482),
        Region("צפת", 32.9646, 35.4960, 900.0),
        Region("טבריה", 32.7940, 35.5312, -200.0),
        Region("נהריה", 33.0090, 35.0930),
        Region("קריית שמונה", 33.2075, 35.5697, 100.0),
        Region("עפולה", 32.6078, 35.2897),
        Region("נתניה", 32.3215, 34.8532),
        Region("חריש", 32.4646, 35.0480),
        Region("מודיעין", 31.8928, 35.0104),
        Region("אשדוד", 31.8014, 34.6435),
        Region("אשקלון", 31.6688, 34.5742),
        Region("קריית גת", 31.6100, 34.7642),
        Region("דימונה", 31.0672, 35.0327),
        Region("ים המלח", 31.5000, 35.4700, -400.0)
    )

    /** Nearest region by simple planar distance — good enough at this grid density. */
    fun nearestTo(lat: Double, lon: Double): Region {
        return regions.minByOrNull { r ->
            val dLat = r.lat - lat
            val dLon = r.lon - lon
            dLat * dLat + dLon * dLon
        } ?: regions.first()
    }
}
