package com.linroid.viewit.data.repo.local

import android.content.pm.ApplicationInfo
import com.linroid.viewit.data.model.Favorite
import com.orm.SugarRecord
import rx.Observable
import rx.schedulers.Schedulers

/**
 * @author linroid <linroid@gmail.com>
 * @since 01/02/2017
 */
class FavoriteRepo() {

    fun find(path: String, appInfo: ApplicationInfo): Observable<Favorite> {
        return Observable
                .create<List<Favorite>> {
                    val result = SugarRecord.find(Favorite::class.java,
                            "path = ? and package_name = ? ", path, appInfo.packageName)
                    it.onNext(result)
                    it.onCompleted()
                }.flatMap { Observable.from(it) }
                .take(1)
                .subscribeOn(Schedulers.io())
    }

    fun list(appInfo: ApplicationInfo): Observable<List<Favorite>> {
        return Observable
                .create<List<Favorite>> {
                    val result = SugarRecord.find(Favorite::class.java,
                            "package_name = ?", appInfo.packageName)
                    it.onNext(result)
                    it.onCompleted()
                }
                .subscribeOn(Schedulers.io())
    }

    fun create(appInfo: ApplicationInfo, path: String, name: String): Observable<Favorite> {
        return Observable
                .create<Favorite> {
                    val favorite = Favorite()
                    favorite.name = name
                    favorite.packageName = appInfo.packageName
                    favorite.path = path
                    SugarRecord.save(favorite)
                    it.onNext(favorite)
                    it.onCompleted()
                }
                .subscribeOn(Schedulers.io())
    }

    fun delete(favorite: Favorite, appInfo: ApplicationInfo): Observable<Boolean> {
        return Observable
                .create<Boolean> {
                    val result = SugarRecord.delete(favorite)
                    it.onNext(result)
                    it.onCompleted()
                }
                .subscribeOn(Schedulers.io())
    }
}