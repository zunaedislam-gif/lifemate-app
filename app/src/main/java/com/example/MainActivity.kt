package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AiChatDialog
import com.example.ui.components.QuickActionModal
import com.example.ui.screens.*
import com.example.ui.theme.LifeMateTheme
import com.example.ui.util.L10n
import com.example.ui.viewmodel.LifeMateViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LifeMateViewModel = viewModel()
            val userProfile by viewModel.userProfile.collectAsState()

            val isSystemDark = isSystemInDarkTheme()
            val isDark = userProfile.isDarkMode ?: isSystemDark

            LifeMateTheme(darkTheme = isDark) {
                LifeMateApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LifeMateApp(viewModel: LifeMateViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val lang = userProfile.language

    // Bottom navigation current tab: 0: Home, 1: Tasks, 2: Money, 3: Documents, 4: More
    var selectedTab by remember { mutableStateOf(0) }

    // Dialog states
    var showAiChat by remember { mutableStateOf(false) }
    var activeQuickAction by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = L10n.t("home", lang)
                        )
                    },
                    label = { Text(L10n.t("home", lang)) },
                    modifier = Modifier.testTag("nav_item_home")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.TaskAlt else Icons.Outlined.TaskAlt,
                            contentDescription = L10n.t("tasks", lang)
                        )
                    },
                    label = { Text(L10n.t("tasks", lang)) },
                    modifier = Modifier.testTag("nav_item_tasks")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                            contentDescription = L10n.t("money", lang)
                        )
                    },
                    label = { Text(L10n.t("money", lang)) },
                    modifier = Modifier.testTag("nav_item_money")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Filled.FolderShared else Icons.Outlined.FolderShared,
                            contentDescription = L10n.t("documents", lang)
                        )
                    },
                    label = { Text(L10n.t("documents", lang)) },
                    modifier = Modifier.testTag("nav_item_documents")
                )

                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 4) Icons.Filled.GridView else Icons.Outlined.GridView,
                            contentDescription = L10n.t("more", lang)
                        )
                    },
                    label = { Text(L10n.t("more", lang)) },
                    modifier = Modifier.testTag("nav_item_more")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToTasks = { selectedTab = 1 },
                    onNavigateToMoney = { selectedTab = 2 },
                    onNavigateToDocs = { selectedTab = 3 },
                    onNavigateToMore = { selectedTab = 4 },
                    onOpenAiChat = { showAiChat = true },
                    onOpenQuickAction = { action -> activeQuickAction = action }
                )
                1 -> TasksScreen(viewModel = viewModel)
                2 -> MoneyScreen(viewModel = viewModel)
                3 -> DocumentsScreen(viewModel = viewModel)
                4 -> MoreScreen(viewModel = viewModel)
            }
        }
    }

    // AI Assistant Dialog
    if (showAiChat) {
        AiChatDialog(
            viewModel = viewModel,
            onDismiss = { showAiChat = false }
        )
    }

    // Quick Action Dialog / Modal
    if (activeQuickAction != null) {
        QuickActionModal(
            actionType = activeQuickAction,
            viewModel = viewModel,
            onDismiss = { activeQuickAction = null }
        )
    }
}
