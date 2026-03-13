package com.patatus.axioma.core.hardware.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class LocationCoordinates(
    val latitude: Double,
    val longitude: Double
)

sealed class LocationResult2 {
    data class Success(val coordinates: LocationCoordinates) : LocationResult2()
    data object GpsDisabled : LocationResult2()
    data object PermissionDenied : LocationResult2()
    data class Error(val message: String) : LocationResult2()
}

@Singleton
class LocationCapture @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun isGpsEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationResult2 {
        if (!hasLocationPermission(context)) return LocationResult2.PermissionDenied
        if (!isGpsEnabled()) return LocationResult2.GpsDisabled

        return suspendCancellableCoroutine { continuation ->
            // Try last known location first for speed
            fusedClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(
                            LocationResult2.Success(
                                LocationCoordinates(location.latitude, location.longitude)
                            )
                        )
                    } else {
                        // No cached location — request a fresh one
                        requestFreshLocation(continuation)
                    }
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestFreshLocation(
        continuation: kotlin.coroutines.Continuation<LocationResult2>
    ) {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .setMaxUpdates(1)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                fusedClient.removeLocationUpdates(this)
                val loc = result.lastLocation
                if (loc != null) {
                    continuation.resume(
                        LocationResult2.Success(LocationCoordinates(loc.latitude, loc.longitude))
                    )
                } else {
                    continuation.resume(LocationResult2.Error("No se pudo obtener la ubicación"))
                }
            }
        }

        fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        (continuation as? kotlinx.coroutines.CancellableContinuation)?.invokeOnCancellation {
            fusedClient.removeLocationUpdates(callback)
        }
    }

    @SuppressLint("MissingPermission")
    fun observeLocation(intervalMs: Long = 10_000L): Flow<LocationCoordinates> = callbackFlow {
        if (!hasLocationPermission(context) || !isGpsEnabled()) {
            close()
            return@callbackFlow
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs).build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    trySend(LocationCoordinates(it.latitude, it.longitude))
                }
            }
        }

        fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        awaitClose { fusedClient.removeLocationUpdates(callback) }
    }
}