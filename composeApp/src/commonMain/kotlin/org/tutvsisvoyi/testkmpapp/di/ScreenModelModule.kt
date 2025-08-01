package org.tutvsisvoyi.testkmpapp.di

import org.koin.dsl.module
import org.tutvsisvoyi.testkmpapp.ui.screens.login.LoginScreenModel
import org.tutvsisvoyi.testkmpapp.ui.screens.profile.ProfileScreenModel
import org.tutvsisvoyi.testkmpapp.ui.screens.time_entries.TimeEntriesScreenModel

val screenModelModule = module {
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
//    factory { TimerScreenModel(get(), get(), get(), get(), get()) }
//    factory { ProjectsScreenModel(get()) }
}