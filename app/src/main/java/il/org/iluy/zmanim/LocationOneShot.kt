package il.org.iluy.zmanim

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat

/**
 * Deliberately uses plain android.location.LocationManager rather than
 * FusedLocationProviderClient: this watch chipset (ASR8601) is not confirmed
 * to ship full Google Play Services, so avoid that dependency entirely.
 * Takes one fix and stops — this is a periodic wake-and-check, not tracking.
 */
object LocationOneShot {

    private const val TIMEOUT_MS = 20_000L

    fun requestSingleFix(context: Context, onResult: (Location?) -> Unit) {
        val hasPermission = ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            onResult(null)
            return
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val provider = when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }

        if (provider == null) {
            // No provider available — fall back to last known fix from either provider.
            val last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            onResult(last)
            return
        }

        var resolved = false
        val handler = android.os.Handler(Looper.getMainLooper())

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (resolved) return
                resolved = true
                locationManager.removeUpdates(this)
                onResult(location)
            }
        }

        handler.postDelayed({
            if (!resolved) {
                resolved = true
                locationManager.removeUpdates(listener)
                val last = locationManager.getLastKnownLocation(provider)
                onResult(last)
            }
        }, TIMEOUT_MS)

        try {
            locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
        } catch (e: SecurityException) {
            resolved = true
            onResult(null)
        }
    }
}
