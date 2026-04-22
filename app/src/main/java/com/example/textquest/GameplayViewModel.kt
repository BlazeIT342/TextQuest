package com.example.textquest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.textquest.data.MockRepository
import com.example.textquest.data.local.AppDatabase
import com.example.textquest.data.local.PlayerProgressEntity
import com.example.textquest.models.QuestChoiceModel
import com.example.textquest.models.QuestSceneModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameplayViewModel(application: Application) : AndroidViewModel(application) {
    private val databaseInstance = AppDatabase.getDatabase(application)
    private val progressDao = databaseInstance.progressDao()

    private val _currentActiveScene = MutableStateFlow<QuestSceneModel?>(null)
    val currentActiveScene: StateFlow<QuestSceneModel?> = _currentActiveScene

    private val _playerHealthPoints = MutableStateFlow(100)
    val playerHealthPoints: StateFlow<Int> = _playerHealthPoints

    private val _lastActionFeedback = MutableStateFlow<String?>(null)
    val lastActionFeedback: StateFlow<String?> = _lastActionFeedback

    private var currentCampaignContextId: String = ""

    fun initializeSession(campaignId: String) {
        currentCampaignContextId = campaignId
        viewModelScope.launch {
            val persistentProgress = progressDao.getProgress(campaignId)
            if (persistentProgress != null) {
                _playerHealthPoints.value = persistentProgress.healthPoints
                _currentActiveScene.value = MockRepository.getSceneById(persistentProgress.currentSceneId)
            } else {
                executeHardReset()
            }
        }
    }

    fun processPlayerChoice(selectedChoice: QuestChoiceModel) {
        viewModelScope.launch {
            _lastActionFeedback.value = selectedChoice.resultMessage

            val calculatedHealth = (_playerHealthPoints.value + selectedChoice.healthChange).coerceIn(0, 100)
            _playerHealthPoints.value = calculatedHealth

            if (calculatedHealth <= 0) {
                _currentActiveScene.value = MockRepository.getSceneById("global_death")
            } else {
                _currentActiveScene.value = MockRepository.getSceneById(selectedChoice.targetSceneId)
            }

            synchronizeProgressWithDatabase()
        }
    }

    private suspend fun synchronizeProgressWithDatabase() {
        val targetSceneId = _currentActiveScene.value?.sceneId ?: return
        val progressSnapshot = PlayerProgressEntity(
            campaignId = currentCampaignContextId,
            currentSceneId = targetSceneId,
            healthPoints = _playerHealthPoints.value
        )
        progressDao.saveProgress(progressSnapshot)
    }

    fun executeHardReset() {
        viewModelScope.launch {
            _lastActionFeedback.value = null
            progressDao.clearProgress(currentCampaignContextId)
            _playerHealthPoints.value = 100
            _currentActiveScene.value = MockRepository.getStartingScene(currentCampaignContextId)
        }
    }
}