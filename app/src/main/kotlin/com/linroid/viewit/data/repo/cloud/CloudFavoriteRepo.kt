package com.linroid.viewit.data.repo.cloud

import android.content.pm.ApplicationInfo
import cn.leancloud.rx.RxLeanCloud
import com.avos.avoscloud.AVObject
import com.avos.avoscloud.AVQuery
import com.linroid.viewit.data.model.CloudFavorite
import com.linroid.viewit.data.model.Favorite
import rx.Observable
import rx.schedulers.Schedulers
import java.util.*

/**
 * @author linroid <linroid@gmail.com>
 * @since 06/02/2017
 */
class CloudFavoriteRepo {

    fun list(appInfo: ApplicationInfo): Observable<List<CloudFavorite>> {
        val query = AVQuery<AVObject>("CloudFavorite")
                .whereEqualTo("packageName", appInfo.packageName)
        return RxLeanCloud.find(query)
                .map { objs ->
                    val favorites = ArrayList<CloudFavorite>()
                    objs.forEach {
                        favorites.add(CloudFavorite(it))
                    }
                    return@map favorites.toList()
                }.subscribeOn(Schedulers.io())
    }

    fun upload(data: Favorite, appInfo: ApplicationInfo): Observable<CloudFavorite> {
        val avObj = data.toAVObject("Favorite")
        return RxLeanCloud.save(avObj)
                .map { CloudFavorite(avObj) }
                .subscribeOn(Schedulers.io())
    }
}