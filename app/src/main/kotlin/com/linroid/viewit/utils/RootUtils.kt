package com.linroid.viewit.utils

import android.os.Environment
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


/**
 * @author linroid <linroid@gmail.com>
 * @since 08/01/2017
 */
object RootUtils {
    /**
     * 手机是否ROOT
     */
    private val rooted: Boolean by lazy {
        return@lazy checkRootMethod1() || checkRootMethod2() || checkRootMethod3()
    }

    /**
     * 判断手机是否ROOT
     */
    fun isRootAvailable(): Boolean {
        return rooted
    }

    private fun checkRootMethod1(): Boolean {
        val buildTags = android.os.Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkRootMethod2(): Boolean {
        val paths = arrayOf("/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su")
        return paths.any { File(it).exists() }
    }

    private fun checkRootMethod3(): Boolean {
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val line = BufferedReader(InputStreamReader(process!!.inputStream))
            if (line.readLine() != null) return true
            return false
        } catch (t: Throwable) {
            return false
        } finally {
            process?.destroy()
        }
    }


    fun isRootFile(path: String): Boolean {
        return !path.startsWith(Environment.getExternalStorageDirectory().absolutePath)
    }

    fun isRootFile(file: File): Boolean {
        return !file.absolutePath.startsWith(Environment.getExternalStorageDirectory().absolutePath)
    }

}