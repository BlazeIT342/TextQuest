package com.example.textquest.data.socket

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*

class SocketManager(private val client: OkHttpClient) {
    private var webSocket: WebSocket? = null
    private val _state = MutableStateFlow(SocketState.Disconnected)
    val state: StateFlow<SocketState> = _state

    private val _messages = MutableStateFlow<String?>(null)
    val messages: StateFlow<String?> = _messages

    private val managerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null

    fun connect(url: String) {
        if (_state.value == SocketState.Connected || _state.value == SocketState.Connecting) return

        _state.value = SocketState.Connecting
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _state.value = SocketState.Connected
                startHeartbeat()
                reconnectJob?.cancel()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                _messages.value = text
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                handleFailure(url)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                stopHeartbeat()
                _state.value = SocketState.Disconnected
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                stopHeartbeat()
                _state.value = SocketState.Disconnected
            }
        })
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = managerScope.launch {
            while (isActive) {
                delay(8000)
                if (_state.value == SocketState.Connected) {
                    webSocket?.send("heartbeat_ping")
                }
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    private fun handleFailure(url: String) {
        stopHeartbeat()
        _state.value = SocketState.Disconnected

        reconnectJob?.cancel()
        reconnectJob = managerScope.launch {
            delay(5000)
            connect(url)
        }
    }

    fun disconnect() {
        stopHeartbeat()
        reconnectJob?.cancel()
        webSocket?.close(1000, "User initiated disconnect")
        _state.value = SocketState.Disconnected
    }
}