package com.example.textquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.textquest.data.MockRepository
import com.example.textquest.data.socket.SocketState
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
    val socketStatus by viewModel.socketStatus.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.List, null) },
                    label = { Text("Квести") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; navController.navigate("list") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Star, null) },
                    label = { Text("Лідери") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; navController.navigate("leaderboard") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, null) },
                    label = { Text("Опції") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2; navController.navigate("settings") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(navController, "list", Modifier.padding(innerPadding)) {
            composable("list") { CampaignListScreen(campaigns, { navController.navigate("detail/$it") }, { viewModel.refreshFromServer() }) }
            composable("leaderboard") { LeaderboardScreen() }
            composable("settings") { SettingsScreen(socketStatus) }
            composable("detail/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")
                campaigns.find { it.campaignId == id }?.let {
                    CampaignDetailScreen(it, { navController.navigate("gameplay/${it.campaignId}") }, { navController.popBackStack() }, { viewModel.deleteCampaign(it.campaignId); navController.popBackStack() })
                }
            }
            composable("gameplay/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                GameplayScreen(MockRepository.getStartingScene(id), { navController.popBackStack("list", false) })
            }
        }
    }
}

@Composable
fun CampaignListScreen(campaigns: List<QuestCampaignModel>, onCampaignClick: (String) -> Unit, onSyncClick: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Квести", style = MaterialTheme.typography.headlineMedium)
                Button(onClick = onSyncClick) { Text("Sync") }
            }
        }
        items(campaigns) { campaign ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onCampaignClick(campaign.campaignId) }) {
                Column(Modifier.padding(16.dp)) {
                    Text(campaign.title, style = MaterialTheme.typography.titleLarge)
                    Text("Складність: ${campaign.difficultyLevel}", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun LeaderboardScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Таблиця лідерів", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))
        listOf("Ultimate_Slayer - 5000 XP", "ShadowWalker - 4950 XP", "Adventurer - 1800 XP").forEach {
            Text(it, Modifier.padding(8.dp), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun SettingsScreen(socketStatus: SocketState, viewModel: MainViewModel = viewModel()) {
    val notification by viewModel.serverNotifications.collectAsState()
    var isSoundEnabled by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Налаштування", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(16.dp)) {
                Text("Статус сервера: ${socketStatus.name}",
                    color = if (socketStatus == SocketState.Connected) Color(0xFF4CAF50) else Color.Red,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Text("Жива стрічка подій:", style = MaterialTheme.typography.labelLarge)
                Text(notification, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Звукові ефекти")
            Spacer(Modifier.weight(1f))
            Switch(
                checked = isSoundEnabled,
                onCheckedChange = { isSoundEnabled = it }
            )
        }
    }
}

@Composable
fun CampaignDetailScreen(campaign: QuestCampaignModel, onStart: () -> Unit, onBack: () -> Unit, onDelete: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Button(onClick = onBack) { Text("Назад") }
            Button(onClick = onDelete, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Видалити") }
        }
        Text(campaign.title, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.weight(1f))
        Button(onClick = onStart, Modifier.fillMaxWidth().height(50.dp)) { Text("Почати") }
    }
}

@Composable
fun GameplayScreen(scene: QuestSceneModel, onExit: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(scene.descriptionText)
        Spacer(Modifier.weight(1f))
        OutlinedButton(onClick = onExit, Modifier.fillMaxWidth()) { Text("Вийти", color = Color.Red) }
    }
}