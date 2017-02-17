package com.linroid.viewit.data.model

import com.orm.dsl.Table
import com.orm.dsl.Unique

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/02/2017
 */
@Table
class AppUsage {
    var id: Long? = null
    @Unique
    var packageName: String = ""
    var count: Int = 0

    override fun toString(): String {
        return "AppUsage(id=$id, packageName='$packageName', count=$count)"
    }

}