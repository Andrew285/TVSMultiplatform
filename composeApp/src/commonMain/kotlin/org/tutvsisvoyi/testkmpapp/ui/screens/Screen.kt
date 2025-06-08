package org.tutvsisvoyi.testkmpapp.ui.screens

sealed class Screen(val route: String) {
    object TasksList: Screen("task_list")
    object Reports: Screen("reports")
    object ReportDetails: Screen("report_details")
    object Settings: Screen("settings")
}