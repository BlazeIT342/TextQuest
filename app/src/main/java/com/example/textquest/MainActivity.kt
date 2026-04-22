package com.example.textquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.textquest.data.MockRepository
import com.example.textquest.models.QuestCampaignModel
import com.example.textquest.models.QuestSceneModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreenWithNavigation()
            }
        }
    }
}

@Composable
fun MainScreenWithNavigation(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }
    val campaigns by viewModel.campaignsList.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = "Квести") },
                    label = { Text("Квести") },
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        navController.navigate("list") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Профіль") },
                    label = { Text("Профіль") },
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        navController.navigate("profile") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "list",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("list") {
                CampaignListScreen(
                    campaigns = campaigns,
                    onCampaignClick = { campaignId ->
                        navController.navigate("detail/$campaignId")
                    },
                    onSyncClick = {
                        viewModel.refreshFromServer()
                    }
                )
            }
            composable("profile") {
                ProfileScreen()
            }
            composable("detail/{campaignId}") { backStackEntry ->
                val campaignId = backStackEntry.arguments?.getString("campaignId")
                val campaign = campaigns.find { it.campaignId == campaignId }
                if (campaign != null) {
                    CampaignDetailScreen(
                        campaign = campaign,
                        onStartGame = { navController.navigate("gameplay/${campaign.campaignId}") },
                        onBack = { navController.popBackStack() },
                        onDelete = {
                            viewModel.deleteCampaign(campaign.campaignId)
                            navController.popBackStack()
                        }
                    )
                }
            }
            composable("gameplay/{campaignId}") { backStackEntry ->
                val campaignId = backStackEntry.arguments?.getString("campaignId") ?: ""
                val initialScene = MockRepository.getStartingScene(campaignId)
                GameplayScreen(
                    scene = initialScene,
                    onExit = { navController.popBackStack("list", inclusive = false) }
                )
            }
        }
    }
}

@Composable
fun CampaignListScreen(
    campaigns: List<QuestCampaignModel>,
    onCampaignClick: (String) -> Unit,
    onSyncClick: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Доступні квести", style = MaterialTheme.typography.headlineMedium)
                Button(onClick = onSyncClick) {
                    Text("Синхронізувати")
                }
            }
        }

        if (campaigns.isEmpty()) {
            item {
                Text("Список порожній. Натисніть 'Синхронізувати', щоб завантажити дані.", color = Color.Gray)
            }
        }

        items(campaigns) { campaign ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable { onCampaignClick(campaign.campaignId) },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = campaign.title, style = MaterialTheme.typography.titleLarge)
                    Text(text = "Складність: ${campaign.difficultyLevel}", color = Color.Gray)
                    if (campaign.isCompleted) {
                        Text(text = "Пройдено", color = Color.Green)
                    }
                }
            }
        }
    }
}

@Composable
fun CampaignDetailScreen(
    campaign: QuestCampaignModel,
    onStartGame: () -> Unit,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack) {
                Text("<- Назад")
            }
            OutlinedButton(onClick = onDelete) {
                Text("Видалити квест", color = Color.Red)
            }
        }
        Text(text = campaign.title, style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Рівень складності: ${campaign.difficultyLevel}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Статус: ${if (campaign.isCompleted) "Завершено" else "Не пройдено"}", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onStartGame,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Почати гру")
        }
    }
}

@Composable
fun GameplayScreen(scene: QuestSceneModel, onExit: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = scene.descriptionText, style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.weight(1f))

        scene.availableChoices.forEach { choice ->
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text(choice.buttonLabel)
            }
        }
        OutlinedButton(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Здатися та вийти", color = Color.Red)
        }
    }
}

@Composable
fun ProfileScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Екран профілю гравця", style = MaterialTheme.typography.headlineMedium)
    }
}