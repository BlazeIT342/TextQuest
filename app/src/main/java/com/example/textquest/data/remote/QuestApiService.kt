package com.example.textquest.data.remote

import com.example.textquest.data.local.CampaignEntity
import kotlinx.coroutines.delay

class QuestApiService {
    suspend fun fetchCampaignsFromServer(): List<CampaignEntity> {
        try {
            delay(1500)
            return listOf(
                CampaignEntity(
                    "c1",
                    "Втеча з підземелля",
                    "Ви прокидаєтесь у кайданах. Ваша мета — знайти вихід із лабіринту тортур.",
                    3,
                    false,
                    1672531200000L,
                    "synced"
                ),
                CampaignEntity(
                    "c2",
                    "Таємниця старого лісу",
                    "Легенди кажуть, що дерева тут розмовляють. Ви вирушаєте на пошуки зниклого поселення.",
                    1,
                    true,
                    1675209600000L,
                    "synced"
                ),
                CampaignEntity(
                    "c3",
                    "Кібер-шпигун",
                    "Неон, дощ та корпоративні змови. Зламайте головний сервер Megacorp.",
                    5,
                    false,
                    1677628800000L,
                    "synced"
                ),
                CampaignEntity(
                    "c4",
                    "Нова загроза (З сервера!)",
                    "Це секретне завдання, отримане через захищений канал зв'язку.",
                    4,
                    false,
                    1680000000000L,
                    "synced"
                )
            )
        } catch (e: Exception) {
            return emptyList()
        }
    }
}