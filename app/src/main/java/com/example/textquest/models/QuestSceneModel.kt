package com.example.textquest.models

data class QuestSceneModel(
    val sceneId: String,
    val descriptionText: String,
    val isTerminalScene: Boolean = false,
    val availableChoices: List<QuestChoiceModel> = emptyList()
)