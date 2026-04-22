package com.example.textquest.models

data class QuestChoiceModel(
    val choiceId: String,
    val buttonLabel: String,
    val targetSceneId: String,
    val healthChange: Int = 0,
    val resultMessage: String = "",
    val isHidden: Boolean = false,
    val requiredScore: Int = 0
)