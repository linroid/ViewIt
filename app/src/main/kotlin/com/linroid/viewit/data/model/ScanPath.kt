package com.linroid.viewit.data.model

import com.avos.avoscloud.AVObject
import com.orm.dsl.Table

/**
 * @author linroid <linroid@gmail.com>
 * @since 10/02/2017
 */
@Table
class ScanPath {
    val id: Long? = null
    var path: String = ""
    var packageName: String = ""

    fun toAVObject(name: String): AVObject {
        val obj = AVObject(name)
        obj.put("path", path)
        obj.put("packageName", packageName)
        return obj
    }
}