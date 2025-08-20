import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.tutvsisvoyi.testkmpapp.data.network.TogglApiClient
import org.tutvsisvoyi.testkmpapp.data.network.model.TogglReportRequest

@Serializable
data class TogglSummaryReportRequest(
    val start_date: String,
    val end_date: String,
    val grouping: String = "projects",
    val sub_grouping: String? = null,
    val project_ids: List<Long>? = null,
    val user_ids: List<Long>? = null,
    val client_ids: List<Long>? = null,
    val tag_ids: List<Long>? = null,
    val task_ids: List<Long>? = null,
    val billable: Boolean? = null,
    val description: String? = null,
    val group_ids: List<Long>? = null,
    val include_time_entry_ids: Boolean? = null,
    val max_duration_seconds: Int? = null,
    val min_duration_seconds: Int? = null,
    val order_by: String? = null,
    val order_dir: String? = "desc",
    val rounding: Int? = null,
    val rounding_minutes: Int? = null,
    val distinguish_rates: Boolean? = null,
    val hide_amounts: Boolean? = null,
    val hide_rates: Boolean? = null,
    val collapse: Boolean? = null,

    // PDF-specific formatting
    val cents_separator: String? = ".",
    val date_format: String? = "MM/DD/YYYY",
    val duration_format: String? = "improved"
)

class TogglPdfExporter(
    private val togglApiClient: TogglApiClient
) {

//    /**
//     * Export detailed report as PDF directly from Toggl API
//     */
//    suspend fun exportDetailedReportPdf(
//        workspaceId: Long,
//        startDate: String,
//        endDate: String,
//        projectIds: List<Long>? = null,
//        userIds: List<Long>? = null,
//    ): Result<ByteArray> {
//        return try {
//            val client = createAuthenticatedClient()
//
//            val request = TogglReportRequest(
//                start_date = startDate,
//                end_date = endDate,
//                project_ids = projectIds,
//                user_ids = userIds,
//                grouped = false,
//                enrich_response = true,
//                // PDF formatting options
//                date_format = "MM/DD/YYYY",
//                duration_format = "improved",
//                hour_format = "H:mm",
//                display_mode = "days",
//                cents_separator = ".",
//                page_size = 50
//            )
//
//            println("Making PDF request to: https://api.track.toggl.com/reports/api/v3/workspace/$workspaceId/search/time_entries.pdf")
//            println("Request body: ${Json.encodeToString(TogglReportRequest.serializer(), request)}")
//
//            // Make request to PDF endpoint
//            val response = client.post("https://api.track.toggl.com/reports/api/v3/workspace/$workspaceId/search/time_entries.pdf") {
//                contentType(ContentType.Application.Json)
//                setBody(request)
//            }
//
//            println("Response status: ${response.status}")
//            println("Response headers: ${response.headers}")
//
//            if (response.status.value in 200..299) {
//                val pdfBytes = response.body<ByteArray>()
//                client.close()
//                println("PDF received successfully, size: ${pdfBytes.size} bytes")
//                Result.success(pdfBytes)
//            } else {
//                val errorBody = try {
//                    response.body<String>()
//                } catch (e: Exception) {
//                    "Could not read error response"
//                }
//                client.close()
//                println("Error response body: $errorBody")
//                Result.failure(Exception("PDF export failed with status: ${response.status.value} - $errorBody"))
//            }
//
//        } catch (e: Exception) {
//            println("Exception during PDF export: ${e.message}")
//            e.printStackTrace()
//            Result.failure(Exception("Failed to export PDF: ${e.message}"))
//        }
//    }

    suspend fun exportDetailedReportPdf(
        workspaceId: Long,
        startDate: String, // Format: "YYYY-MM-DD"
        endDate: String,   // Format: "YYYY-MM-DD"
    ): Result<ByteArray> {
        return try {
            val client = createAuthenticatedClient()

            // Absolute minimum request - just dates
            val requestBody = mapOf(
                "start_date" to startDate,
                "end_date" to endDate
            )

            println("Ultra minimal PDF request to: https://api.track.toggl.com/reports/api/v3/workspace/$workspaceId/search/time_entries.pdf")
            println("Request body: $requestBody")

            val response = client.post("https://api.track.toggl.com/reports/api/v3/workspace/$workspaceId/search/time_entries.pdf") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            println("Response status: ${response.status}")

            if (response.status.value in 200..299) {
                val pdfBytes = response.body<ByteArray>()
                client.close()
                println("PDF received successfully, size: ${pdfBytes.size} bytes")
                Result.success(pdfBytes)
            } else {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    "Could not read error response"
                }
                client.close()
                println("Error response: $errorBody")
                Result.failure(Exception("PDF export failed: ${response.status.value} - $errorBody"))
            }

        } catch (e: Exception) {
            println("Exception: ${e.message}")
            Result.failure(Exception("Failed to export PDF: ${e.message}"))
        }
    }

    /**
     * Export summary report as PDF
     */
    suspend fun exportSummaryReportPdf(
        workspaceId: Long,
        startDate: String,
        endDate: String,
        projectIds: List<Long>? = null,
        userIds: List<Long>? = null,
        grouping: String = "projects"
    ): Result<ByteArray> {
        return try {
            val client = createAuthenticatedClient()

            val request = TogglSummaryReportRequest(
                start_date = startDate,
                end_date = endDate,
                project_ids = projectIds,
                user_ids = userIds,
                grouping = grouping,
                sub_grouping = "time_entries",
                date_format = "MM/DD/YYYY",
                duration_format = "improved",
                cents_separator = "."
            )

            println("Making summary PDF request to: https://api.track.toggl.com/reports/api/v3/workspace/$workspaceId/summary/time_entries.pdf")

            val response = client.post("https://api.track.toggl.com/reports/api/v3/workspace/$workspaceId/summary/time_entries.pdf") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status.value in 200..299) {
                val pdfBytes = response.body<ByteArray>()
                client.close()
                Result.success(pdfBytes)
            } else {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    "Could not read error response"
                }
                client.close()
                Result.failure(Exception("Summary PDF export failed with status: ${response.status.value} - $errorBody"))
            }

        } catch (e: Exception) {
            Result.failure(Exception("Failed to export summary PDF: ${e.message}"))
        }
    }

    /**
     * Test method to check if the API is working with a simple JSON request first
     */
    suspend fun testDetailedReportJson(
        workspaceId: Long,
        startDate: String,
        endDate: String
    ): Result<String> {
        return try {
            val client = createAuthenticatedClient()

            val request = TogglReportRequest(
                start_date = startDate,
                end_date = endDate,
                page_size = 10,
                enrich_response = true
            )

            // Test with JSON endpoint first
            val response = client.post("https://api.track.toggl.com/reports/api/v3/workspace/$workspaceId/search/time_entries") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            println("JSON Test - Status: ${response.status}")

            if (response.status.value in 200..299) {
                val jsonResponse = response.body<String>()
                client.close()
                Result.success(jsonResponse)
            } else {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    "Could not read error response"
                }
                client.close()
                Result.failure(Exception("JSON test failed with status: ${response.status.value} - $errorBody"))
            }

        } catch (e: Exception) {
            Result.failure(Exception("JSON test failed: ${e.message}"))
        }
    }

    private fun createAuthenticatedClient(): HttpClient {
        return togglApiClient.createAuthenticatedClient()
    }
}