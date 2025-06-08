package org.tutvsisvoyi.testkmpapp.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.tutvsisvoyi.testkmpapp.data.network.model.LoginRequest
import org.tutvsisvoyi.testkmpapp.data.network.model.LoginResponse
import org.tutvsisvoyi.testkmpapp.data.network.model.SessionResponse
import org.tutvsisvoyi.testkmpapp.data.network.model.TogglTimeEntryResponse
import org.tutvsisvoyi.testkmpapp.data.network.model.TogglUserResponse
import org.tutvsisvoyi.testkmpapp.data.network.model.TogglWorkspaceResponse

class TogglApiClient(private val httpClient: HttpClient) {
    val baseUrl = "https://api.track.toggl.com/api/v9"

    suspend fun getCurrentUser(): Result<TogglUserResponse> {
        return try {
            val response: TogglUserResponse = httpClient.get("$baseUrl/me").body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWorkspaces(): Result<List<TogglWorkspaceResponse>> {
        return try {
            val response: List<TogglWorkspaceResponse> = httpClient.get("$baseUrl/workspaces").body()
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
            val url = buildString {
                append("$baseUrl/me/time_entries")
                val params = mutableListOf<String>()
                startDate?.let { params.add("start_date=$it") }
                endDate?.let { params.add("end_date=$it") }
                if (params.isNotEmpty()) {
                    append("?${params.joinToString("&")}")
                }
            }

            val response: List<TogglTimeEntryResponse> = httpClient.get(url).body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentTimeEntry(): Result<TogglTimeEntryResponse?> {
        return try {
            val response: TogglTimeEntryResponse? = httpClient.get("$baseUrl/me/time_entries/current").body()
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
            }

            val response: SessionResponse = client.post("https://api.track.toggl.com/api/v9/me") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }.body()

            // After successful login, get the user's API token
            val userResponse = response.data

            // Create authenticated client to get API token
            val authClient = HttpClient {
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

            // Get user profile which includes API token info
            val profileResponse: TogglUserResponse = authClient.get("https://api.track.toggl.com/api/v9/me").body()

            client.close()
            authClient.close()

            Result.success(LoginResponse(
                data = profileResponse,
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

            val response: TogglUserResponse = client.get("https://api.track.toggl.com/api/v9/me").body()
            client.close()
            Result.success(response)

        } catch (e: Exception) {
            Result.failure(Exception("Invalid API token: ${e.message}"))
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
}