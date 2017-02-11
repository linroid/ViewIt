package com.linroid.viewit.data.model

import com.avos.avoscloud.AVObject

/**
 * @author linroid <linroid@gmail.com>
 * @since 10/02/2017
 */
class ScanPath {
    var path: String = ""
    var packageName: String = ""

    constructor(avObj: AVObject) {
        path = avObj.getString("path")
        packageName = avObj.getString("packageName")
    }

    constructor()

    fun toAVObject(name: String): AVObject {
        val obj = AVObject(name)
        obj.put("path", path)
        obj.put("packageName", packageName)
        return obj
    }
}