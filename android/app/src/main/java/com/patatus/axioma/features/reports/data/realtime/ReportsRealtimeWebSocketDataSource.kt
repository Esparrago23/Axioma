package com.patatus.axioma.features.reports.data.realtime

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.patatus.axioma.core.config.AppConfig
import com.patatus.axioma.core.di.WebSocketOkHttp
import com.patatus.axioma.features.reports.domain.entities.Report
import com.patatus.axioma.features.reports.domain.entities.ReportRealtimeEvent
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
class ReportsRealtimeWebSocketDataSource @Inject constructor(
    @WebSocketOkHttp private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {

    private val scopeJob: Job = SupervisorJob()
    private val scope = CoroutineScope(scopeJob + Dispatchers.IO)
    private val events = MutableSharedFlow<ReportRealtimeEvent>(extraBufferCapacity = 64)
    private val isConnecting = AtomicBoolean(false)

    private val _state = MutableStateFlow<WebSocketState>(WebSocketState.Disconnected)
    val state: StateFlow<WebSocketState> = _state.asStateFlow()

    @Volatile
    private var webSocket: WebSocket? = null

    @Volatile
    private var reconnectAttempt = 0

    fun observeEvents(): Flow<ReportRealtimeEvent> {
        return events.asSharedFlow().onStart {
            connectIfNeeded()
        }
    }

    private fun connectIfNeeded() {
        if (webSocket != null || !isConnecting.compareAndSet(false, true)) {
            return
        }

        _state.value = WebSocketState.Connecting

        val request = Request.Builder()
            .url(AppConfig.network.reportsWebSocketUrl)
            .build()

        webSocket = okHttpClient.newWebSocket(request, ReportsWebSocketListener())
    }

    fun disconnect() {
        webSocket?.close(1000, "Manual disconnect")
        webSocket = null
        scopeJob.cancelChildren()
        _state.value = WebSocketState.Disconnected
    }

    private fun scheduleReconnect() {
        if (isConnecting.get()) {
            return
        }

        scope.launch {
            reconnectAttempt += 1
            val delayMs = (2_000L * reconnectAttempt.coerceAtMost(5)).coerceAtMost(30_000L)
            delay(delayMs)
            connectIfNeeded()
        }
    }

    private inner class ReportsWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            reconnectAttempt = 0
            isConnecting.set(false)
            _state.value = WebSocketState.Connected
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val event = parseEvent(text) ?: return
            scope.launch {
                events.emit(event)
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(code, reason)
            this@ReportsRealtimeWebSocketDataSource.webSocket = null
            isConnecting.set(false)
            scheduleReconnect()
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            this@ReportsRealtimeWebSocketDataSource.webSocket = null
            isConnecting.set(false)
            _state.value = WebSocketState.Disconnected
            scheduleReconnect()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            this@ReportsRealtimeWebSocketDataSource.webSocket = null
            isConnecting.set(false)
            _state.value = WebSocketState.Error(t)
            scheduleReconnect()
        }
    }

    private fun parseEvent(rawMessage: String): ReportRealtimeEvent? {
        val jsonObject = JsonParser.parseString(rawMessage).asJsonObject
        val eventType = jsonObject.get("event")?.asString ?: return null
        val payload = jsonObject.getAsJsonObject("payload") ?: return null

        return when (eventType) {
            "NEW_REPORT" -> payload.getAsJsonObject("report")
                ?.let { gson.fromJson(it, RealtimeReportPayload::class.java) }
                ?.toDomain()
                ?.let(ReportRealtimeEvent::NewReport)

            "VOTE_UPDATE" -> gson.fromJson(payload, VoteUpdatePayload::class.java)?.toDomain()
            else -> null
        }
    }

    private data class RealtimeReportPayload(
        val id: Int,
        val title: String,
        val description: String,
        val category: String,
        val latitude: Double,
        val longitude: Double,
        @SerializedName("photo_url") val photoUrl: String?,
        @SerializedName("credibility_score") val credibilityScore: Int,
        val status: String,
        @SerializedName("created_at") val createdAt: String,
        @SerializedName("user_id") val userId: Int
    ) {
        fun toDomain(): Report {
            return Report(
                id = id,
                title = title,
                description = description,
                category = category,
                latitude = latitude,
                longitude = longitude,
                photoUrl = photoUrl,
                credibilityScore = credibilityScore,
                status = status,
                createdAt = createdAt,
                authorId = userId,
                userVote = 0
            )
        }
    }

    private data class VoteUpdatePayload(
        @SerializedName("report_id") val reportId: Int,
        @SerializedName("credibility_score") val credibilityScore: Int,
        val status: String
    ) {
        fun toDomain(): ReportRealtimeEvent.VoteUpdate {
            return ReportRealtimeEvent.VoteUpdate(
                reportId = reportId,
                credibilityScore = credibilityScore,
                status = status
            )
        }
    }
}