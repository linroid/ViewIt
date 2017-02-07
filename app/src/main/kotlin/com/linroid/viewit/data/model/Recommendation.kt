package com.linroid.viewit.data.model

import android.annotation.SuppressLint
import com.avos.avoscloud.AVObject

/**
 * @author linroid <linroid@gmail.com>
 * @since 06/02/2017
 */
@SuppressLint("ParcelCreator")
class Recommendation {
    val avObject: AVObject

    constructor(obj: AVObject) {
        avObject = obj
    }

    constructor() {
        avObject = AVObject(CLASS_NAME)
    }

    companion object {
        val CLASS_NAME = "Recommendation"
    }

    fun toAVObject(): AVObject {
        return avObject
    }

    var name: String
        get() = avObject.getString("name")
        set(value) = avObject.put("name", value)

    var packageName: String
        get() = avObject.getString("packageName")
        set(value) = avObject.put("packageName", value)

    var pattern: String
        get() = avObject.getString("pattern")
        set(value) = avObject.put("pattern", value)

    var createdAt: String
        get() = avObject.getString("createdAt")
        set(value) = avObject.put("createdAt", value)

    var tree: ImageTree? = null
}