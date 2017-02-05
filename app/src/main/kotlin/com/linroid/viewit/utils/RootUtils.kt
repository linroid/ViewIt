package com.linroid.viewit.utils

import android.content.Context
import android.os.Environment
import timber.log.Timber
import java.io.File

/**
 * @author linroid <linroid@gmail.com>
 * @since 08/01/2017
 */
object RootUtils {
    /**
     * 判断手机是否ROOT
     */
    fun isRootAvailable(): Boolean {
        try {
            return !(!File("/system/bin/su").exists() && !File("/system/xbin/su").exists())
        } catch (e: Exception) {
            Timber.e(e, "isRootAvailable")
            return false;
        }
    }

    fun isRootFile(path: String): Boolean {
        return !path.startsWith(Environment.getExternalStorageDirectory().absolutePath)
    }

    fun isRootFile(file: File): Boolean {
        return !file.absolutePath.startsWith(Environment.getExternalStorageDirectory().absolutePath)
    }

}