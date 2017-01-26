package com.linroid.viewit.data.model

import java.io.File

/**
 * @author linroid <linroid@gmail.com>
 * @since 08/01/2017
 */
data class Image(val path: String, val size: Long, val lastModified: Long, val type: ImageType, var mountPath: String = "") {
    fun mimeType() = type.mime
    fun file(): File = File(if (mountPath.isNullOrEmpty().not()) mountPath else path)
    fun postfix(): String = type.postfix
}