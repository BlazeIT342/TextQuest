package com.example.textquest.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "campaigns")
data class CampaignEntity(
    @PrimaryKey val campaignId: String,
    val title: String,
    val description: String,
    val difficultyLevel: Int,
    val isCompleted: Boolean,
    val releaseDateTimestamp: Long,
    val syncStatus: String
)