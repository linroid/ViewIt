package com.linroid.viewit.data

import android.content.pm.ApplicationInfo
import com.linroid.viewit.data.model.Favorite
import io.realm.Realm
import rx.Observable

/**
 * @author linroid <linroid@gmail.com>
 * @since 01/02/2017
 */
class FavoriteRepo(val realm: Realm) {
    fun findFavorites(appInfo: ApplicationInfo): Observable<List<Favorite>> {
        return realm.where(Favorite::class.java).equalTo("packageName", appInfo.packageName)
                .findAllAsync()
                .asObservable()
                .map {
                    it.toList()
                }
    }

    fun createFavorite(appInfo: ApplicationInfo, pathPattern: String, name: String): Observable<Favorite> {
        return realm.asObservable().map {
            val nextID = realm.where(Favorite::class.java).max("id").toLong() + 1
            val favorite = realm.createObject(Favorite::class.java, nextID)
            favorite.packageName = appInfo.packageName
            favorite.name = name
            favorite.pathPattern = pathPattern
            realm.copyToRealm(favorite)
            return@map favorite
        }
    }

    fun asyncCreateFavorite(appInfo: ApplicationInfo, pathPattern: String, name: String) {
        realm.executeTransaction {
            val maxID = realm.where(Favorite::class.java).max("id")?.toLong() ?: 0
            val nextID = maxID + 1
            val favorite = realm.createObject(Favorite::class.java, nextID)
            favorite.name = name
            favorite.pathPattern = pathPattern
            favorite.packageName = appInfo.packageName
        }
    }
}