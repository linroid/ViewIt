package com.linroid.viewit.data.repo.local

import android.content.Context
import android.content.pm.ApplicationInfo
import com.google.gson.Gson
import com.linroid.viewit.data.model.ImageTree
import okio.Okio
import rx.Observable
import timber.log.Timber
import java.io.File

/**
 * @author linroid <linroid@gmail.com>
 * @since 01/03/2017
 */
class ImageTreeCacheRepo(private val gson: Gson, private val context: Context) {
    fun updateOrCrate(appInfo: ApplicationInfo, tree: ImageTree?): Observable<Boolean> {
        Timber.d("updateOrCrate")
        return find(appInfo)
                .map { found ->
                    if (!found.exists()) {
                        found.createNewFile()
                    }
                    val buffer = Okio.buffer(Okio.sink(found))
                    val json = gson.toJson(tree)
                    buffer.writeUtf8(json)
                    buffer.flush()
                    buffer.close()
                    return@map true
                }
    }

    fun findAsImageTree(appInfo: ApplicationInfo): Observable<ImageTree?> {
        Timber.d("findAsImageTree")
        return find(appInfo).map { file ->
            if (file.exists()) {
                val buffer = Okio.buffer(Okio.source(file))
                val json = buffer.readUtf8()
                buffer.close()
                return@map gson.fromJson(json, ImageTree::class.java)
            }
            return@map null
        }
    }

    fun find(appInfo: ApplicationInfo): Observable<File> {
        Timber.d("find")
        return Observable.create<File> { subscriber ->
            val dir = File(context.cacheDir, "scan");
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val fileName = "${appInfo.packageName}.json"
            subscriber.onNext(File(dir, fileName))
            subscriber.onCompleted()
        }
    }
}