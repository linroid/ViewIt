package com.linroid.viewit.data.repo.local

import android.content.pm.ApplicationInfo
import com.linroid.viewit.data.model.Favorite
import com.linroid.viewit.utils.PathUtils
import com.orm.SugarRecord
import rx.Observable
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject

/**
 * @author linroid <linroid@gmail.com>
 * @since 01/02/2017
 */
class FavoriteRepo() {
    private val eventBus: BehaviorSubject<DBEvent> = BehaviorSubject.create(DBEvent.DEFAULT)

    fun find(path: String, appInfo: ApplicationInfo): Observable<Favorite?> {
        return eventBus
                .map {
                    val result = SugarRecord.find(Favorite::class.java,
                            "path = ? and package_name = ? ", path, appInfo.packageName)
                    return@map result.firstOrNull()
                }
                .subscribeOn(Schedulers.io())
    }

    fun listWithChangeObserver(appInfo: ApplicationInfo): Observable<List<Favorite>> {
        return eventBus
                .map {
                    SugarRecord.find(Favorite::class.java,
                            "package_name = ?", appInfo.packageName)
                }
                .subscribeOn(Schedulers.io())
    }

    fun list(appInfo: ApplicationInfo): Observable<List<Favorite>> {
        return Observable.just(appInfo)
                .map {
                    SugarRecord.find(Favorite::class.java,
                            "package_name = ?", appInfo.packageName)
                }
                .subscribeOn(Schedulers.io())
    }

    fun create(appInfo: ApplicationInfo, path: String, name: String): Observable<Favorite> {
        return Observable
                .create<Favorite> {
                    val favorite = Favorite()
                    favorite.name = name
                    favorite.packageName = appInfo.packageName
                    favorite.path = PathUtils.formatToVariable(path, appInfo)
                    SugarRecord.save(favorite)
                    it.onNext(favorite)
                    eventBus.onNext(DBEvent.INSERT)
                    it.onCompleted()
                }
                .subscribeOn(Schedulers.io())
    }

    fun delete(favorite: Favorite, appInfo: ApplicationInfo): Observable<Boolean> {
        return Observable
                .create<Boolean> {
                    val result = SugarRecord.delete(favorite)
                    if (result) {
                        eventBus.onNext(DBEvent.DELETE)
                    }
                    it.onNext(result)
                    it.onCompleted()
                }
                .subscribeOn(Schedulers.io())
    }
}