package com.linroid.viewit.data.model

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey

/**
 * @author linroid <linroid@gmail.com>
 * @since 01/02/2017
 */
open class Favorite : RealmObject() {
    @PrimaryKey
    open var id: Long = 0
    open var name: String = ""
    open var pathPattern: String = ""
    open var packageName: String = ""
    @Ignore
    open var tree: ImageTree? = null
}