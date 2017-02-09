package com.linroid.viewit.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.provider.Settings

/**
 * @author linroid <linroid@gmail.com>
 * @since 10/02/2017
 */
object AndroidNavUtil {
    /**
     * 启动其他应用
     */
    fun launchApp(context: Context, appInfo: ApplicationInfo) {
        val intent = context.packageManager.getLaunchIntentForPackage(appInfo.packageName)
        context.startActivity(intent)
    }

    /**
     * 打开应用信息
     */
    fun openAppDetail(context: Context, appInfo: ApplicationInfo) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:" + appInfo.packageName)
        context.startActivity(intent)
    }
}