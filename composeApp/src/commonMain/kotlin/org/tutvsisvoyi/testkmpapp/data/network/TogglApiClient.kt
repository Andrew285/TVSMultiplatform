package org.tutvsisvoyi.testkmpapp.data.network

import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.tutvsisvoyi.testkmpapp.data.network.model.LoginResponse
import org.tutvsisvoyi.testkmpapp.data.network.model.TogglProjectResponse
import org.tutvsisvoyi.testkmpapp.data.network.model.TogglTimeEntryResponse
import org.tutvsisvoyi.testkmpapp.data.network.model.TogglUserResponse
import org.tutvsisvoyi.testkmpapp.data.network.model.TogglWorkspaceResponse
import org.tutvsisvoyi.testkmpapp.domain.model.Project

class TogglApiClient(
    private val httpClient: HttpClient,
    private val settings: Settings
) {
    val baseUrl = "https://api.track.toggl.com/api/v9"

    // Create authenticated client for API calls
    private fun createAuthenticatedClient(): HttpClient {
        val authMethod = settings.getStringOrNull("auth_method")

        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }

            install(Auth) {
                basic {
                    credentials {
                        when (authMethod) {
                            "token" -> {
                                val apiToken = settings.getStringOrNull("api_token")
                                if (apiToken != null) {
                                    BasicAuthCredentials(username = apiToken, password = "api_token")
                                } else {
                                    throw Exception("No API token found")
                                }
                            }
                            "credentials" -> {
                                val email = settings.getStringOrNull("user_email")
                                val password = settings.getStringOrNull("user_password")
                                if (email != null && password != null) {
                                    BasicAuthCredentials(username = email, password = password)
                                } else {
                                    throw Exception("No credentials found")
                                }
                            }
                            else -> throw Exception("No authentication method configured")
                        }
                    }
                }
            }
        }
    }


    suspend fun getCurrentUser(): Result<TogglUserResponse> {
        return try {
            val client = createAuthenticatedClient()
            val response: TogglUserResponse = client.get("$baseUrl/me").body()
            client.close()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWorkspaces(): Result<List<TogglWorkspaceResponse>> {
        return try {
            val client = createAuthenticatedClient()
            val response: List<TogglWorkspaceResponse> = client.get("$baseUrl/workspaces").body()
            client.close()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTimeEntries(
        startDate: String? = null,
        endDate: String? = null
    ): Result<List<TogglTimeEntryResponse>> {
        return try {
            val client = createAuthenticatedClient()

            val url = buildString {
                append("$baseUrl/me/time_entries")
                val params = mutableListOf<String>()
                startDate?.let { params.add("start_date=$it") }
                endDate?.let { params.add("end_date=$it") }
                if (params.isNotEmpty()) {
                    append("?${params.joinToString("&")}")
                }
            }

            val response: List<TogglTimeEntryResponse> = client.get(url).body()
            client.close()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentTimeEntry(): Result<TogglTimeEntryResponse?> {
        return try {
            val client = createAuthenticatedClient()
            val response: TogglTimeEntryResponse? = client.get("$baseUrl/me/time_entries/current").body()
            client.close()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithCredentials(email: String, password: String): Result<LoginResponse> {
        return try {
            val client = HttpClient {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    })
                }
                install(Auth) {
                    basic {
                        credentials {
                            BasicAuthCredentials(username = email, password = password)
                        }
                    }
                }
            }

            // Use GET request to /me endpoint with basic auth
            val response: TogglUserResponse = client.get("$baseUrl/me").body()

            client.close()

            Result.success(LoginResponse(
                data = response,
                apiToken = null // Toggl doesn't return API token in login response
            ))

        } catch (e: Exception) {
            Result.failure(Exception("Login failed: ${e.message}"))
        }
    }

    suspend fun loginWithToken(apiToken: String): Result<TogglUserResponse> {
        return try {
            val client = HttpClient {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    })
                }
                install(Auth) {
                    basic {
                        credentials {
                            BasicAuthCredentials(username = apiToken, password = "api_token")
                        }
                    }
                }
            }

            val response: TogglUserResponse = client.get("$baseUrl/me").body()
            client.close()
            Result.success(response)

        } catch (e: Exception) {
            Result.failure(Exception("Invalid API token: ${e.message}"))
        }
    }

    // Helper method to check if we have valid authentication
    fun hasValidAuth(): Boolean {
        val authMethod = settings.getStringOrNull("auth_method")
        return when (authMethod) {
            "token" -> settings.getStringOrNull("api_token") != null
            "credentials" -> {
                val email = settings.getStringOrNull("user_email")
                val password = settings.getStringOrNull("user_password")
                email != null && password != null
            }
            else -> false
        }
    }

    // Get user's API token (requires email/password auth)
    suspend fun getUserApiToken(email: String, password: String): Result<String> {
        return try {
            val client = HttpClient {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    })
                }
                install(Auth) {
                    basic {
                        credentials {
                            BasicAuthCredentials(username = email, password = password)
                        }
                    }
                }
            }

            // Get user profile to extract API token
            val response: TogglUserResponse = client.get("https://api.track.toggl.com/api/v9/me").body()

            // Note: Toggl API doesn't directly return the API token in responses
            // You need to direct users to get it from their profile page
            // For now, we'll store the email/password and use basic auth

            client.close()
            Result.failure(Exception("API token must be obtained from Toggl profile page"))

        } catch (e: Exception) {
            Result.failure(Exception("Failed to authenticate: ${e.message}"))
        }
    }

    suspend fun getUserProjects(): Result<List<TogglProjectResponse>> {
        return try {
            val client = createAuthenticatedClient()

            val url = "$baseUrl/me/projects"
            val response: List<TogglProjectResponse> = client.get(url).body()
            client.close()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to retrieve projects: ${e.message}"))
        }
    }
}