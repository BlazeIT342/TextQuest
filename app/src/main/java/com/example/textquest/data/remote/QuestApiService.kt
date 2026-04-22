package com.example.textquest.data.remote

import com.example.textquest.data.local.CampaignEntity
import kotlinx.coroutines.delay

class QuestApiService {
    suspend fun fetchCampaignsFromServer(): List<CampaignEntity> {
        delay(1500)
        return listOf(
            CampaignEntity("c1", "Втеча з підземелля", 3, false, 1672531200000L, "synced"),
            CampaignEntity("c2", "Таємниця старого лісу", 1, true, 1675209600000L, "synced"),
            CampaignEntity("c3", "Кібер-шпигун", 5, false, 1677628800000L, "synced"),
            CampaignEntity("c4", "Нова загроза (З сервера!)", 4, false, 1680000000000L, "synced")
        )
    }
}