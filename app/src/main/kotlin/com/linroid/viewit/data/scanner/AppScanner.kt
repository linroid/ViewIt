package com.linroid.viewit.data.scanner

import android.content.pm.ApplicationInfo
import com.linroid.viewit.BuildConfig
import rx.Observable
import rx.schedulers.Schedulers

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
object AppScanner {
    fun scan(context: android.content.Context): rx.Observable<ApplicationInfo> {
        val pm = context.packageManager;
        val packages = pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
        return rx.Observable.from(packages)
                .filter { info ->
                    val isSelfApp = info.packageName == BuildConfig.APPLICATION_ID
                    val isSystemApp = (info.flags and ApplicationInfo::FLAG_SYSTEM.get()) != 0
                    !isSelfApp && !isSystemApp
                }
                .subscribeOn(Schedulers.io())
    }
}