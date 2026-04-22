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

    private var retryCount = 0
    private val maxRetries = 1

    fun connect(url: String) {
        if (_state.value == SocketState.Connected) return

        _state.value = SocketState.Connecting
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _state.value = SocketState.Connected
                retryCount = 0
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                _messages.value = text
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                handleFailure()
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _state.value = SocketState.Disconnected
            }
        })
    }

    private fun handleFailure() {
        if (retryCount < maxRetries) {
            retryCount++
            _state.value = SocketState.Reconnecting
            CoroutineScope(Dispatchers.IO).launch {
                delay(3000)
                connect("ws://mock.url")
            }
        } else {
            _state.value = SocketState.Disconnected
        }
    }

    fun send(message: String) {
        webSocket?.send(message)
    }

    fun disconnect() {
        webSocket?.close(1000, "Normal closure")
        _state.value = SocketState.Disconnected
    }
}