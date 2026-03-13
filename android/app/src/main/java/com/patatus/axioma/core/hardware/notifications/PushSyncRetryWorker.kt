package com.patatus.axioma.core.hardware.notifications

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.patatus.axioma.BuildConfig
import com.patatus.axioma.core.network.TokenManager
import com.patatus.axioma.features.users.data.datasources.remote.api.UsersApiService
import com.patatus.axioma.features.users.data.datasources.remote.models.UpdateFcmTokenRequest
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class PushSyncRetryWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val service = createUsersService()

        val tokenInput = inputData.getString(KEY_FCM_TOKEN)
        val latitude = inputData.getDouble(KEY_LAST_LATITUDE, Double.NaN).takeUnless { it.isNaN() }
        val longitude = inputData.getDouble(KEY_LAST_LONGITUDE, Double.NaN).takeUnless { it.isNaN() }

        val token = when {
            tokenInput != null -> tokenInput
            latitude != null && longitude != null -> null
            else -> fetchCurrentFcmToken().getOrElse { return Result.retry() }
        }

        val request = UpdateFcmTokenRequest(
            fcmToken = token,
            lastLatitude = latitude,
            lastLongitude = longitude,
        )

        return try {
            val response = service.updateMyFcmToken(request)
            when {
                response.isSuccessful -> Result.success()
                response.code() in 500..599 -> Result.retry()
                else -> Result.failure()
            }
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun createUsersService(): UsersApiService {
        val authInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            TokenManager.getToken()?.let { requestBuilder.addHeader("Authorization", "Bearer $it") }
            chain.proceed(requestBuilder.build())
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL_API)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsersApiService::class.java)
    }

    private suspend fun fetchCurrentFcmToken(): kotlin.Result<String> {
        return suspendCancellableCoroutine { continuation ->
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    if (token.isNullOrBlank()) {
                        continuation.resume(kotlin.Result.failure(Exception("No se pudo obtener FCM token")))
                    } else {
                        continuation.resume(kotlin.Result.success(token))
                    }
                }
                .addOnFailureListener { error ->
                    continuation.resume(kotlin.Result.failure(error))
                }
        }
    }

    companion object {
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_LAST_LATITUDE = "last_latitude"
        private const val KEY_LAST_LONGITUDE = "last_longitude"

        fun enqueue(
            context: Context,
            fcmToken: String? = null,
            lastLatitude: Double? = null,
            lastLongitude: Double? = null,
        ) {
            val input = Data.Builder().apply {
                fcmToken?.let { putString(KEY_FCM_TOKEN, it) }
                lastLatitude?.let { putDouble(KEY_LAST_LATITUDE, it) }
                lastLongitude?.let { putDouble(KEY_LAST_LONGITUDE, it) }
            }.build()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<PushSyncRetryWorker>()
                .setInputData(input)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
