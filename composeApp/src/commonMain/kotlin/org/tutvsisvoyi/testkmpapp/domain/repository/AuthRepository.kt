package org.tutvsisvoyi.testkmpapp.domain.repository

import org.tutvsisvoyi.testkmpapp.domain.model.AuthResponse
import org.tutvsisvoyi.testkmpapp.domain.model.User

interface AuthRepository {
    suspend fun loginWithToken(apiToken: String): Result<User>
    suspend fun loginWithCredentials(email: String, password: String): Result<AuthResponse>
    suspend fun logout()
    suspend fun getCurrentUser(): User?
    suspend fun isLoggedIn(): Boolean
    suspend fun getApiToken(): String?
}