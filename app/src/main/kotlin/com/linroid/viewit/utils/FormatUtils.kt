package com.linroid.viewit.utils

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.graphics.Typeface
import android.os.Environment
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import com.linroid.viewit.App
import com.linroid.viewit.R
import java.io.File

/**
 * @author linroid <linroid@gmail.com>
 * @since 31/01/2017
 */
object FormatUtils {
    fun formatPath(path: String?, appInfo: ApplicationInfo): CharSequence {
        if (path == null) {
            return ""
        }
        val context = App.get()

        if (path == File.separator) {
            return context.getString(R.string.path_format_root)
        }
        val packInfo: PackageInfo = context.packageManager.getPackageInfo(appInfo.packageName, 0)
        val internalDataDir = packInfo.applicationInfo.dataDir
        val externalDataDir = PathUtils.append(context.externalCacheDir.parentFile.parent, appInfo.packageName)
        val sdcardDir = Environment.getExternalStorageDirectory().absolutePath

        if (path.startsWith(internalDataDir)) {
            val ssb = SpannableStringBuilder(context.getString(R.string.path_format_internal_data))
            ssb.setSpan(StyleSpan(Typeface.BOLD), 0, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            ssb.append(path.substringAfter(internalDataDir))
            return ssb
        } else if (path.startsWith(externalDataDir)) {
            val ssb = SpannableStringBuilder(context.getString(R.string.path_format_external_data))
            ssb.setSpan(StyleSpan(Typeface.BOLD), 0, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            ssb.append(path.substringAfter(externalDataDir))
            return ssb
        } else if (path.startsWith(sdcardDir)) {
            val ssb = SpannableStringBuilder(context.getString(R.string.path_format_sdcard))
            ssb.setSpan(StyleSpan(Typeface.BOLD), 0, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            ssb.append(path.substringAfter(sdcardDir))
            return ssb
        }
        return path
    }
}