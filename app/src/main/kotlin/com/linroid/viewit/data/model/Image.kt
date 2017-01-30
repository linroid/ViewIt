package com.linroid.viewit.data.model

import java.io.File

/**
 * @author linroid <linroid@gmail.com>
 * @since 08/01/2017
 */
data class Image(val source: File, val size: Long, val lastModified: Long, val type: ImageType, var mountFile: File? = null) {
    val path: String
        get() = source.absolutePath

    fun mimeType() = type.mime
    fun file(): File = mountFile ?: source
    fun postfix(): String = type.postfix
}