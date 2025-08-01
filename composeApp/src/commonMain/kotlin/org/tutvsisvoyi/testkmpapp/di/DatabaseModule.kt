package org.tutvsisvoyi.testkmpapp.di

import com.russhwolf.settings.Settings
import org.koin.dsl.module
//import org.tutvsisvoyi.testkmpapp.data.database.realm.RealmDatabase

val databaseModule = module {
//    single { RealmDatabase() }
    single { Settings() }
}