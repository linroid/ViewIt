package com.linroid.viewit.data.repo.local

import android.content.pm.ApplicationInfo
import com.linroid.viewit.data.model.ScanPath
import com.linroid.viewit.utils.PathUtils
import com.orm.SugarRecord
import rx.Observable
import rx.schedulers.Schedulers

/**
 * @author linroid <linroid@gmail.com>
 * @since 10/02/2017
 */
class PathRepo {
    fun find(path: String, appInfo: ApplicationInfo): Observable<ScanPath> {
        return Observable
                .create<List<ScanPath>> {
                    val result = SugarRecord.find(ScanPath::class.java,
                            "path = ? and package_name = ? ", path, appInfo.packageName)
                    it.onNext(result)
                    it.onCompleted()
                }.flatMap { Observable.from(it) }
                .take(1)
                .subscribeOn(Schedulers.io())
    }

    fun list(appInfo: ApplicationInfo): Observable<List<ScanPath>> {
        return Observable
                .create<List<ScanPath>> {
                    val result = SugarRecord.find(ScanPath::class.java,
                            "package_name = ?", appInfo.packageName)
                    it.onNext(result)
                    it.onCompleted()
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
                    it.onNext(scanPath)
                    it.onCompleted()
                }
                .subscribeOn(Schedulers.io())
    }

    fun delete(scanPath: ScanPath): Observable<Boolean> {
        return Observable
                .create<Boolean> {
                    val result = SugarRecord.delete(scanPath)
                    it.onNext(result)
                    it.onCompleted()
                }
                .subscribeOn(Schedulers.io())
    }
}