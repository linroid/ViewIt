package com.linroid.viewit.data

import android.content.pm.ApplicationInfo
import cn.leancloud.rx.RxLeanCloud
import com.avos.avoscloud.AVObject
import com.avos.avoscloud.AVQuery
import com.linroid.viewit.data.model.Favorite
import com.linroid.viewit.data.model.Recommendation
import com.linroid.viewit.utils.PathUtils
import rx.Observable
import rx.schedulers.Schedulers
import java.util.*

/**
 * @author linroid <linroid@gmail.com>
 * @since 06/02/2017
 */
class NetRepo {

    fun listRecommendations(appInfo: ApplicationInfo): Observable<List<Recommendation>> {
        val query = AVQuery<AVObject>(Recommendation.CLASS_NAME)
                .whereEqualTo("packageName", appInfo.packageName)
        return RxLeanCloud.find(query)
                .map { objs ->
                    val recommends = ArrayList<Recommendation>()
                    objs.forEach {
                        recommends.add(Recommendation(it))
                    }
                    return@map recommends.toList()
                }.subscribeOn(Schedulers.io())
    }

    fun uploadFavorite(favorite: Favorite, appInfo: ApplicationInfo): Observable<Recommendation> {
        val recommendation = Recommendation()
        recommendation.name = favorite.name
        recommendation.pattern = PathUtils.formatToVariable(favorite.path, appInfo)
        recommendation.packageName = favorite.packageName
        return RxLeanCloud.save(recommendation.toAVObject()).map { recommendation }.subscribeOn(Schedulers.io())
    }
}