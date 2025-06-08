package org.tutvsisvoyi.testkmpapp.domain.usecase

import org.tutvsisvoyi.testkmpapp.domain.model.User
import org.tutvsisvoyi.testkmpapp.domain.repository.AuthRepository

class LoginWithTokenUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(apiToken: String): Result<User> {
        return authRepository.loginWithToken(apiToken)
    }
}