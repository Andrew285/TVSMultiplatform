package org.tutvsisvoyi.testkmpapp.di

import TogglPdfExporter
import org.koin.dsl.module
import org.tutvsisvoyi.testkmpapp.Platform
import org.tutvsisvoyi.testkmpapp.ui.screens.login.LoginScreenModel
import org.tutvsisvoyi.testkmpapp.ui.screens.profile.ProfileScreenModel
import org.tutvsisvoyi.testkmpapp.ui.screens.reports.ReportsScreenModel
import org.tutvsisvoyi.testkmpapp.ui.screens.time_entries.TimeEntriesScreenModel

val screenModelModule = module {
    single { TogglPdfExporter(get()) }

    factory { LoginScreenModel(get(), get()) }
    factory { TimeEntriesScreenModel(
        timeEntryRepository = get(),
        workspaceRepository = get(),
        projectsRepository = get(),
        tagsRepository = get()
    )}
    factory {
        ProfileScreenModel(
            authRepository = get(),
            settings = get()
        )
    }
    factory {
        ReportsScreenModel(
            timeEntryRepository = get(),
            workspaceRepository = get(),
            projectsRepository = get(),
            togglApiClient = get()
        )
    }
//    factory { TimerScreenModel(get(), get(), get(), get(), get()) }
//    factory { ProjectsScreenModel(get()) }
}