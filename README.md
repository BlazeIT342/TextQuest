# Лабораторна робота №1: Архітектура мобільного застосунку та навігація
## Мета роботи
Створити робочу основу свого мобільного застосунку: структуру навігації та моделі даних.
## Архітектура та моделі даних
Для текстового квесту було створено 3 ключові сутності. Кожна модель відповідає вимогам: має необхідну кількість полів різних типів (String, Int, Boolean, Long, Float, List) та зберігається як `data class`.
### 1. Модель `QuestCampaignModel`
Представляє загальну сюжетну кампанію квесту.
* `campaignId: String` - унікальний ідентифікатор кампанії.
* `title: String` - назва квесту (наприклад, "Втеча з підземелля").
* `difficultyLevel: Int` - рівень складності проходження.
* `isCompleted: Boolean` - статус проходження кампанії.
* `releaseDateTimestamp: Long` — дата випуску у форматі timestamp.

### 2. Модель `QuestSceneModel`
Описує конкретний етап всередині квесту.
* `sceneId: String` - ідентифікатор сцени.
* `descriptionText: String` - основний текст, який описує поточну ситуацію для гравця.
* `isTerminalScene: Boolean`- чи є ця сцена фінальною.
* `sceneMultiplier: Float` - множник очок для цієї сцени.
* `availableChoices: List<QuestChoiceModel>` - список доступних варіантів вибору для гравця.

### 3. Модель `QuestChoiceModel`
Описує варіант дії гравця на конкретній сцені.
* `targetSceneId: String` - ідентифікатор сцени, на яку веде цей вибір.
* `buttonLabel: String` - текст на кнопці дії.
* `isLocked: Boolean` - чи заблокований цей вибір.
* `requiredKarma: Int` - кількість карми, необхідна для розблокування вибору.

## Екрани та Навігація
У застосунку реалізовано 4 екрани. Навігація керується через `NavHost` та `NavigationBar` (нижня панель).
### Список екранів:
1. **`CampaignListScreen` (Екран списку):** Відображає перелік доступних квестів у вигляді карток через `LazyColumn`. Дані беруться з `MockRepository.campaigns` (Рис.1.).
<img width="822" height="1657" alt="image" src="https://github.com/user-attachments/assets/b7e2f7d9-3e46-40d5-8009-11d2868c1786" /><br>
2. **`CampaignDetailScreen` (Екран деталей):** Показує детальну інформацію про обраний квест та має кнопку "Почати гру" (Рис.2.). 
<img width="889" height="1709" alt="image" src="https://github.com/user-attachments/assets/f806f422-fe1e-4f28-ba55-7a7ea788ff12" /><br>
3. **`GameplayScreen` (Екран геймплею):** Ігровий екран, де виводиться текст сцени (`QuestSceneModel`) та генеруються кнопки вибору дій (`QuestChoiceModel`) (Рис.3.).
<img width="859" height="1716" alt="image" src="https://github.com/user-attachments/assets/e94180df-7fbe-4c41-83ee-f40bd0357513" /><br>
4. **`ProfileScreen` (Додатковий екран):** Екран профілю користувача, доступний через нижню панель навігації (Рис.4.).
<img width="889" height="1715" alt="image" src="https://github.com/user-attachments/assets/dae1d80b-d735-4b39-80d6-8bae0b01c41b" /><br>




