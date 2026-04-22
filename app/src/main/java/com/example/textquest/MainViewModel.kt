package com.example.textquest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.textquest.data.QuestRepository
import com.example.textquest.data.local.AppDatabase
import com.example.textquest.data.remote.QuestApiService
import com.example.textquest.data.socket.SocketManager
import com.example.textquest.data.socket.SocketState
import com.example.textquest.models.QuestCampaignModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: QuestRepository
    private val socketManager: SocketManager
    private val client = OkHttpClient()

    val socketStatus: StateFlow<SocketState>
    private val _serverNotifications = MutableStateFlow<String>("Очікування новин...")
    val serverNotifications: StateFlow<String> = _serverNotifications

    init {
        val database = AppDatabase.getDatabase(application)
        repository = QuestRepository(database.campaignDao(), QuestApiService())
        socketManager = SocketManager(client)
        socketStatus = socketManager.state

        viewModelScope.launch {
            socketManager.messages.collect { msg ->
                if (msg != null) _serverNotifications.value = msg
            }
        }

        viewModelScope.launch {
            val news = listOf(
                "Гравців онлайн: 124",
                "Новий квест 'Тінь минулого' вже доступний!",
                "Гравець Mark щойно пройшов Dungeon Escape!",
                "Сервер буде оновлено через 10 хвилин"
            )
            var index = 0
            while(true) {
                delay(5000)
                if (socketStatus.value == SocketState.Connected) {
                    _serverNotifications.value = news[index % news.size]
                    index++
                }
            }
        }

        connectToWebSocket()
    }

    val campaignsList: StateFlow<List<QuestCampaignModel>> = repository.observeAllCampaigns()
        .map { entities ->
            entities.map { entity ->
                QuestCampaignModel(entity.campaignId, entity.title, entity.difficultyLevel, entity.isCompleted, entity.releaseDateTimestamp)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun refreshFromServer() = viewModelScope.launch { repository.syncCampaigns() }
    fun deleteCampaign(id: String) = viewModelScope.launch { repository.deleteCampaign(id) }

    fun connectToWebSocket() {
        socketManager.connect("wss://ws.postman-echo.com/raw")
    }

    override fun onCleared() {
        super.onCleared()
        socketManager.disconnect()
    }
}