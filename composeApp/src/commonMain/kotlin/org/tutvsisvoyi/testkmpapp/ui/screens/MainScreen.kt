package org.tutvsisvoyi.testkmpapp.ui.screens

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions

class MainScreen : Screen {
    @Composable
    override fun Content() {
        TabNavigator(TimerTab) {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        TabNavigationItem(TimerTab)
                        TabNavigationItem(ProjectsTab)
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
        icon = { Icon(tab.options.icon!!, contentDescription = tab.options.title) },
        label = { Text(tab.options.title) }
    )
}

// Tabs
object TimerTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 0u,
            title = "Timer",
            icon = null
        )

    @Composable
    override fun Content() {
//        TimerScreen().Content()
    }
}

object ProjectsTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 1u,
            title = "Projects",
            icon = null
        )

    @Composable
    override fun Content() {
//        ProjectsScreen().Content()
    }
}

object ProfileTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 2u,
            title = "Profile",
            icon = null
        )

    @Composable
    override fun Content() {
//        ProfileScreen().Content()
    }
}