package org.tutvsisvoyi.testkmpapp.di
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import io.ktor.serialization.kotlinx.json.*
import com.russhwolf.settings.Settings
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import org.tutvsisvoyi.testkmpapp.data.network.TogglApiClient

val networkModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                })
            }

//            install(Logging) {
//                logger = Logger.DEFAULT
//                level = LogLevel.ALL // This will help debug
//            }

            // Fix the auth configuration
            install(Auth) {
                basic {
                    credentials {
                        val settings: Settings = get()
                        val apiToken = settings.getStringOrNull("api_token")
                        if (apiToken != null) {
                            // Toggl uses token as username, "api_token" as password
                            BasicAuthCredentials(username = apiToken, password = "api_token")
                        } else {
                            null // Don't provide credentials if no token
                        }
                    }
                    sendWithoutRequest { true } // Send auth with every request
                }
            }
        }
    }
    single { TogglApiClient(get()) }
}