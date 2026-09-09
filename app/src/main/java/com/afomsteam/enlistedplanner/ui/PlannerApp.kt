package com.afomsteam.enlistedplanner.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.afomsteam.enlistedplanner.data.PlannerViewModel

private enum class MainTab(val label: String) { HOME("Home"), CAREER("Career"), DEVELOP("Develop"), LEAD("Lead"), TOOLS("Tools") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerApp(vm: PlannerViewModel = viewModel()) {
    if (!vm.state.settings.disclosureAccepted) {
        DisclosureScreen(onAccept = vm::setDisclosureAccepted)
        return
    }

    var tabName by rememberSaveable { mutableStateOf(MainTab.HOME.name) }
    val tab = MainTab.valueOf(tabName)
    var detail by rememberSaveable { mutableStateOf<String?>(null) }
    var showCapture by remember { mutableStateOf(false) }

    if (detail != null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(detailTitle(detail!!)) },
                    navigationIcon = { IconButton(onClick = { detail = null }) { Icon(Icons.Default.ArrowBack, "Back") } }
                )
            }
        ) { padding ->
            DetailRouter(detail!!, vm, onBack = { detail = null }, onOpen = { detail = it }, modifier = Modifier.padding(padding))
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val p = vm.state.profile
                    ColumnTitle(
                        if (p.name.isBlank()) "Enlisted Planner" else p.name,
                        if (p.name.isBlank()) "Your enlisted brain" else "${p.grade.label} • ${p.component.label}"
                    )
                },
                actions = {
                    IconButton(onClick = { detail = "search" }) { Icon(Icons.Default.Search, "Search") }
                    IconButton(onClick = { detail = "profile" }) { Icon(Icons.Default.Settings, "Profile and settings") }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tabName = item.name },
                        icon = {
                            Icon(
                                when (item) {
                                    MainTab.HOME -> Icons.Default.Home
                                    MainTab.CAREER -> Icons.Default.Star
                                    MainTab.DEVELOP -> Icons.Default.TrendingUp
                                    MainTab.LEAD -> Icons.Default.Groups
                                    MainTab.TOOLS -> Icons.Default.Build
                                }, item.label
                            )
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCapture = true }) { Icon(Icons.Default.Add, "Capture") }
        }
    ) { padding ->
        when (tab) {
            MainTab.HOME -> HomeScreen(vm, Modifier.padding(padding), onOpen = { detail = it }, onCapture = { showCapture = true })
            MainTab.CAREER -> CareerScreen(vm, Modifier.padding(padding), onOpen = { detail = it })
            MainTab.DEVELOP -> DevelopScreen(vm, Modifier.padding(padding), onOpen = { detail = it })
            MainTab.LEAD -> LeadershipScreen(vm, Modifier.padding(padding), onOpen = { detail = it })
            MainTab.TOOLS -> ToolsScreen(vm, Modifier.padding(padding), onOpen = { detail = it })
        }
    }

    if (showCapture) QuickCaptureDialog(vm = vm, onDismiss = { showCapture = false })
}

@Composable
private fun ColumnTitle(title: String, subtitle: String) {
    androidx.compose.foundation.layout.Column {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextMuted)
    }
}

private fun detailTitle(route: String): String = when {
    route == "search" -> "Search"
    route == "profile" -> "Profile & Settings"
    route == "launchpad" -> "Airman Launchpad"
    route.startsWith("member:") -> "Airman"
    route.startsWith("tool:") -> route.removePrefix("tool:")
    else -> route
}
