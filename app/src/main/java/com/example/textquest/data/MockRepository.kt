package com.example.textquest.data

import com.example.textquest.models.QuestCampaignModel
import com.example.textquest.models.QuestChoiceModel
import com.example.textquest.models.QuestSceneModel

object MockRepository {

    private val allScenes = mapOf(
        "c1_start" to QuestSceneModel(
            sceneId = "c1_start",
            descriptionText = "Сирена реве. Ви у житловому секторі бункера. Двері в основний шлюз заблоковано, але є хід через технічний тунель та склад.",
            availableChoices = listOf(
                QuestChoiceModel("c1_tunnel", "Лізти в тунель", "c1_tunnel_crawl", -5, "Ви обпеклися об гарячу трубу, але пролізли."),
                QuestChoiceModel("c1_warehouse", "Бігти через склад", "c1_warehouse_fight", -10, "Дрон помітив вас і встиг вистрілити один раз."),
                QuestChoiceModel("c1_hack", "Зламати термінал", "c1_tunnel_crawl", -15, "Термінал вдарив вас струмом, але двері прочинилися.")
            )
        ),
        "c1_tunnel_crawl" to QuestSceneModel(
            sceneId = "c1_tunnel_crawl",
            descriptionText = "Ви у вузькій шахті. Попереду вентиляція або щитова, де можна вимкнути світло.",
            availableChoices = listOf(
                QuestChoiceModel("c1_vent", "Вентиляція", "c1_win", 0, "Ви тихо вибралися назовні."),
                QuestChoiceModel("c1_power", "Вимкнути щиток", "c1_win", 5, "Ви знайшли енергетик біля щитка і відновили сили!")
            )
        ),
        "c1_warehouse_fight" to QuestSceneModel(
            sceneId = "c1_warehouse_fight",
            descriptionText = "На складі забагато охорони. Потрібно діяти швидко.",
            availableChoices = listOf(
                QuestChoiceModel("c1_explode", "Підірвати бочки", "c1_win", -20, "Вибухова хвиля сильно контузила вас, але шлях вільний."),
                QuestChoiceModel("c1_stealth_run", "Пробігти стіною", "c1_win", -5, "Кілька куль зачепили одяг, ви майже не постраждали.")
            )
        ),
        "c1_win" to QuestSceneModel("c1_win", "Ви вибралися на поверхню. Перемога!", true),

        "c2_start" to QuestSceneModel(
            sceneId = "c2_start",
            descriptionText = "Старий ліс шепоче. Дорога розходиться: до болота, через скелі або в хащу.",
            availableChoices = listOf(
                QuestChoiceModel("c2_swamp", "Через болото", "c2_swamp_sink", -10, "Трясовина витягує сили."),
                QuestChoiceModel("c2_rocks", "Через скелі", "c2_win", -15, "Ви впали з невеликого виступу."),
                QuestChoiceModel("c2_deep", "В хащу", "c2_witch_hut", 0, "Ви йдете тихо і спокійно.")
            )
        ),
        "c2_swamp_sink" to QuestSceneModel(
            sceneId = "c2_swamp_sink",
            descriptionText = "Ви по пояс у воді. Попереду берег.",
            availableChoices = listOf(
                QuestChoiceModel("c2_branch", "Гілка", "c2_win", 0, "Ви витягли себе на сушу."),
                QuestChoiceModel("c2_panic", "Паніка", "global_death", -100, "Ви занадто активно борсалися і пішли на дно.")
            )
        ),
        "c2_witch_hut" to QuestSceneModel(
            sceneId = "c2_witch_hut",
            descriptionText = "Хатина відьми. Вона пропонує чай або відпочинок.",
            availableChoices = listOf(
                QuestChoiceModel("c2_drink", "Випити чай", "c2_win", 40, "Чай виявився цілющим!"),
                QuestChoiceModel("c2_rest", "Відпочити", "c2_win", 20, "Ви трохи відновилися.")
            )
        ),
        "c2_win" to QuestSceneModel("c2_win", "Ви пройшли ліс і бачите вогні міста.", true),

        "c3_start" to QuestSceneModel(
            sceneId = "c3_start",
            descriptionText = "Взлом Megacorp. Можна використати фальшивий ID або зламати термінал.",
            availableChoices = listOf(
                QuestChoiceModel("c3_fake_id", "Показати ID", "c3_server_room", -5, "Вас змусили пройти через сканер, він трохи опромінив."),
                QuestChoiceModel("c3_hack", "Зламати термінал", "c3_security_alert", -20, "Захист виявився міцнішим, ви отримали зворотний сигнал.")
            )
        ),
        "c3_security_alert" to QuestSceneModel(
            sceneId = "c3_security_alert",
            descriptionText = "Тривога! Газ заповнює кімнату.",
            availableChoices = listOf(
                QuestChoiceModel("c3_breath", "Затримати дихання", "c3_server_room", -10, "Ви встигли вискочити, але трохи наковталися газу."),
                QuestChoiceModel("c3_surrender", "Здатися", "global_death", -100, "Вас ліквідували на місці.")
            )
        ),
        "c3_server_room" to QuestSceneModel(
            sceneId = "c3_server_room",
            descriptionText = "Ви у серверній. Фінальний злам.",
            availableChoices = listOf(
                QuestChoiceModel("c3_fast", "Швидкий злам", "c3_win", 0, "Дані вкрадені без проблем."),
                QuestChoiceModel("c3_full", "Повне копіювання", "c3_win", -10, "Вас запеленгували, довелось відбиватися.")
            )
        ),
        "c3_win" to QuestSceneModel("c3_win", "Дані у вас. Це успіх.", true),

        "c4_start" to QuestSceneModel(
            sceneId = "c4_start",
            descriptionText = "Ви на позиції. Можна атакувати зараз або замінувати дорогу.",
            availableChoices = listOf(
                QuestChoiceModel("c4_attack", "Атакувати", "c4_heavy_fire", -30, "Зав'язалася перестрілка."),
                QuestChoiceModel("c4_mines", "Міни", "c4_win", 0, "Вибух зробив усю роботу за вас.")
            )
        ),
        "c4_heavy_fire" to QuestSceneModel(
            sceneId = "c4_heavy_fire",
            descriptionText = "Вас затисли. Потрібно рішення.",
            availableChoices = listOf(
                QuestChoiceModel("c4_airstrike", "Авіаудар", "c4_win", -10, "Осколки трохи зачепили вас."),
                QuestChoiceModel("c4_ram", "Таран", "global_death", -100, "Це було самогубство.")
            )
        ),
        "c4_win" to QuestSceneModel("c4_win", "Місію завершено.", true),

        "global_death" to QuestSceneModel(
            sceneId = "global_death",
            descriptionText = "ГРУ ЗАКІНЧЕНО.",
            isTerminalScene = true
        )
    )

    val campaigns = listOf(
        QuestCampaignModel("c1", "Втеча з бункеру", "Проберіться крізь охорону секретного об'єкту.", 4, false, 1672531200000L),
        QuestCampaignModel("c2", "Таємниця старого лісу", "Виживіть у містичному лісі.", 2, false, 1675209600000L),
        QuestCampaignModel("c3", "Кібер-шпигун", "Злам Megacorp.", 5, false, 1677628800000L),
        QuestCampaignModel("c4", "Нова загроза", "Усуньте ціль у зоні бойових дій.", 4, false, 1680000000000L)
    )

    fun getStartingScene(campaignId: String): QuestSceneModel {
        val startKey = "${campaignId}_start"
        return allScenes[startKey] ?: allScenes["c1_start"]!!
    }

    fun getSceneById(sceneId: String): QuestSceneModel {
        return allScenes[sceneId] ?: allScenes["global_death"]!!
    }
}