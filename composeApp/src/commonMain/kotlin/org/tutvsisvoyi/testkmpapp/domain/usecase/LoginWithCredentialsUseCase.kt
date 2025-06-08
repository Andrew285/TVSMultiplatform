package org.tutvsisvoyi.testkmpapp.domain.usecase

import org.tutvsisvoyi.testkmpapp.domain.model.AuthResponse
import org.tutvsisvoyi.testkmpapp.domain.repository.AuthRepository

class LoginWithCredentialsUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<AuthResponse> {
        return authRepository.loginWithCredentials(email, password)
    }
}