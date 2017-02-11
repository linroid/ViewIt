package com.linroid.viewit.data.repo.cloud

import android.content.pm.ApplicationInfo
import cn.leancloud.rx.RxLeanCloud
import com.avos.avoscloud.AVObject
import com.avos.avoscloud.AVQuery
import com.linroid.viewit.data.model.CloudScanPath
import com.linroid.viewit.data.model.ScanPath
import rx.Observable
import rx.schedulers.Schedulers
import java.util.*

/**
 * @author linroid <linroid@gmail.com>
 * @since 11/02/2017
 */
class CloudPathRepo {
    fun upload(data: ScanPath, appInfo: ApplicationInfo): Observable<CloudScanPath> {
        val obj = data.toAVObject("ScanPath")
        return RxLeanCloud.save(obj)
                .map { CloudScanPath(obj) }
                .subscribeOn(Schedulers.io())
    }

    fun list(appInfo: ApplicationInfo): Observable<List<CloudScanPath>> {
        val query = AVQuery<AVObject>("CloudScanPath")
                .whereEqualTo("packageName", appInfo.packageName)
        return RxLeanCloud.find(query)
                .map { objs ->
                    val paths = ArrayList<CloudScanPath>()
                    objs.forEach {
                        paths.add(CloudScanPath(it))
                    }
                    return@map paths.toList()
                }.subscribeOn(Schedulers.io())
    }
}