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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

sealed class WebSocketState {
    object Disconnected : WebSocketState()
    object Connecting : WebSocketState()
    object Connected : WebSocketState()
    data class Error(val cause: Throwable) : WebSocketState()
}

@Singleton
class NotificationsRealtimeWebSocketDataSource @Inject constructor(
    @WebSocketOkHttp private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {
    private val scopeJob: Job = SupervisorJob()
    private val scope = CoroutineScope(scopeJob + Dispatchers.IO)
    private val events = MutableSharedFlow<NotificationRealTimeEvent>(extraBufferCapacity = 64)
    private val isConnecting = AtomicBoolean(false)

    private val _state = MutableStateFlow<WebSocketState>(WebSocketState.Disconnected)
    val state: StateFlow<WebSocketState> = _state.asStateFlow()

    @Volatile private var webSocket: WebSocket? = null
    @Volatile private var reconnectAttempt = 0

    fun observeEvents(): Flow<NotificationRealTimeEvent> {
        android.util.Log.d("NotificationWS", "observeEvents() llamado")
        return events.asSharedFlow().onStart { connectIfNeeded() }
    }

    private fun connectIfNeeded() {
        if (webSocket != null || !isConnecting.compareAndSet(false, true)) {
            android.util.Log.d("NotificationWS", "connectIfNeeded() omitido — ya conectado o conectando")
            return
        }

        val url = AppConfig.network.notificationsWebSocketUrl
        android.util.Log.d("NotificationWS", "Conectando a: $url")

        _state.value = WebSocketState.Connecting

        val request = Request.Builder()
            .url(url)
            .build()

        webSocket = okHttpClient.newWebSocket(request, NotificationsWebSocketListener())
    }

    fun disconnect() {
        webSocket?.close(1000, "Manual disconnect")
        webSocket = null
        scopeJob.cancelChildren()
        _state.value = WebSocketState.Disconnected
    }

    private fun scheduleReconnect() {
        if (isConnecting.get()) return
        scope.launch {
            reconnectAttempt += 1
            val delayMs = (2_000L * reconnectAttempt.coerceAtMost(5)).coerceAtMost(30_000L)
            android.util.Log.d("NotificationWS", "Reconectando en ${delayMs}ms (intento $reconnectAttempt)")
            delay(delayMs)
            connectIfNeeded()
        }
    }

    private inner class NotificationsWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            android.util.Log.d("NotificationWS", "WebSocket conectado")
            reconnectAttempt = 0
            isConnecting.set(false)
            _state.value = WebSocketState.Connected
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            android.util.Log.d("NotificationWS", "Mensaje recibido: $text")
            val event = parseEvent(text)
            android.util.Log.d("NotificationWS", "Evento parseado: $event")
            if (event != null) {
                scope.launch { events.emit(event) }
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            android.util.Log.w("NotificationWS", "WebSocket cerrando: code=$code reason=$reason")
            webSocket.close(code, reason)
            this@NotificationsRealtimeWebSocketDataSource.webSocket = null
            isConnecting.set(false)
            scheduleReconnect()
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            android.util.Log.w("NotificationWS", "WebSocket cerrado: code=$code reason=$reason")
            this@NotificationsRealtimeWebSocketDataSource.webSocket = null
            isConnecting.set(false)
            _state.value = WebSocketState.Disconnected
            scheduleReconnect()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            android.util.Log.e("NotificationWS", "WebSocket error: ${t.message}", t)
            this@NotificationsRealtimeWebSocketDataSource.webSocket = null
            isConnecting.set(false)
            _state.value = WebSocketState.Error(t)
            scheduleReconnect()
        }
    }

    private fun parseEvent(rawMessage: String): NotificationRealTimeEvent? {
        return try {
            val jsonObject = JsonParser.parseString(rawMessage).asJsonObject
            val eventType = jsonObject.get("event")?.asString ?: return null
            val payload = jsonObject.getAsJsonObject("payload") ?: return null

            android.util.Log.d("NotificationWS", "Tipo de evento: $eventType")

            when (eventType) {
                "NEW_NOTIFICATION" -> {
                    gson.fromJson(payload, NotificationPayload::class.java)
                        ?.toDomain()
                        ?.let(NotificationRealTimeEvent::NewNotification)
                }
                else -> {
                    android.util.Log.w("NotificationWS", "Tipo de evento desconocido: $eventType")
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationWS", "Error parseando evento: ${e.message}", e)
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