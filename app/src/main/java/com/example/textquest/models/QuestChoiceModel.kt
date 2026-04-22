package com.example.textquest.models

data class QuestChoiceModel(
    val targetSceneId: String,
    val buttonLabel: String,
    val isLocked: Boolean,
    val requiredKarma: Int
)