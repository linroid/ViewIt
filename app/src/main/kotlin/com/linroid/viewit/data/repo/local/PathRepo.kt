package com.linroid.viewit.data.repo.local

import android.content.pm.ApplicationInfo
import com.linroid.viewit.data.model.ScanPath
import com.linroid.viewit.utils.PathUtils
import com.orm.SugarRecord
import rx.Observable
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject

/**
 * @author linroid <linroid@gmail.com>
 * @since 10/02/2017
 */
class PathRepo {
    private val eventBus: BehaviorSubject<DBEvent> = BehaviorSubject.create(DBEvent.DEFAULT)

    fun find(path: String, appInfo: ApplicationInfo): Observable<ScanPath?> {
        return eventBus
                .map {
                    val result = SugarRecord.find(ScanPath::class.java,
                            "path = ? and package_name = ? ", path, appInfo.packageName)
                    return@map result.firstOrNull()
                }
                .subscribeOn(Schedulers.io())
    }

    fun listWithChangObserver(appInfo: ApplicationInfo): Observable<List<ScanPath>> {
        return eventBus
                .map {
                    SugarRecord.find(ScanPath::class.java,
                            "package_name = ?", appInfo.packageName)
                }
                .subscribeOn(Schedulers.io())
    }

    fun list(appInfo: ApplicationInfo): Observable<List<ScanPath>> {
        return Observable.just(appInfo)
                .map {
                    SugarRecord.find(ScanPath::class.java,
                            "package_name = ?", it.packageName)
                }
                .subscribeOn(Schedulers.io())
    }

    fun create(appInfo: ApplicationInfo, path: String): Observable<ScanPath> {
        return Observable
                .create<ScanPath> {
                    val scanPath = ScanPath()
                    scanPath.packageName = appInfo.packageName
                    scanPath.path = PathUtils.formatToVariable(path, appInfo)
                    SugarRecord.save(scanPath)
                    eventBus.onNext(DBEvent.INSERT)
                    it.onNext(scanPath)
                    it.onCompleted()
                }
                .subscribeOn(Schedulers.io())
    }

    fun delete(scanPath: ScanPath): Observable<Boolean> {
        return Observable
                .create<Boolean> {
                    val result = SugarRecord.delete(scanPath)
                    if (result) {
                        eventBus.onNext(DBEvent.DELETE)
                    }
                    it.onNext(result)
                    it.onCompleted()
                }
                .subscribeOn(Schedulers.io())
    }
}