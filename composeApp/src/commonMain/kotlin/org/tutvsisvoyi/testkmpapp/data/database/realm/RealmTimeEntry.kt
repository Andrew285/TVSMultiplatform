package org.tutvsisvoyi.testkmpapp.data.database.realm

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class RealmTimeEntry: RealmObject {
    @PrimaryKey
    var id: Long = 0
    var description: String? = null
    var projectId: Long? = null
    var workspaceId: Long = 0
    var startTime: Long = 0 // Epoch milliseconds
    var stopTime: Long? = null
    var duration: Long = 0
    var tags: String = "" // JSON string
}