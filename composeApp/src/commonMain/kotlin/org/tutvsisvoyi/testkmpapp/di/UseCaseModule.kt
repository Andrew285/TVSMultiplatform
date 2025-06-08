package org.tutvsisvoyi.testkmpapp.di

import org.koin.dsl.module
import org.tutvsisvoyi.testkmpapp.domain.usecase.GetTodayTimeEntriesUseCase
import org.tutvsisvoyi.testkmpapp.domain.usecase.LoginWithCredentialsUseCase
import org.tutvsisvoyi.testkmpapp.domain.usecase.LoginWithTokenUseCase

val useCaseModule = module {
    // Auth use cases
    single { LoginWithCredentialsUseCase(get()) }
    single { LoginWithTokenUseCase(get()) }
//    single { LogoutUseCase(get()) }

    // Timer use cases
//    single { StartTimerUseCase(get(), get()) }
//    single { StopTimerUseCase(get()) }
//    single { GetCurrentTimerUseCase(get()) }

    // Project use cases
//    single { GetProjectsUseCase(get(), get()) }

    // Time entry use cases
    single { GetTodayTimeEntriesUseCase(get(), get()) }
}