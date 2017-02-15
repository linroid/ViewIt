package com.linroid.viewit.data.model

import com.avos.avoscloud.AVObject

/**
 * @author linroid <linroid@gmail.com>
 * @since 10/02/2017
 */
class CloudScanPath(val avObj: AVObject) {

    var path: String
        get() = avObj.getString("path")
        set(value) = avObj.put("path", value)

    var packageName: String
        get() = avObj.getString("packageName")
        set(value) = avObj.put("packageName", value)

    override fun toString(): String {
        return "[CloudScanPath]${avObj.toString()}"
    }
}