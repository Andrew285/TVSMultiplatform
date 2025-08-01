package org.tutvsisvoyi.testkmpapp.di

import org.koin.dsl.module
import org.tutvsisvoyi.testkmpapp.data.repository.AuthRepositoryImpl
import org.tutvsisvoyi.testkmpapp.data.repository.ProjectRepositoryImpl
import org.tutvsisvoyi.testkmpapp.data.repository.TagsRepositoryImpl
import org.tutvsisvoyi.testkmpapp.data.repository.TimeEntryRepositoryImpl
import org.tutvsisvoyi.testkmpapp.data.repository.WorkspaceRepositoryImpl
import org.tutvsisvoyi.testkmpapp.domain.repository.AuthRepository
import org.tutvsisvoyi.testkmpapp.domain.repository.ProjectRepository
import org.tutvsisvoyi.testkmpapp.domain.repository.TagsRepository
import org.tutvsisvoyi.testkmpapp.domain.repository.TimeEntryRepository
import org.tutvsisvoyi.testkmpapp.domain.repository.WorkspaceRepository

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    single<WorkspaceRepository> { WorkspaceRepositoryImpl(get(), get()) }
    single<ProjectRepository> { ProjectRepositoryImpl(get()) }
    single<TagsRepository> { TagsRepositoryImpl(get()) }
    single<TimeEntryRepository> { TimeEntryRepositoryImpl(get()) }
}