package org.tutvsisvoyi.testkmpapp.di

import org.koin.dsl.module
import org.tutvsisvoyi.testkmpapp.ui.screens.login.LoginScreenModel

val screenModelModule = module {
    factory { LoginScreenModel(get(), get()) }
//    factory { TimerScreenModel(get(), get(), get(), get(), get()) }
//    factory { ProjectsScreenModel(get()) }
}