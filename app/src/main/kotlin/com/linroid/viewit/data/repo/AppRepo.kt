package com.linroid.viewit.data.repo

import android.content.Context
import android.content.pm.ApplicationInfo
import com.linroid.viewit.BuildConfig
import com.linroid.viewit.data.model.AppUsage
import com.linroid.viewit.data.repo.local.AppUsageRepo
import rx.Observable
import rx.observables.GroupedObservable
import rx.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

/**
 * @author linroid <linroid@gmail.com>
 * @since 16/02/2017
 */
class AppRepo @Inject constructor(val usageRepo: AppUsageRepo, val context: Context) {
//    private fun list(topCount: Int): Observable<List<ApplicationInfo>> {
//        return usageRepo.listTop(topCount)
//                .map { topApps ->
//                    val map = HashMap<String, AppUsage>()
//                    topApps.forEach {
//                        map.put(it.packageName, it)
//                    }
//                    return@map map
//                }
//                .flatMap { topApps ->
//                    scan(context)
//                            .sorted { info1, info2 ->
//                                val usage1 = topApps[info1.packageName]
//                                val usage2 = topApps[info2.packageName]
//                                if (usage1 == null && usage2 == null) {
//                                    val label1 = context.packageManager.getApplicationLabel(info1).toString()
//                                    val label2 = context.packageManager.getApplicationLabel(info2).toString()
//                                    return@sorted label1.compareTo(label2)
//                                } else if (usage1 == null) {
//                                    return@sorted 1
//                                } else if (usage2 == null) {
//                                    return@sorted -1
//                                }
//                                return@sorted usage2.count - usage1.count
//                            }.toList()
//                }
//    }

    //    Observable<List<ApplicationInfo>>
    var scannedApps: List<ApplicationInfo>? = null;

    fun list(count: Int): Observable<GroupedObservable<Boolean, ApplicationInfo>> {
        return usageRepo.listTop(count).map { topApps ->
            val map = HashMap<String, AppUsage>()
            topApps.forEach {
                map.put(it.packageName, it)
            }
            return@map map
        }.flatMap { topApps ->
            return@flatMap scan(context)
                    .sorted { info1, info2 ->
                        val usage1 = topApps[info1.packageName]
                        val usage2 = topApps[info2.packageName]
                        if (usage1 == null && usage2 == null) {
                            val label1 = context.packageManager.getApplicationLabel(info1).toString()
                            val label2 = context.packageManager.getApplicationLabel(info2).toString()
                            return@sorted label1.compareTo(label2)
                        } else if (usage1 == null) {
                            return@sorted 1
                        } else if (usage2 == null) {
                            return@sorted -1
                        }
                        return@sorted usage2.count - usage1.count
                    }
                    .groupBy { topApps.containsKey(it.packageName) }
        }
    }

    private fun scan(context: Context): Observable<ApplicationInfo> {
        return Observable
                .create<List<ApplicationInfo>> { subscriber ->
                    val pm = context.packageManager;
                    val packages = pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA).filter { info ->
                        val isSelfApp = info.packageName == BuildConfig.APPLICATION_ID
                        val isSystemApp = (info.flags and ApplicationInfo::FLAG_SYSTEM.get()) != 0
                        !isSelfApp && !isSystemApp
                    }
                    scannedApps = packages
                    subscriber.onNext(packages)
                    subscriber.onCompleted()
                }
                .flatMap {
                    Observable.from(it)
                }
                .subscribeOn(Schedulers.io())
    }
}