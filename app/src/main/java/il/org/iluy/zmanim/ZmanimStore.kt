package il.org.iluy.zmanim

import android.content.Context

/**
 * Persists the precomputed per-region table so the "check" alarm (which may fire
 * hours after the process that computed it was killed) can read it back.
 * Deliberately not a database — this is at most ~18 rows, twice a day.
 */
object ZmanimStore {
    private const val PREFS = "iluy_zmanim_store"

    fun saveShema(context: Context, table: List<RegionZman>) = save(context, "shema", table)
    fun saveMincha(context: Context, table: List<RegionZman>) = save(context, "mincha", table)

    fun loadShema(context: Context): List<RegionZman> = load(context, "shema")
    fun loadMincha(context: Context): List<RegionZman> = load(context, "mincha")

    private fun save(context: Context, key: String, table: List<RegionZman>) {
        val serialized = table.joinToString(";") { "${it.region.displayName},${it.epochMillis}" }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(key, serialized)
            .apply()
    }

    private fun load(context: Context, key: String): List<RegionZman> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(key, null) ?: return emptyList()
        return raw.split(";").mapNotNull { entry ->
            val parts = entry.split(",")
            if (parts.size != 2) return@mapNotNull null
            val region = RegionTable.regions.find { it.displayName == parts[0] } ?: return@mapNotNull null
            val millis = parts[1].toLongOrNull() ?: return@mapNotNull null
            RegionZman(region, millis)
        }
    }
}
