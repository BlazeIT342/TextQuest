package com.example.textquest.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_progress")
data class PlayerProgressEntity(
    @PrimaryKey val campaignId: String,
    val currentSceneId: String,
    val healthPoints: Int
)