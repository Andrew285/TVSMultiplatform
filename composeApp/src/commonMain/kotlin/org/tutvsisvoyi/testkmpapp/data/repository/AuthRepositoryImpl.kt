package org.tutvsisvoyi.testkmpapp.data.repository

import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import org.tutvsisvoyi.testkmpapp.data.database.realm.RealmDatabase
import org.tutvsisvoyi.testkmpapp.data.network.TogglApiClient
import org.tutvsisvoyi.testkmpapp.domain.model.AuthResponse
import org.tutvsisvoyi.testkmpapp.domain.model.User
import org.tutvsisvoyi.testkmpapp.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val togglApiClient: TogglApiClient,
    private val realmDatabase: RealmDatabase,
    private val settings: Settings,
    private val httpClient: HttpClient
) : AuthRepository {

    override suspend fun loginWithCredentials(email: String, password: String): Result<AuthResponse> {
        return try {
            togglApiClient.loginWithCredentials(email, password).fold(
                onSuccess = { loginResponse ->
                    val user = User(
                        id = loginResponse.data.id,
                        email = loginResponse.data.email,
                        fullName = loginResponse.data.fullName,
                        defaultWorkspaceId = loginResponse.data.defaultWorkspaceId,
                        timezone = loginResponse.data.timezone,
                        imageUrl = loginResponse.data.imageUrl
                    )

                    // Store credentials for API calls - In production, encrypt these!
                    settings.putString("user_email", email)
                    settings.putString("user_password", password) // Encrypt this in production!
                    settings.putBoolean("logged_in", true)
                    settings.putString("auth_method", "credentials") // Track auth method

                    // Save user to local database
                    realmDatabase.saveUser(user)

                    Result.success(AuthResponse(user, ""))
                },
                onFailure = { error ->
                    Result.failure(Exception("Authentication failed: ${error.message}"))
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithToken(apiToken: String): Result<User> {
        return try {
            togglApiClient.loginWithToken(apiToken).fold(
                onSuccess = { response ->
                    val user = User(
                        id = response.id,
                        email = response.email,
                        fullName = response.fullName,
                        defaultWorkspaceId = response.defaultWorkspaceId,
                        timezone = response.timezone,
                        imageUrl = response.imageUrl
                    )

                    // Store API token (more secure than email/password)
                    settings.putString("api_token", apiToken)
                    settings.putBoolean("logged_in", true)
                    settings.putString("auth_method", "token") // Track auth method

                    // Clear any stored email/password if switching to token auth
                    settings.remove("user_email")
                    settings.remove("user_password")

                    // Save user to local database
                    realmDatabase.saveUser(user)

                    Result.success(user)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        settings.remove("api_token")
        settings.remove("user_email")
        settings.remove("user_password")
        settings.remove("auth_method")
        settings.putBoolean("logged_in", false)

        // Clear local database
        // TODO uncomment
//        realmDatabase.clearUser()
    }

    override suspend fun getCurrentUser(): User? {
        return realmDatabase.getUser()
    }

    override suspend fun isLoggedIn(): Boolean {
        val isLoggedInFlag = settings.getBoolean("logged_in", false)
        if (!isLoggedInFlag) return false

        // Check if we have valid credentials for either auth method
        val authMethod = settings.getStringOrNull("auth_method")
        return when (authMethod) {
            "token" -> getApiToken() != null
            "credentials" -> getStoredCredentials() != null
            else -> false
        }
    }

    override suspend fun getApiToken(): String? {
        return settings.getStringOrNull("api_token")
    }

    private fun getStoredCredentials(): Pair<String, String>? {
        val email = settings.getStringOrNull("user_email")
        val password = settings.getStringOrNull("user_password")
        return if (email != null && password != null) {
            Pair(email, password)
        } else null
    }

    // Helper method to get auth method for configuring HTTP client
    fun getAuthMethod(): String? {
        return settings.getStringOrNull("auth_method")
    }

    // Helper method to validate current authentication
    suspend fun validateAuth(): Boolean {
        return try {
            when (getAuthMethod()) {
                "token" -> {
                    val token = getApiToken()
                    if (token != null) {
                        togglApiClient.loginWithToken(token).isSuccess
                    } else false
                }
                "credentials" -> {
                    val credentials = getStoredCredentials()
                    if (credentials != null) {
                        togglApiClient.loginWithCredentials(credentials.first, credentials.second).isSuccess
                    } else false
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
}