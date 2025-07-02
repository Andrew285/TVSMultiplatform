package org.tutvsisvoyi.testkmpapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import org.koin.compose.KoinApplication
import org.tutvsisvoyi.testkmpapp.di.appModules
import org.tutvsisvoyi.testkmpapp.domain.repository.AuthRepository
import org.tutvsisvoyi.testkmpapp.ui.screens.MainScreen
import org.tutvsisvoyi.testkmpapp.ui.screens.login.LoginScreen

@Composable
fun App() {
    KoinApplication(
        application = {
            modules(appModules)
        }
    )
    {
        MaterialTheme {
            var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }
            val authRepository: AuthRepository = org.koin.compose.koinInject()
            LaunchedEffect(Unit) {
                // Check if user is already logged in
                isLoggedIn = authRepository.isLoggedIn()
            }

            println("isLoggedIn: = $isLoggedIn")
            when (isLoggedIn) {
                null -> {
                    // Loading state - you can show a splash screen here
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                true -> Navigator(screen = MainScreen())
                false -> Navigator(screen = LoginScreen())
            }
        }
    }
}
