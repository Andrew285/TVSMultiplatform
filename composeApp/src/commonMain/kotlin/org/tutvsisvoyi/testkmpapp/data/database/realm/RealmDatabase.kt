package org.tutvsisvoyi.testkmpapp.data.database.realm

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.serialization.encodeToString
import org.tutvsisvoyi.testkmpapp.domain.model.TimeEntry
import org.tutvsisvoyi.testkmpapp.domain.model.User
import org.tutvsisvoyi.testkmpapp.domain.model.Workspace

class RealmDatabase {
    private val realm: Realm by lazy {
        val config = RealmConfiguration.Builder(
            schema = setOf(
                RealmUser::class,
                RealmTimeEntry::class,
                RealmWorkspace::class,
            )
        ).name("tvs_realm.app").build()

        Realm.open(config)
    }

    // User operations
    suspend fun saveUser(user: User) {
        realm.write {
            copyToRealm(RealmUser().apply {
                id = user.id
                email = user.email
                fullName = user.fullName
                defaultWorkspaceId = user.defaultWorkspaceId
                timezone = user.timezone
                imageUrl = user.imageUrl
            }, updatePolicy = UpdatePolicy.ALL)
        }
    }

    suspend fun getUser(): User? {
        return realm.query<RealmUser>().first().find()?.let { realmUser ->
            User(
                id = realmUser.id,
                email = realmUser.email,
                fullName = realmUser.fullName,
                defaultWorkspaceId = realmUser.defaultWorkspaceId,
                timezone = realmUser.timezone,
                imageUrl = realmUser.imageUrl
            )
        }
    }
    // Workspace operations
    suspend fun saveWorkspaces(workspaces: List<Workspace>) {
        realm.write {
            // Clear existing workspaces
            val existingWorkspaces = query<RealmWorkspace>().find()
            delete(existingWorkspaces)

            // Insert new workspaces
            workspaces.forEach { workspace ->
                copyToRealm(RealmWorkspace().apply {
                    id = workspace.id
                    name = workspace.name
                    organizationId = workspace.organizationId
                    premium = workspace.premium
                }, updatePolicy = UpdatePolicy.ALL)
            }
        }
    }

    fun getWorkspaces(): Flow<List<Workspace>> {
        return realm.query<RealmWorkspace>().asFlow().map { results ->
            results.list.map { realmWorkspace ->
                Workspace(
                    id = realmWorkspace.id,
                    name = realmWorkspace.name,
                    organizationId = realmWorkspace.organizationId,
                    premium = realmWorkspace.premium
                )
            }
        }
    }

    // TimeEntry operations
    suspend fun saveTimeEntries(timeEntries: List<TimeEntry>) {
        realm.write {
            timeEntries.forEach { timeEntry ->
                timeEntry.id?.let { id ->
                    copyToRealm(RealmTimeEntry().apply {
                        this.id = id
                        description = timeEntry.description
                        projectId = timeEntry.projectId
                        workspaceId = timeEntry.workspaceId
                        startTime = timeEntry.start.toEpochMilliseconds()
                        stopTime = timeEntry.stop?.toEpochMilliseconds()
                        duration = timeEntry.duration
                        tags = kotlinx.serialization.json.Json.encodeToString(timeEntry.tags)
                    }, updatePolicy = UpdatePolicy.ALL)
                }
            }
        }
    }

    suspend fun saveTimeEntry(timeEntry: TimeEntry) {
        realm.write {
            timeEntry.id?.let { id ->
                copyToRealm(RealmTimeEntry().apply {
                    this.id = id
                    description = timeEntry.description
                    projectId = timeEntry.projectId
                    workspaceId = timeEntry.workspaceId
                    startTime = timeEntry.start.toEpochMilliseconds()
                    stopTime = timeEntry.stop?.toEpochMilliseconds()
                    duration = timeEntry.duration
                    tags = kotlinx.serialization.json.Json.encodeToString(timeEntry.tags)
                }, updatePolicy = UpdatePolicy.ALL)
            }
        }
    }

    fun getTimeEntries(
        workspaceId: Long,
        startDate: kotlinx.datetime.LocalDate? = null,
        endDate: kotlinx.datetime.LocalDate? = null
    ): Flow<List<TimeEntry>> {
        val query = if (startDate != null && endDate != null) {
            val startMillis = startDate.atStartOfDayIn(kotlinx.datetime.TimeZone.currentSystemDefault()).toEpochMilliseconds()
            val endMillis = endDate.atTime(23, 59, 59).toInstant(kotlinx.datetime.TimeZone.currentSystemDefault()).toEpochMilliseconds()
            realm.query<RealmTimeEntry>(
                "workspaceId == $0 AND startTime >= $1 AND startTime <= $2",
                workspaceId, startMillis, endMillis
            )
        } else {
            realm.query<RealmTimeEntry>("workspaceId == $0", workspaceId)
        }

        return query.asFlow().map { results ->
            results.list.map { realmEntry ->
                TimeEntry(
                    id = realmEntry.id,
                    description = realmEntry.description,
                    projectId = realmEntry.projectId,
                    workspaceId = realmEntry.workspaceId,
                    start = kotlinx.datetime.Instant.fromEpochMilliseconds(realmEntry.startTime),
                    stop = realmEntry.stopTime?.let { kotlinx.datetime.Instant.fromEpochMilliseconds(it) },
                    duration = realmEntry.duration,
                    tags = try {
                        kotlinx.serialization.json.Json.decodeFromString(realmEntry.tags)
                    } catch (e: Exception) {
                        emptyList()
                    }
                )
            }
        }
    }
}