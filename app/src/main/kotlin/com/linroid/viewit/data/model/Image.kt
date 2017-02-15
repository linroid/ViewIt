package com.linroid.viewit.data.model

import com.linroid.viewit.utils.RootUtils
import paperparcel.PaperParcel
import paperparcel.PaperParcelable
import java.io.File

/**
 * @author linroid <linroid@gmail.com>
 * @since 08/01/2017
 */
@PaperParcel
data class Image(
        val source: File,
        val size: Long,
        val lastModified: Long,
        val type: ImageType,
        var mountFile: File? = null) : PaperParcelable {

    companion object {
        @JvmField val CREATOR = PaperParcelImage.CREATOR
    }

    val path: String
        get() = source.absolutePath

    fun mimeType() = type.mime
    fun file(): File? {
        if (mountFile != null) {
            return mountFile
        }
        if (RootUtils.isRootFile(source)) {
            return mountFile
        }
        return source
    }

    fun postfix(): String = type.postfix

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other !is Image) {
            return false
        }
        return source == other.source
    }
}