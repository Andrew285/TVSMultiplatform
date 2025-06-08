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

                    // Store email/password for API calls (encrypted in production!)
                    settings.putString("user_email", email)
                    settings.putString("user_password", password) // Encrypt this!
                    settings.putBoolean("logged_in", true)

                    // Save user to local database
                    realmDatabase.saveUser(user)

                    Result.success(AuthResponse(user, "")) // No API token from email/password
                },
                onFailure = { Result.failure(it) }
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

                    // Store API token
                    settings.putString("api_token", apiToken)
                    settings.putBoolean("logged_in", true)

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
        // Clear local database if needed
    }

    override suspend fun getCurrentUser(): User? {
        return realmDatabase.getUser()
    }

    override suspend fun isLoggedIn(): Boolean {
        return getApiToken() != null
    }

    override suspend fun getApiToken(): String? {
        return settings.getStringOrNull("api_token")
    }

    private fun configureAuth(apiToken: String) {
        // This would be handled in the HTTP client configuration
        // See the DI module for proper implementation
    }
}