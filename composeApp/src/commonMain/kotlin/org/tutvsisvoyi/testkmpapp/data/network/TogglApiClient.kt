package org.tutvsisvoyi.testkmpapp.data.network

import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.tutvsisvoyi.testkmpapp.data.network.model.CreateTimeEntryRequest
import org.tutvsisvoyi.testkmpapp.data.network.model.LoginResponse
import org.tutvsisvoyi.testkmpapp.data.network.model.TogglProjectResponse
import org.tutvsisvoyi.testkmpapp.data.network.model.TogglTagResponse
import org.tutvsisvoyi.testkmpapp.data.network.model.TogglTimeEntryResponse
import org.tutvsisvoyi.testkmpapp.data.network.model.TogglUserResponse
import org.tutvsisvoyi.testkmpapp.data.network.model.TogglWorkspaceResponse

// Fixed authentication for your TogglApiClient

class TogglApiClient(
    private val httpClient: HttpClient,
    private val settings: Settings
) {
    val baseUrl = "https://api.track.toggl.com/api/v9"

    // FIXED: Create authenticated client with correct credentials
    fun createAuthenticatedClient(): HttpClient {
        val authMethod = settings.getStringOrNull("auth_method")

        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true // Important: encode all fields
                })
            }

            install(Auth) {
                basic {
                    credentials {
                        when (authMethod) {
                            "token" -> {
                                val apiToken = settings.getStringOrNull("api_token")
                                val userEmail = settings.getStringOrNull("user_email") // MUST have this
                                if (apiToken != null && userEmail != null) {
                                    // CORRECT: email as username, token as password
                                    BasicAuthCredentials(username = userEmail, password = apiToken)
                                } else {
                                    throw Exception("No API token or email found. Both are required for token auth.")
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

            // Add logging to debug requests
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
        }
    }

    // UPDATED: Save both email and token when authenticating with token
    suspend fun loginWithToken(email: String, apiToken: String): Result<TogglUserResponse> {
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
                            // Use email:token for authentication
                            BasicAuthCredentials(username = email, password = apiToken)
                        }
                    }
                }
            }

            val response: TogglUserResponse = client.get("$baseUrl/me").body()

            // IMPORTANT: Save both email and token
            settings.putString("auth_method", "token")
            settings.putString("api_token", apiToken)
            settings.putString("user_email", email) // Save email too!

            client.close()
            Result.success(response)

        } catch (e: Exception) {
            Result.failure(Exception("Invalid API token: ${e.message}"))
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

    // UPDATED: Enhanced createNewTimeEntry with better error handling
    suspend fun createNewTimeEntry(
        workspaceId: Long,
        request: CreateTimeEntryRequest
    ): Result<TogglTimeEntryResponse?> {
        return try {
            val client = createAuthenticatedClient()

            // Debug: Print what we're sending
            println("Creating time entry:")
            println("Workspace ID: $workspaceId")
            println("Request: $request")

            val response = client.post("$baseUrl/workspaces/${workspaceId}/time_entries") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            println("Response status: ${response.status}")

            if (response.status.isSuccess()) {
                val responseBody = response.body<TogglTimeEntryResponse>()
                client.close()
                Result.success(responseBody)
            } else {
                val errorMessage = try {
                    response.body<String>()
                } catch (e: Exception) {
                    "Unknown error occurred"
                }
                println("Error response: $errorMessage")
                client.close()
                Result.failure(Exception("HTTP ${response.status.value}: $errorMessage"))
            }
        } catch (e: Exception) {
            println("Exception creating time entry: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // Helper method to validate authentication setup
    fun validateAuthSetup(): String? {
        val authMethod = settings.getStringOrNull("auth_method")
        return when (authMethod) {
            "token" -> {
                val apiToken = settings.getStringOrNull("api_token")
                val userEmail = settings.getStringOrNull("user_email")
                when {
                    apiToken == null -> "Missing API token"
                    userEmail == null -> "Missing user email (required for token auth)"
                    else -> null // All good
                }
            }
            "credentials" -> {
                val email = settings.getStringOrNull("user_email")
                val password = settings.getStringOrNull("user_password")
                when {
                    email == null -> "Missing email"
                    password == null -> "Missing password"
                    else -> null // All good
                }
            }
            else -> "No authentication method configured"
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

    suspend fun getTagsByWorkspace(workspaceId: Int): Result<List<TogglTagResponse>> {
        return try {
            val client = createAuthenticatedClient()
            val fullUrl = "$baseUrl/workspaces/$workspaceId/tags"
            val tagsBodyResponse: List<TogglTagResponse> = client.get(fullUrl).body()
            Result.success(tagsBodyResponse)
        } catch(e: Exception) {
            Result.failure(e)
        }
    }
}


//// Test function to validate everything is working
//suspend fun testTimeEntryCreation(
//    togglClient: TogglApiClient,
//    workspaceId: Long,
//    description: String = "Test from KMP app"
//) {
//    // First validate auth setup
//    val authError = togglClient.validateAuthSetup()
//    if (authError != null) {
//        println("❌ Auth setup error: $authError")
//        return
//    }
//
//    // Test getting current user first
//    println("Testing authentication...")
//    val userResult = togglClient.getCurrentUser()
//    userResult.fold(
//        onSuccess = { user ->
//            println("✅ Authentication successful!")
//            println("User: ${user.fullname} (${user.email})")
//        },
//        onFailure = { error ->
//            println("❌ Authentication failed: ${error.message}")
//            return
//        }
//    )
//
//    // Now test creating time entry
//    println("\nTesting time entry creation...")
//    val request = CreateTimeEntryRequest(
//        description = description,
//        projectId = null,
//        taskId = null,
//        tags = emptyList(),
//        start = Clock.System.now().toString(),
//        stop = null,
//        duration = -1,
//        workspaceId = workspaceId,
//        billable = false,
//        createdWith = "TVS Multiplatform Test"
//    )
//
//    val result = togglClient.createNewTimeEntry(workspaceId, request)
//    result.fold(
//        onSuccess = { response ->
//            println("✅ Time entry created successfully!")
//            println("ID: ${response?.id}")
//            println("Description: ${response?.description}")
//        },
//        onFailure = { error ->
//            println("❌ Time entry creation failed:")
//            println("Error: ${error.message}")
//        }
//    )
