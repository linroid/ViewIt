package com.linroid.viewit.utils

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Environment
import com.linroid.viewit.App
import com.linroid.viewit.R
import java.io.File

/**
 * @author linroid <linroid@gmail.com>
 * @since 30/01/2017
 */
object PathUtils {
    /**
     * 给路径添加一个子路径
     */
    fun append(dir: String, sub: String): String {
        if (dir.endsWith(File.separator)) {
            return dir + sub
        } else {
            return dir + File.separator + sub
        }
    }

    fun parent(path: String): String {
        return path.substringBeforeLast(File.separator)
    }

    fun relative(parent: String, path: String): String {
        if (parent == File.separator) {
            return path
        }
        val p = path.substringAfter(parent)
        if (p.startsWith(File.separatorChar)) {
            return p.substringAfter(File.separator)
        }
        return p
    }

    fun name(path: String): String {
        if (path.endsWith(File.separator)) {
            return path.substringBeforeLast(File.separator).substringAfterLast(File.separator)
        } else {
            return path.substringAfterLast(File.separator)
        }
    }

    fun formatToVariable(path: String, appInfo: ApplicationInfo): String {
        val context = App.get()

        val packInfo: PackageInfo = context.packageManager.getPackageInfo(appInfo.packageName, 0)
        val internalDataDir = packInfo.applicationInfo.dataDir
        val externalDataDir = PathUtils.append(context.externalCacheDir.parentFile.parent, appInfo.packageName)
        val sdcardDir = Environment.getExternalStorageDirectory().absolutePath

        if (path.startsWith(internalDataDir)) {
            return path.replaceFirst(internalDataDir, INTERNAL_DATA_DIR)
        } else if (path.startsWith(externalDataDir)) {
            return path.replaceFirst(externalDataDir, EXTERNAL_DATA_DIR)
        } else if (path.startsWith(sdcardDir)) {
            return path.replaceFirst(sdcardDir, SDCARD_DIR)
        }
        return path
    }

    /**
     * 格式化为设备的绝对路径
     */
    fun formatToDevice(path: String, appInfo: ApplicationInfo): String {
        val context = App.get()
        if (path == File.separator) {
            return context.getString(R.string.path_format_root)
        }
        val packInfo: PackageInfo = context.packageManager.getPackageInfo(appInfo.packageName, 0)
        if (path.startsWith(INTERNAL_DATA_DIR)) {
            val internalDataDir = packInfo.applicationInfo.dataDir
            return path.replace(INTERNAL_DATA_DIR, internalDataDir)
        } else if (path.startsWith(EXTERNAL_DATA_DIR)) {
            val externalDataDir = PathUtils.append(context.externalCacheDir.parentFile.parent, appInfo.packageName)
            return path.replace(EXTERNAL_DATA_DIR, externalDataDir)
        } else if (path.startsWith(SDCARD_DIR)) {
            val sdcardDir = Environment.getExternalStorageDirectory().absolutePath
            return path.replace(SDCARD_DIR, sdcardDir)
        }
        return path
    }
}