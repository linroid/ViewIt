package com.linroid.viewit.data.model

import com.avos.avoscloud.AVObject
import com.orm.dsl.Ignore
import com.orm.dsl.MultiUnique
import com.orm.dsl.Table

/**
 * @author linroid <linroid@gmail.com>
 * @since 01/02/2017
 */
@MultiUnique("path, packageName")
@Table
class Favorite {
    var id: Long? = null
    var name: String = ""
    var path: String = ""
    var packageName: String = ""

    @Ignore
    var tree: ImageTree? = null

    fun toAVObject(className: String): AVObject {
        val obj = AVObject(className)
        obj.put("name", name)
        obj.put("path", path)
        obj.put("packageName", packageName)
        return obj
    }
}