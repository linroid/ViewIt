package com.linroid.viewit.data.model

import com.avos.avoscloud.AVObject

/**
 * @author linroid <linroid@gmail.com>
 * @since 10/02/2017
 */
class CloudScanPath(val avObj: AVObject) {
    var path: String = ""
    var packageName: String = ""

    init {
        path = avObj.getString("path")
        packageName = avObj.getString("packageName")
    }
}