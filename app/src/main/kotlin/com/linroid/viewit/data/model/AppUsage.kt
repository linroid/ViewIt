package com.linroid.viewit.data.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/02/2017
 */
open class AppUsage : RealmObject() {
    @PrimaryKey
    open var packageName: String = ""
    open var openCount: Int = 0
}