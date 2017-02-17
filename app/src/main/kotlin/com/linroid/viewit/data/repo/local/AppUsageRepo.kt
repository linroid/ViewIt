package com.linroid.viewit.data.repo.local

import android.content.pm.ApplicationInfo
import com.linroid.viewit.data.model.AppUsage
import com.orm.SugarRecord
import com.orm.util.NamingHelper
import rx.Observable
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject

/**
 * @author linroid <linroid@gmail.com>
 * @since 16/02/2017
 */
class AppUsageRepo {
    private val eventBus: BehaviorSubject<DBEvent> = BehaviorSubject.create(DBEvent.DEFAULT)

    fun findOrCreate(appInfo: ApplicationInfo): Observable<AppUsage> {
        return find(appInfo)
                .map { found ->
                    if (found != null) {
                        return@map found
                    }
                    val usage = AppUsage()
                    usage.packageName = appInfo.packageName
                    SugarRecord.save(usage)
                    return@map usage
                }
    }

    fun find(appInfo: ApplicationInfo): Observable<AppUsage?> {
        return Observable.just(appInfo)
                .map {
                    val result = SugarRecord.find(AppUsage::class.java,
                            "package_name = ? ", appInfo.packageName)
                    return@map result.firstOrNull()
                }
                .subscribeOn(Schedulers.io())
    }

    fun listTop(count: Int): Observable<List<AppUsage>> {
        return Observable.just(count)
                .map {
                    SugarRecord.findWithQuery(AppUsage::class.java,
                            "SELECT * FROM ${NamingHelper.toSQLName(AppUsage::class.java)} ORDER BY COUNT DESC LIMIT $count")
                }
                .subscribeOn(Schedulers.io())
    }

    fun visitApp(appInfo: ApplicationInfo): Observable<AppUsage> {
        return findOrCreate(appInfo).map {
            it.count++
            SugarRecord.update(it)
            eventBus.onNext(DBEvent.UPDATE)
            return@map it
        }
    }
}