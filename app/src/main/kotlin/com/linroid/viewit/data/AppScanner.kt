package com.linroid.viewit.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.linroid.viewit.BuildConfig
import com.linroid.viewit.data.model.AppHolder
import rx.Observable
import rx.schedulers.Schedulers

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
object AppScanner {
    fun scan(context: Context): Observable<AppHolder> {
        val pm = context.packageManager;
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return Observable.from(packages).filter { info ->
            val isSelfApp = info.packageName == BuildConfig.APPLICATION_ID
            val isSystemApp = (info.flags and ApplicationInfo::FLAG_SYSTEM.get()) != 0
            !isSelfApp && !isSystemApp
        }.map { info ->
            AppHolder(pm.getApplicationLabel(info), pm.getApplicationIcon(info), info)
        }.subscribeOn(Schedulers.io())
    }
}