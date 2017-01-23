package com.linroid.viewit.data.scanner

import com.linroid.viewit.data.model.AppInfo
import rx.Observable
import rx.schedulers.Schedulers

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
object AppScanner {
    fun scan(context: android.content.Context): rx.Observable<com.linroid.viewit.data.model.AppInfo> {
        val pm = context.packageManager;
        val packages = pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
        return rx.Observable.from(packages)
                .filter { info ->
                    val isSelfApp = info.packageName == com.linroid.viewit.BuildConfig.APPLICATION_ID
                    val isSystemApp = (info.flags and android.content.pm.ApplicationInfo::FLAG_SYSTEM.get()) != 0
                    !isSelfApp && !isSystemApp
                }
                .map { info -> AppInfo(pm.getApplicationLabel(info), pm.getApplicationIcon(info), info) }
                .subscribeOn(Schedulers.io())
    }
}