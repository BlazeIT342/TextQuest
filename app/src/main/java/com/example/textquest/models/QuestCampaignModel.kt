package com.example.textquest.models

data class QuestCampaignModel(
    val campaignId: String,
    val title: String,
    val difficultyLevel: Int,
    val isCompleted: Boolean,
    val releaseDateTimestamp: Long
)