package com.linroid.viewit.data.model

import com.bumptech.glide.load.resource.bitmap.ImageHeaderParser

/**
 * @author linroid <linroid></linroid>@gmail.com>
 * *
 * @since 23/01/2017
 */
enum class ImageType private constructor(private val `val`: Int) {
    /**
     * GIF type.
     */
    GIF(0),

    /**
     * JPG type.
     */
    JPEG(1),

    /**
     * PNG type with alpha.
     */
    PNG_A(2),

    /**
     * PNG type without alpha.
     */
    PNG(3),

    /**
     * Unrecognized type.
     */
    UNKNOWN(-1);


    companion object {

        fun from(typeVal: Int): ImageType {
            when (typeVal) {
                0 -> return GIF
                1 -> return JPEG
                2 -> return PNG_A
                3 -> return PNG
                else -> {
                    return UNKNOWN
                }
            }
        }

        fun from(type: ImageHeaderParser.ImageType): ImageType {
            when (type) {
                ImageHeaderParser.ImageType.GIF -> return GIF
                ImageHeaderParser.ImageType.JPEG -> return JPEG
                ImageHeaderParser.ImageType.PNG_A -> return PNG_A
                ImageHeaderParser.ImageType.PNG -> return PNG
                else -> {
                    return UNKNOWN
                }
            }
        }
    }
}
