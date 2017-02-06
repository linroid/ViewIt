package com.linroid.viewit.data

import android.content.pm.ApplicationInfo
import com.linroid.viewit.data.model.Favorite
import com.linroid.viewit.utils.PathUtils
import io.realm.Realm
import rx.Observable

/**
 * @author linroid <linroid@gmail.com>
 * @since 01/02/2017
 */
class FavoriteRepo(val realm: Realm) {

    fun find(path: String, appInfo: ApplicationInfo): Observable<Favorite> {
        return realm.where(Favorite::class.java).equalTo("path", PathUtils.formatToVariable(path, appInfo))
                .findFirstAsync()
                .asObservable<Favorite>()
                .filter { it.isLoaded }
    }

    fun list(appInfo: ApplicationInfo): Observable<List<Favorite>> {
        return realm.where(Favorite::class.java).equalTo("packageName", appInfo.packageName)
                .findAllAsync()
                .asObservable()
                .filter { it.isLoaded }
                .map {
                    it.toList()
                }
    }

    fun create(appInfo: ApplicationInfo, path: String, name: String) {
        realm.executeTransaction {
            val maxID = realm.where(Favorite::class.java).max("id")?.toLong() ?: 0
            val nextID = maxID + 1
            val favorite = realm.createObject(Favorite::class.java, nextID)
            favorite.name = name
            favorite.path = path
            favorite.packageName = appInfo.packageName
        }
    }

    fun delete(path: String, appInfo: ApplicationInfo) {
        realm.executeTransaction {
            realm.where(Favorite::class.java)
                    .equalTo("path", PathUtils.formatToVariable(path, appInfo))
                    .findFirst().deleteFromRealm()
        }
    }
}