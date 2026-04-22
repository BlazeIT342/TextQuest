package com.example.textquest.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlayerProgressDao {
    @Query("SELECT * FROM player_progress WHERE campaignId = :id")
    suspend fun getProgress(id: String): PlayerProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: PlayerProgressEntity)

    @Query("DELETE FROM player_progress WHERE campaignId = :id")
    suspend fun clearProgress(id: String)
}