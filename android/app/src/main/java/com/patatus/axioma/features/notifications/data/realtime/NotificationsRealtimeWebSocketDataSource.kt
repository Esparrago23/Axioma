package com.patatus.axioma.features.notifications.data.realtime

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.patatus.axioma.core.config.AppConfig
import com.patatus.axioma.core.di.WebSocketOkHttp
import com.patatus.axioma.features.notifications.domain.entities.NotificationEntity
import com.patatus.axioma.features.notifications.domain.entities.NotificationRealTimeEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRealtimeWebSocketDataSource @Inject constructor(
    @WebSocketOkHttp private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val events = MutableSharedFlow<NotificationRealTimeEvent>(extraBufferCapacity = 64)
    private val isConnecting = AtomicBoolean(false)

    @Volatile private var webSocket: WebSocket? = null
    @Volatile private var reconnectAttempt = 0

    fun observeEvents(): Flow<NotificationRealTimeEvent> {
        return events.asSharedFlow().onStart { connectIfNeeded() }
    }

    private fun connectIfNeeded() {
        if (webSocket != null || !isConnecting.compareAndSet(false, true)) return

        val request = Request.Builder()
            .url(AppConfig.network.notificationsWebSocketUrl) // añade esta url en AppConfig
            .build()

        webSocket = okHttpClient.newWebSocket(request, NotificationsWebSocketListener())
    }

    private fun scheduleReconnect() {
        if (isConnecting.get()) return
        scope.launch {
            reconnectAttempt += 1
            val delayMs = (2_000L * reconnectAttempt.coerceAtMost(5)).coerceAtMost(30_000L)
            delay(delayMs)
            connectIfNeeded()
        }
    }

    private inner class NotificationsWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            reconnectAttempt = 0
            isConnecting.set(false)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val event = parseEvent(text) ?: return
            scope.launch { events.emit(event) }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(code, reason)
            this@NotificationsRealtimeWebSocketDataSource.webSocket = null
            isConnecting.set(false)
            scheduleReconnect()
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            this@NotificationsRealtimeWebSocketDataSource.webSocket = null
            isConnecting.set(false)
            scheduleReconnect()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            this@NotificationsRealtimeWebSocketDataSource.webSocket = null
            isConnecting.set(false)
            scheduleReconnect()
        }
    }

    private fun parseEvent(rawMessage: String): NotificationRealTimeEvent? {
        return try {
            val jsonObject = JsonParser.parseString(rawMessage).asJsonObject
            val eventType = jsonObject.get("event")?.asString ?: return null
            val payload = jsonObject.getAsJsonObject("payload") ?: return null

            when (eventType) {
                "NEW_NOTIFICATION" -> {
                    gson.fromJson(payload, NotificationPayload::class.java)
                        ?.toDomain()
                        ?.let(NotificationRealTimeEvent::NewNotification)
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private data class NotificationPayload(
        val id: Int,
        val title: String,
        val body: String,
        val type: String,
        @SerializedName("reference_id") val referenceId: Int?,
        @SerializedName("created_at") val createdAt: String,
        @SerializedName("is_read") val isRead: Boolean
    ) {
        fun toDomain() = NotificationEntity(
            id = id,
            title = title,
            body = body,
            type = type,
            referenceId = referenceId,
            createdAt = createdAt,
            isRead = isRead
        )
    }
}