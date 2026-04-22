package com.example.textquest

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.textquest.data.QuestRepository
import com.example.textquest.data.local.AppDatabase
import com.example.textquest.data.local.SecurityPreferences
import com.example.textquest.data.remote.QuestApiService
import com.example.textquest.data.security.BiometricStatus
import com.example.textquest.data.security.InternalBiometricManager
import com.example.textquest.data.socket.SocketManager
import com.example.textquest.data.socket.SocketState
import com.example.textquest.models.QuestCampaignModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: QuestRepository
    private val socketManager = SocketManager(OkHttpClient())
    private val bioManager = InternalBiometricManager(application)
    private val securityPrefs = SecurityPreferences(application)

    val socketStatus: StateFlow<SocketState> = socketManager.state
    val biometricStatus = bioManager.status
    val isBiometricEnabled = MutableStateFlow(securityPrefs.isBiometricEnabled())

    private val _serverNotifications = MutableStateFlow<String>("З'єднання...")
    val serverNotifications: StateFlow<String> = _serverNotifications

    init {
        val database = AppDatabase.getDatabase(application)
        repository = QuestRepository(database.campaignDao(), QuestApiService())

        viewModelScope.launch {
            socketManager.messages.collect { msg ->
                if (msg != null && !msg.contains("heartbeat")) {
                    _serverNotifications.value = msg
                }
            }
        }

        viewModelScope.launch {
            val newsItems = listOf(
                "Гравців онлайн: 142",
                "Новий квест доступний!",
                "Сервер працює стабільно",
                "ShadowWalker пройшов бункер!"
            )
            var index = 0
            while(true) {
                delay(5000)
                if (socketStatus.value == SocketState.Connected) {
                    _serverNotifications.value = newsItems[index % newsItems.size]
                    index++
                } else {
                    _serverNotifications.value = "Перепідключення до сервера..."
                }
            }
        }

        socketManager.connect("wss://ws.postman-echo.com/raw")
    }

    val campaignsList: StateFlow<List<QuestCampaignModel>> = repository.observeAllCampaigns()
        .map { entities ->
            entities.map { entity ->
                QuestCampaignModel(
                    entity.campaignId,
                    entity.title,
                    entity.description,
                    entity.difficultyLevel,
                    entity.isCompleted,
                    entity.releaseDateTimestamp
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun toggleBiometric(enabled: Boolean) {
        securityPrefs.setBiometricEnabled(enabled)
        isBiometricEnabled.value = enabled
    }

    fun authenticate(activity: FragmentActivity, reason: String) = bioManager.authenticate(activity, reason)
    fun getSensorInfo() = bioManager.checkAvailability()
    fun resetBioStatus() = bioManager.reset()
    fun refreshFromServer() = viewModelScope.launch { repository.syncCampaigns() }
    fun deleteCampaign(id: String) = viewModelScope.launch { repository.deleteCampaign(id) }
}