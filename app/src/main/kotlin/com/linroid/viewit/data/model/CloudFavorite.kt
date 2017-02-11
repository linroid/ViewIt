package com.linroid.viewit.data.model

import com.avos.avoscloud.AVObject
import com.orm.dsl.Ignore

/**
 * @author linroid <linroid@gmail.com>
 * @since 01/02/2017
 */
class CloudFavorite {
    var name: String = ""
    var path: String = ""
    var packageName: String = ""

    @Ignore
    var tree: ImageTree? = null

    constructor(avObj: AVObject) {
        name = avObj.getString("name")
        path = avObj.getString("path")
        packageName = avObj.getString("packageName")
    }

    constructor()

    fun toAVObject(name: String): AVObject {
        val obj = AVObject(name)
        obj.put("name", name)
        obj.put("path", path)
        obj.put("packageName", packageName)
        return obj
    }
}