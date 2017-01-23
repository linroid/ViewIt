package com.linroid.viewit.utils

import com.bumptech.glide.load.resource.bitmap.ImageHeaderParser
import com.linroid.viewit.data.model.ImageType
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * @author linroid <linroid@gmail.com>
 * @since 08/01/2017
 */
object ImageMIME {
    fun isImage(file: File): Boolean = isImage(FileInputStream(file))
    fun isImage(inputStream: InputStream): Boolean {
        try {
            val parser = ImageHeaderParser(inputStream)
            return parser.type != ImageHeaderParser.ImageType.UNKNOWN

//            val isFile = isValidPNG(inputStream) || isValidJPEG(inputStream, source.length())
//            inputStream.close()
//            return isFile
        } catch (error: Exception) {
            Timber.e(error, "failed to read source")
            return false
        } finally {
            inputStream.close()
        }
    }

    fun getImageType(file: File): ImageType = getImageType(FileInputStream(file))
    fun getImageType(inputStream: InputStream): ImageType {
        var type = ImageHeaderParser.ImageType.UNKNOWN
        try {
            val parser = ImageHeaderParser(inputStream)
            type = parser.type
        } catch (error: Exception) {
            Timber.e(error, "failed to read source")
        } finally {
            inputStream.close()
        }
        return ImageType.from(type)
    }

}