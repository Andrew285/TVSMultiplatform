package org.tutvsisvoyi.testkmpapp.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import org.tutvsisvoyi.testkmpapp.ui.screens.MainScreen

class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val screenModel = koinScreenModel<LoginScreenModel>()
        val state by screenModel.state.collectAsState()

        LaunchedEffect(state.isSuccess) {
            if (state.isSuccess) {
                navigator?.replaceAll(MainScreen())
            }
        }

        LoginContent(
            state = state,
            onTokenChange = screenModel::updateApiToken,
            onLogin = screenModel::login,
            onEmailChange = screenModel::updateEmail,
            onPasswordChange = screenModel::updatePassword,
            onMethodSwitch = screenModel::switchLoginMethod
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginContent(
    state: LoginState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTokenChange: (String) -> Unit,
    onMethodSwitch: (LoginMethod) -> Unit,
    onLogin: () -> Unit
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isTokenVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Toggl Time Tracker") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to Toggl",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Login method selector
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Login Method",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            onClick = { onMethodSwitch(LoginMethod.EMAIL_PASSWORD) },
                            label = { Text("Email & Password") },
                            selected = state.loginMethod == LoginMethod.EMAIL_PASSWORD,
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        FilterChip(
                            onClick = { onMethodSwitch(LoginMethod.API_TOKEN) },
                            label = { Text("API Token") },
                            selected = state.loginMethod == LoginMethod.API_TOKEN
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login fields based on selected method
            when (state.loginMethod) {
                LoginMethod.EMAIL_PASSWORD -> {
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = onEmailChange,
                        label = { Text("Email") },
                        placeholder = { Text("Enter your Toggl email") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        leadingIcon = {
//                            Icon(Icons.Default.Email, contentDescription = null)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = onPasswordChange,
                        label = { Text("Password") },
                        placeholder = { Text("Enter your password") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        visualTransformation = if (isPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
//                                Icon(
//                                    imageVector = if (isPasswordVisible) {
//                                        Icons.Default.VisibilityOff
//                                    } else {
//                                        Icons.Default.Visibility
//                                    },
//                                    contentDescription = if (isPasswordVisible) {
//                                        "Hide password"
//                                    } else {
//                                        "Show password"
//                                    }
//                                )
                            }
                        },
                        leadingIcon = {
//                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true
                    )
                }

                LoginMethod.API_TOKEN -> {
                    OutlinedTextField(
                        value = state.apiToken,
                        onValueChange = onTokenChange,
                        label = { Text("API Token") },
                        placeholder = { Text("Enter your Toggl API token") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        visualTransformation = if (isTokenVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
//                                Icon(
//                                    imageVector = if (isTokenVisible) {
//                                        Icons.Default.VisibilityOff
//                                    } else {
//                                        Icons.Default.Visibility
//                                    },
//                                    contentDescription = if (isTokenVisible) {
//                                        "Hide token"
//                                    } else {
//                                        "Show token"
//                                    }
//                                )
                            }
                        },
                        leadingIcon = {
//                            Icon(Icons.Default.Key, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            state.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Button(
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading && when (state.loginMethod) {
                    LoginMethod.EMAIL_PASSWORD -> state.email.isNotBlank() && state.password.isNotBlank()
                    LoginMethod.API_TOKEN -> state.apiToken.isNotBlank()
                }
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Login")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Help card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = when (state.loginMethod) {
                            LoginMethod.EMAIL_PASSWORD -> "Email & Password Login"
                            LoginMethod.API_TOKEN -> "How to get your API token:"
                        },
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (state.loginMethod) {
                            LoginMethod.EMAIL_PASSWORD -> "Use your regular Toggl account credentials to login."
                            LoginMethod.API_TOKEN -> "1. Go to toggl.com/app/profile\n2. Scroll down to API Token\n3. Copy the token and paste it here"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}