package com.example.textquest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.textquest.data.QuestRepository
import com.example.textquest.data.local.AppDatabase
import com.example.textquest.data.remote.QuestApiService
import com.example.textquest.models.QuestCampaignModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: QuestRepository

    init {
        val database = AppDatabase.getDatabase(application)
        val apiService = QuestApiService()
        repository = QuestRepository(database.campaignDao(), apiService)
    }

    val campaignsList: StateFlow<List<QuestCampaignModel>> = repository.observeAllCampaigns()
        .map { entities ->
            entities.map { entity ->
                QuestCampaignModel(
                    campaignId = entity.campaignId,
                    title = entity.title,
                    difficultyLevel = entity.difficultyLevel,
                    isCompleted = entity.isCompleted,
                    releaseDateTimestamp = entity.releaseDateTimestamp
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun refreshFromServer() {
        viewModelScope.launch {
            repository.syncCampaigns()
        }
    }

    fun deleteCampaign(id: String) {
        viewModelScope.launch {
            repository.deleteCampaign(id)
        }
    }
}