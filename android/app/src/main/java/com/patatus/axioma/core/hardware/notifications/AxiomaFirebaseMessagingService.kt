package com.patatus.axioma.core.hardware.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.patatus.axioma.MainActivity
import com.patatus.axioma.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AxiomaFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var pushNotificationManager: PushNotificationManager

    @Inject
    lateinit var incomingPushNotificationHandler: IncomingPushNotificationHandler

    private val serviceScopeJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceScopeJob + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.i("AxiomaFCM", "FCM token actual: $token")
            }
            .addOnFailureListener { error ->
                Log.e("AxiomaFCM", "No se pudo obtener el FCM token", error)
            }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i("AxiomaFCM", "FCM token refrescado: $token")

        serviceScope.launch {
            pushNotificationManager.syncTokenAndCurrentLocation(explicitToken = token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Axioma"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "Tienes una nueva notificación ciudadana."

        val type = remoteMessage.data["type"] ?: "incident_alert"
        val referenceId = remoteMessage.data["reference_id"]?.toIntOrNull()
        val notificationId = remoteMessage.data["notification_id"]?.toIntOrNull()
        val createdAt = remoteMessage.data["created_at"]

        serviceScope.launch {
            incomingPushNotificationHandler.handleIncomingMessage(
                id = notificationId,
                title = title,
                body = body,
                type = type,
                referenceId = referenceId,
                createdAt = createdAt
            )
        }

        showNotification(title = title, body = body, referenceId = referenceId)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(title: String, body: String, referenceId: Int? = null) {
        createChannelIfNeeded()

        val viewIntent = if (referenceId != null) {
            Intent(Intent.ACTION_VIEW, Uri.parse("axioma://report/$referenceId")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                setPackage(packageName)
            }
        } else {
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            1001,
            viewIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setGroup(NOTIFICATION_GROUP)
            .addAction(R.drawable.ic_launcher_foreground, "Ver", pendingIntent)
            .build()

        val notifManager = NotificationManagerCompat.from(this)
        notifManager.notify(System.currentTimeMillis().toInt(), notification)

        val summaryNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setGroup(NOTIFICATION_GROUP)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()

        notifManager.notify(SUMMARY_NOTIFICATION_ID, summaryNotification)
    }

    private fun createChannelIfNeeded() {

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Axioma Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alertas sobre reportes ciudadanos cercanos"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "axioma_alerts"
        private const val NOTIFICATION_GROUP = "axioma_reports_group"
        private const val SUMMARY_NOTIFICATION_ID = 0
    }
}