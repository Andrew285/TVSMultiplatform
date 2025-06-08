package org.tutvsisvoyi.testkmpapp.data.database.realm

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class RealmUser: RealmObject {
    @PrimaryKey
    var id: Long = 0
    var email: String = ""
    var fullName: String = ""
    var defaultWorkspaceId: Long = 0
    var timezone: String = ""
    var imageUrl: String? = null
}