package org.tutvsisvoyi.testkmpapp.data.database.realm

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class RealmWorkspace : RealmObject {
    @PrimaryKey
    var id: Long = 0
    var name: String = ""
    var organizationId: Long? = null
    var premium: Boolean = false
}