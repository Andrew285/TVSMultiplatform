package org.tutvsisvoyi.testkmpapp.ui.screens.login

data class LoginState(
    val email: String = "",
    val password: String = "",
    val apiToken: String = "",
    val loginMethod: LoginMethod = LoginMethod.EMAIL_PASSWORD,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)