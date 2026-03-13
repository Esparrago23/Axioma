package com.patatus.axioma.core.hardware.notifications

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.patatus.axioma.core.hardware.location.LocationCapture
import com.patatus.axioma.core.hardware.location.LocationResult2
import com.patatus.axioma.features.users.domain.repositories.UsersRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class PushNotificationManager @Inject constructor(
    private val usersRepository: UsersRepository,
    private val locationCapture: LocationCapture,
    @ApplicationContext private val context: Context
) {
    suspend fun syncTokenAndCurrentLocation(explicitToken: String? = null): Result<Unit> {
        val tokenResult = explicitToken?.let { Result.success(it) } ?: fetchCurrentFcmToken()
        val token = tokenResult.getOrElse { return Result.failure(it) }

        val location = locationCapture.getCurrentLocation()
        val (latitude, longitude) = when (location) {
            is LocationResult2.Success -> location.coordinates.latitude to location.coordinates.longitude
            else -> null to null
        }

        val result = usersRepository.updatePushRegistration(
            fcmToken = token,
            lastLatitude = latitude,
            lastLongitude = longitude
        )

        if (result.isFailure) {
            PushSyncRetryWorker.enqueue(
                context = context,
                fcmToken = token,
                lastLatitude = latitude,
                lastLongitude = longitude,
            )
        }

        return result
    }

    suspend fun syncLocation(latitude: Double, longitude: Double): Result<Unit> {
        val result = usersRepository.updatePushRegistration(
            fcmToken = null,
            lastLatitude = latitude,
            lastLongitude = longitude
        )

        if (result.isFailure) {
            PushSyncRetryWorker.enqueue(
                context = context,
                lastLatitude = latitude,
                lastLongitude = longitude,
            )
        }

        return result
    }

    private suspend fun fetchCurrentFcmToken(): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    if (token.isNullOrBlank()) {
                        continuation.resume(Result.failure(Exception("No se pudo obtener FCM token")))
                    } else {
                        continuation.resume(Result.success(token))
                    }
                }
                .addOnFailureListener { error ->
                    continuation.resume(Result.failure(error))
                }
        }
    }
}