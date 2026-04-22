package com.example.textquest.models

data class QuestSceneModel(
    val sceneId: String,
    val descriptionText: String,
    val isTerminalScene: Boolean,
    val sceneMultiplier: Float,
    val availableChoices: List<QuestChoiceModel>
)