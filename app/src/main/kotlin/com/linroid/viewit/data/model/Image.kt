package com.linroid.viewit.data.model

import com.bumptech.glide.load.resource.bitmap.ImageHeaderParser
import java.io.File

/**
 * @author linroid <linroid@gmail.com>
 * @since 08/01/2017
 */
data class Image(val source: File, val path: String, val size: Long, val packageName: String, val type: ImageHeaderParser.ImageType);