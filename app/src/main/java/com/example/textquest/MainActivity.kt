package com.example.textquest

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.textquest.data.security.BiometricStatus
import com.example.textquest.data.socket.SocketState
import com.example.textquest.models.QuestCampaignModel

fun Context.findActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val vm: MainViewModel = viewModel()
                val bioEnabled by vm.isBiometricEnabled.collectAsState()
                val sensorInfo = vm.getSensorInfo()
                val isSensorReady = sensorInfo == "Sensor Ready"

                var isAppContentLocked by remember { mutableStateOf(bioEnabled && isSensorReady) }

                if (isAppContentLocked) {
                    LockScreen(vm) { isAppContentLocked = false }
                } else {
                    MainScreenWithNavigation(vm)
                }
            }
        }
    }
}

@Composable
fun LockScreen(vm: MainViewModel, onUnlocked: () -> Unit) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val status by vm.biometricStatus.collectAsState()

    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Icon(Icons.Filled.Fingerprint, null, Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text("Додаток заблоковано", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = {
            activity?.let { vm.authenticate(it, "Авторизація") }
                ?: Toast.makeText(context, "Activity error", Toast.LENGTH_SHORT).show()
        }) {
            Text("Увійти через біометрію")
        }
        TextButton(onClick = { vm.toggleBiometric(false); onUnlocked() }) {
            Text("Аварійний вхід", color = Color.Red)
        }
    }

    LaunchedEffect(status) {
        if (status == BiometricStatus.Success) {
            onUnlocked()
            vm.resetBioStatus()
        }
    }
}

@Composable
fun MainScreenWithNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    var selectedTabItem by remember { mutableIntStateOf(0) }
    val campaigns by viewModel.campaignsList.collectAsState()
    val socketStatus by viewModel.socketStatus.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(icon = { Icon(Icons.Filled.List, null) }, label = { Text("Квести") }, selected = selectedTabItem == 0, onClick = { selectedTabItem = 0; navController.navigate("list") })
                NavigationBarItem(icon = { Icon(Icons.Filled.Star, null) }, label = { Text("Лідери") }, selected = selectedTabItem == 1, onClick = { selectedTabItem = 1; navController.navigate("leaderboard") })
                NavigationBarItem(icon = { Icon(Icons.Filled.Security, null) }, label = { Text("Безпека") }, selected = selectedTabItem == 2, onClick = { selectedTabItem = 2; navController.navigate("security") })
            }
        }
    ) { innerPadding ->
        NavHost(navController, "list", Modifier.padding(innerPadding)) {
            composable("list") { CampaignListScreen(campaigns, { navController.navigate("detail/$it") }, { viewModel.refreshFromServer() }) }
            composable("leaderboard") { LeaderboardScreen() }
            composable("security") { SecurityCombinedScreen(viewModel, socketStatus) }
            composable("detail/{id}") { entry ->
                val id = entry.arguments?.getString("id")
                campaigns.find { it.campaignId == id }?.let { campaign ->
                    CampaignDetailScreen(campaign, { navController.navigate("gameplay/$id") }, { navController.popBackStack() }, { navController.navigate("confirm/$id") })
                }
            }
            composable("confirm/{id}") { entry ->
                ConfirmDeleteScreen(viewModel, entry.arguments?.getString("id") ?: "") {
                    navController.popBackStack("list", false)
                }
            }
            composable("gameplay/{id}") { entry ->
                val id = entry.arguments?.getString("id") ?: ""
                val gameplayVm: GameplayViewModel = viewModel()
                LaunchedEffect(id) { gameplayVm.initializeSession(id) }
                GameplayScreen(gameplayVm) { navController.popBackStack("list", false) }
            }
        }
    }
}

@Composable
fun GameplayScreen(viewModel: GameplayViewModel, onExitRequest: () -> Unit) {
    val scene by viewModel.currentActiveScene.collectAsState()
    val hp by viewModel.playerHealthPoints.collectAsState()
    val feedback by viewModel.lastActionFeedback.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        LinearProgressIndicator(
            progress = { hp / 100f },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = if (hp < 30) Color.Red else Color.Green
        )
        Text("Здоров'я: $hp%", style = MaterialTheme.typography.labelSmall)

        Spacer(Modifier.height(24.dp))

        feedback?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        scene?.let { current ->
            Text(current.descriptionText, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.weight(1f))

            if (!current.isTerminalScene && hp > 0) {
                current.availableChoices.forEach { choice ->
                    Button(onClick = { viewModel.processPlayerChoice(choice) }, Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Text(choice.buttonLabel)
                    }
                }
            } else {
                Button(onClick = { viewModel.executeHardReset() }, Modifier.fillMaxWidth()) {
                    Text(if (hp <= 0) "Спробувати знову" else "Повернутись у початок")
                }
            }
        }

        OutlinedButton(onClick = onExitRequest, Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Вийти в меню")
        }
    }
}

@Composable
fun SecurityCombinedScreen(vm: MainViewModel, socketStatus: SocketState) {
    val news by vm.serverNotifications.collectAsState()
    val bioEnabled by vm.isBiometricEnabled.collectAsState()
    val sensor = vm.getSensorInfo()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Центр керування", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(16.dp)) {
                Text("Сервер: ${socketStatus.name}", color = if (socketStatus == SocketState.Connected) Color(0xFF4CAF50) else Color.Red)
                Text("Новини: $news", style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(20.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Біометрія", style = MaterialTheme.typography.titleMedium)
                Text("Статус: $sensor", style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Використовувати відбиток")
                    Spacer(Modifier.weight(1f))
                    Switch(checked = bioEnabled, onCheckedChange = { vm.toggleBiometric(it) })
                }
            }
        }
    }
}

@Composable
fun ConfirmDeleteScreen(vm: MainViewModel, id: String, onDeleted: () -> Unit) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val status by vm.biometricStatus.collectAsState()

    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("Видалення квесту", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Button(onClick = { activity?.let { vm.authenticate(it, "Видалення даних") } }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
            Text("Підтвердити біометрією")
        }
    }

    LaunchedEffect(status) {
        if (status == BiometricStatus.Success) {
            vm.deleteCampaign(id)
            vm.resetBioStatus()
            onDeleted()
        }
    }
}

@Composable
fun CampaignListScreen(campaigns: List<QuestCampaignModel>, onCampaignClick: (String) -> Unit, onSync: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Ваші пригоди", style = MaterialTheme.typography.headlineMedium)
                IconButton(onClick = onSync) { Icon(Icons.Filled.Refresh, null) }
            }
        }
        items(campaigns) { campaign ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onCampaignClick(campaign.campaignId) }) {
                Column(Modifier.padding(16.dp)) {
                    Text(campaign.title, style = MaterialTheme.typography.titleLarge)
                    Text("Рівень: ${campaign.difficultyLevel}", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun LeaderboardScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Рейтинг", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))
        listOf("ShadowWalker - 9500 XP", "QuestMaster - 8200 XP", "DragonSlayer - 7400 XP", "MysticMage - 6100 XP").forEach {
            Text(it, Modifier.padding(8.dp), style = MaterialTheme.typography.bodyLarge)
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
        Spacer(Modifier.height(20.dp))
        Text(campaign.title, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(10.dp))
        Text(campaign.description, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.weight(1f))
        Button(onClick = onStart, Modifier.fillMaxWidth().height(56.dp)) { Text("Почати квест") }
    }
}