package com.linroid.viewit.data.model

import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable

/**
 * @author linroid <linroid@gmail.com>
 * *
 * @since 07/01/2017
 */
data class AppInfo(val label: CharSequence, val icon: Drawable, val info: ApplicationInfo)
