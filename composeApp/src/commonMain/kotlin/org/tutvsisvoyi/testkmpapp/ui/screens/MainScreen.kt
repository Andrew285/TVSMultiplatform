package org.tutvsisvoyi.testkmpapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.PersonStanding
import org.koin.compose.koinInject
import org.tutvsisvoyi.testkmpapp.ui.screens.login.LoginScreen
import org.tutvsisvoyi.testkmpapp.ui.screens.profile.ProfileScreen
import org.tutvsisvoyi.testkmpapp.ui.screens.profile.ProfileScreenModel
import org.tutvsisvoyi.testkmpapp.ui.screens.time_entries.TimeEntriesScreen
import org.tutvsisvoyi.testkmpapp.ui.screens.time_entries.TimeEntriesScreenModel

class MainScreen : Screen {
    @Composable
    override fun Content() {
        TabNavigator(TimerTab) {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        TabNavigationItem(TimerTab)
                        TabNavigationItem(ReportsTab)
                        TabNavigationItem(ProfileTab)
                    }
                }
            ) { paddingValues ->
                CurrentTab()
            }
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current

    NavigationBarItem(
        selected = tabNavigator.current == tab,
        onClick = { tabNavigator.current = tab },
        icon = {
            tab.options.icon?.let { icon ->
                Icon(painter = icon, contentDescription = tab.options.title)
            }
        },
        label = { Text(tab.options.title) }
    )
}

object TimerTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 0u,
            title = "Timer",
            icon = rememberVectorPainter(Lucide.House)
        )

    @Composable
    override fun Content() {
        val screenModel: TimeEntriesScreenModel = koinInject()
        val state by screenModel.state.collectAsState()

        TimeEntriesScreen(
            state = state,
            onAction = screenModel::handleAction
        )
    }
}

object ReportsTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 1u,
            title = "Reports",
            icon = rememberVectorPainter(Lucide.Check)
        )

    @Composable
    override fun Content() {
        ReportsContentForTab()
    }
}

object ProfileTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 2u,
            title = "Profile",
            icon = rememberVectorPainter(Lucide.PersonStanding)
        )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel: ProfileScreenModel = koinInject()
        val state by screenModel.state.collectAsState()

        LaunchedEffect(state.isLoggingOut) {
            if (state.isLoggingOut) {
                navigator.replaceAll(LoginScreen())
            }
        }

        ProfileScreen(
            state = state,
            onAction = screenModel::handleAction,
            onLogOut = {
                // Navigate to login from the main navigator context
                navigator.replaceAll(LoginScreen())
            }
        )
    }
}

@Composable
fun ReportsContentForTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            "Reports - Coming Soon",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )
    }
}