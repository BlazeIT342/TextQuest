package com.example.textquest.data

import com.example.textquest.data.local.CampaignDao
import com.example.textquest.data.local.CampaignEntity
import com.example.textquest.data.remote.QuestApiService
import kotlinx.coroutines.flow.Flow

class QuestRepository(
    private val campaignDao: CampaignDao,
    private val apiService: QuestApiService
) {
    fun observeAllCampaigns(): Flow<List<CampaignEntity>> {
        return campaignDao.observeAllCampaigns()
    }

    suspend fun getCampaignById(id: String): CampaignEntity? {
        return campaignDao.getCampaignById(id)
    }

    suspend fun deleteCampaign(id: String) {
        campaignDao.deleteCampaignById(id)
    }

    suspend fun syncCampaigns() {
        try {
            val remoteData = apiService.fetchCampaignsFromServer()
            campaignDao.insertCampaigns(remoteData)
        } catch (exception: Exception) {

        }
    }
}