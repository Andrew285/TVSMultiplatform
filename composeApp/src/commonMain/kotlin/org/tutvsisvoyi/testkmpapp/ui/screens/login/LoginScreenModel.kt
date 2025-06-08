package org.tutvsisvoyi.testkmpapp.ui.screens.login

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.tutvsisvoyi.testkmpapp.domain.usecase.LoginWithCredentialsUseCase
import org.tutvsisvoyi.testkmpapp.domain.usecase.LoginWithTokenUseCase

class LoginScreenModel(
    private val loginWithCredentialsUseCase: LoginWithCredentialsUseCase,
    private val loginWithTokenUseCase: LoginWithTokenUseCase
) : ScreenModel {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email, error = null)
    }

    fun updatePassword(password: String) {
        _state.value = _state.value.copy(password = password, error = null)
    }

    fun updateApiToken(token: String) {
        _state.value = _state.value.copy(apiToken = token, error = null)
    }

    fun switchLoginMethod(method: LoginMethod) {
        _state.value = _state.value.copy(loginMethod = method, error = null)
    }

    fun login() {
        screenModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val result = when (_state.value.loginMethod) {
                LoginMethod.EMAIL_PASSWORD -> {
                    loginWithCredentialsUseCase(_state.value.email, _state.value.password)
                        .map { it.user }
                }
                LoginMethod.API_TOKEN -> {
                    loginWithTokenUseCase(_state.value.apiToken)
                }
            }

            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Login failed"
                    )
                }
            )
        }
    }
}