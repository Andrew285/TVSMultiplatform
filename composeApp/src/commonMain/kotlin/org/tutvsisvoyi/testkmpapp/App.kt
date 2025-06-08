package org.tutvsisvoyi.testkmpapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import com.example.compose.darkScheme
import com.example.compose.lightScheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.core.KoinApplication
import org.tutvsisvoyi.testkmpapp.di.appModules
import org.tutvsisvoyi.testkmpapp.domain.repository.AuthRepository
import org.tutvsisvoyi.testkmpapp.ui.screens.MainScreen
import org.tutvsisvoyi.testkmpapp.ui.screens.login.LoginScreen

import tvsmultiplatform.composeapp.generated.resources.Res
import tvsmultiplatform.composeapp.generated.resources.compose_multiplatform

//@Composable
//@Preview
//fun App() {
//    val colors = if (isSystemInDarkTheme()) darkScheme else lightScheme
//    MaterialTheme(colorScheme = colors) {
//        var showContent by remember { mutableStateOf(false) }
//        Column(
//            modifier = Modifier
//                .safeContentPadding()
//                .fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//        ) {
//            Button(onClick = { showContent = !showContent }) {
//                Text("Click me!")
//            }
//            AnimatedVisibility(showContent) {
//                val greeting = remember { Greeting().greet() }
//                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
//                    Image(painterResource(Res.drawable.compose_multiplatform), null)
//                    Text("Compose: $greeting")
//                }
//            }
//        }
//    }
//}

@Composable
fun App() {
    KoinApplication(
        application = {
            modules(appModules)
        }
    )
    {
        MaterialTheme {
            var isLoggedIn by remember { mutableStateOf(false) }
            val authRepository: AuthRepository = org.koin.compose.koinInject()
            LaunchedEffect(Unit) {
                // Check if user is already logged in
                isLoggedIn = authRepository.isLoggedIn()
            }

            Navigator(
                screen = if (isLoggedIn) MainScreen() else LoginScreen()
            )
        }
    }
}
