package org.tutvsisvoyi.testkmpapp.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class TogglReportRequest(
    val billable: Boolean? = null,
    val client_ids: List<Long>? = null,
    val description: String? = null,
    val endTime: String? = null,
    val end_date: String? = null,
    val enrich_response: Boolean? = null,
    val first_id: Long? = null,
    val first_row_number: Int? = null,
    val first_timestamp: Long? = null,
    val group_ids: List<Long>? = null,
    val grouped: Boolean? = null,
    val hide_amounts: Boolean? = null,
    val max_duration_seconds: Int? = null,
    val min_duration_seconds: Int? = null,
    val order_by: String? = null,
    val order_dir: String? = "asc",
    val page_size: Int? = null,
    val project_ids: List<Long>? = null,
    val rounding: Int? = null,
    val rounding_minutes: Int? = null,
    val startTime: String? = null,
    val start_date: String? = null,
    val tag_ids: List<Long>? = null,
    val task_ids: List<Long>? = null,
    val time_entry_ids: List<Long>? = null,
    val user_ids: List<Long>? = null,

    // PDF-specific formatting options
    val cents_separator: String? = null,
    val date_format: String? = null,
    val display_mode: String? = null,
    val duration_format: String? = null,
    val hour_format: String? = null
)