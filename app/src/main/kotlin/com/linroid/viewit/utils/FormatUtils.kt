package com.linroid.viewit.utils

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
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
        if (path == File.separator) {
            return "根目录"
        }
        val context = App.get()
        val packInfo: PackageInfo = context.packageManager.getPackageInfo(appInfo.packageName, 0)
        val internalDataDir = packInfo.applicationInfo.dataDir
        val externalDataDir = PathUtils.append(context.externalCacheDir.parentFile.parent, appInfo.packageName)
        val sdcardDir = Environment.getExternalStorageDirectory().absolutePath

        if (path.startsWith(internalDataDir)) {
//            return "内部数据" + path.substringAfter(internalDataDir)
            val ssb = SpannableStringBuilder(path)
            val icon = ContextCompat.getDrawable(context, R.drawable.ic_sd_card)
            icon.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
            ssb.setSpan(ImageSpan(icon), 0, internalDataDir.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            return ssb
        } else if (path.startsWith(externalDataDir)) {
//            return "外部数据" + path.substringAfter(externalDataDir)
            val ssb = SpannableStringBuilder(path)
            val icon = ContextCompat.getDrawable(context, R.drawable.ic_sd_card)
            icon.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
            ssb.setSpan(ImageSpan(icon), 0, externalDataDir.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            return ssb
        } else if (path.startsWith(sdcardDir)) {
            val ssb = SpannableStringBuilder(path)
            val icon = ContextCompat.getDrawable(context, R.drawable.ic_sd_card)
            icon.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
            ssb.setSpan(ImageSpan(icon), 0, sdcardDir.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            return ssb
        }
        return path
    }
}