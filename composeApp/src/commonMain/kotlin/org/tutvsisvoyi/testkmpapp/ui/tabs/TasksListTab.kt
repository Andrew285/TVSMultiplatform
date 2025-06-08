package org.tutvsisvoyi.testkmpapp.ui.tabs

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import androidx.compose.runtime.remember
import org.jetbrains.compose.resources.painterResource
import tvsmultiplatform.composeapp.generated.resources.Res
import tvsmultiplatform.composeapp.generated.resources.settings

object TasksListTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Home"
            val icon = painterResource(Res.drawable.settings)
//            val selectedIcon = rememberVectorPainter(Icons.Filled.Home)

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
//        Navigator(HomeScreen())
    }
}