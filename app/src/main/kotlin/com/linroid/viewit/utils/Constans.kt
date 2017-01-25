
package com.linroid.viewit.utils

import com.linroid.viewit.BuildConfig

/**
 * @author linroid <linroid@gmail.com>
 * @since 21/01/2017
 */
const val BINARY_SEARCH_IMAGE = "search_image"
const val BINARY_DIRECTORY = "exec"
const val FILE_PROVIDER = "${BuildConfig.APPLICATION_ID}.fileProvider"

val APP_EXTERNAL_PATHS = mapOf(
        "com.tencent.mm" to arrayListOf("Tencent/MicroMsg"),
        "com.tencent.mobileqq" to arrayListOf("Tencent/QQ_Favorite", "Tencent/MobileQQ", "Tencent/QQ_Images", "Tencent/QQfile_recv")
)