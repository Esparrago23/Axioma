package com.patatus.axioma.core.hardware.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.patatus.axioma.MainActivity
import com.patatus.axioma.R // Asegúrate de tener un icono adecuado
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AxiomaFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var pushNotificationManager: PushNotificationManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

        // TODO: (Para tu Feature) Aquí deberías llamar a tu Caso de Uso o Manager para guardar

        showNotification(title = title, body = body)
    }


    @SuppressLint("MissingPermission")
    private fun showNotification(title: String, body: String) {
        createChannelIfNeeded()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder) // <-- Temporal genérico
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notification)
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
    }
}