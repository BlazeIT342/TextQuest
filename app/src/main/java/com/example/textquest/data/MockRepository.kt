package com.example.textquest.data

import com.example.textquest.models.QuestCampaignModel
import com.example.textquest.models.QuestChoiceModel
import com.example.textquest.models.QuestSceneModel

object MockRepository {
    val campaigns = listOf(
        QuestCampaignModel("c1", "Втеча з підземелля", 3, false, 1672531200000L),
        QuestCampaignModel("c2", "Таємниця старого лісу", 1, true, 1675209600000L),
        QuestCampaignModel("c3", "Кібер-шпигун", 5, false, 1677628800000L)
    )

    fun getStartingScene(campaignId: String): QuestSceneModel {
        return QuestSceneModel(
            sceneId = "scene_1",
            descriptionText = "Ви прокидаєтесь у темній кімнаті. Холодна кам'яна підлога нагадує про те, що ви у в'язниці. Що будете робити?",
            isTerminalScene = false,
            sceneMultiplier = 1.0f,
            availableChoices = listOf(
                QuestChoiceModel("scene_2", "Оглянути стіни", false, 0),
                QuestChoiceModel("scene_3", "Спробувати вибити двері", false, 0)
            )
        )
    }
}