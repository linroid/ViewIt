package com.linroid.viewit.data.repo.local

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.SystemClock
import com.google.gson.Gson
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.model.ImageTree
import com.linroid.viewit.data.model.ImageType
import okio.BufferedSink
import okio.BufferedSource
import okio.Okio
import rx.Observable
import rx.schedulers.Schedulers
import timber.log.Timber
import java.io.EOFException
import java.io.File

/**
 * @author linroid <linroid@gmail.com>
 * @since 01/03/2017
 */
class ImageTreeCacheRepo(private val gson: Gson, private val context: Context) {
    fun updateOrCrate(appInfo: ApplicationInfo, tree: ImageTree?): Observable<Boolean> {
        Timber.d("updateOrCrate")
        if (tree == null) {
            return Observable.just(false)
        }
        return find(appInfo)
                .map { found ->
                    if (!found.exists()) {
                        found.createNewFile()
                    }
                    val startTime = SystemClock.uptimeMillis()
                    val buffer = Okio.buffer(Okio.sink(found))
                    storeTree(buffer, tree)
                    buffer.flush()
                    buffer.close()
                    Timber.w("写入耗时:${SystemClock.uptimeMillis() - startTime}")
                    return@map true
                }
    }

    fun findAsImageTree(appInfo: ApplicationInfo): Observable<ImageTree?> {
        Timber.d("findAsImageTree")
        return find(appInfo).map { file ->
            if (file.exists()) {
                val startTime = SystemClock.uptimeMillis()
                val buffer = Okio.buffer(Okio.source(file))
                val tree = restoreTree(buffer)
                buffer.close()
                Timber.w("读取耗时:${SystemClock.uptimeMillis() - startTime}")
                return@map tree
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
            val fileName = "${appInfo.packageName}.dat"
            subscriber.onNext(File(dir, fileName))
            subscriber.onCompleted()
        }.subscribeOn(Schedulers.io())
    }

    private fun storeTree(sink: BufferedSink, tree: ImageTree) {
        writeUTF8(sink, tree.dir)
        sink.writeInt(tree.images.size)
        tree.images.forEach { image ->
            writeUTF8(sink, image.path)
            sink.writeLong(image.size)
            sink.writeLong(image.lastModified)
            sink.writeInt(image.type.value)
        }

        sink.writeInt(tree.children.size)
        for ((subDir, child) in tree.children) {
            writeUTF8(sink, subDir)
            storeTree(sink, child)
        }
    }

    fun restoreTree(source: BufferedSource): ImageTree {
        val dir = readUTF8(source)
        val tree = ImageTree(dir)
        val imageSize = source.readInt()
        for (index in 0 until imageSize) {
            val image = Image(
                    source = File(readUTF8(source)),
                    size = source.readLong(),
                    lastModified = source.readLong(),
                    type = ImageType.from(source.readInt())
            )
            tree.images.add(image)
        }
        val childCount = source.readInt()
        for (index in 0 until childCount) {
            val subDir = readUTF8(source)
            val child = restoreTree(source)
            tree.children.put(subDir, child)
        }
        return tree
    }

    private fun writeUTF8(sink: BufferedSink, content: String) {
        sink.writeInt(content.toByteArray().size)
        sink.writeUtf8(content)
    }

    private fun readUTF8(source: BufferedSource): String {
        val length = source.readInt()
        if (length > 0) {
            try {
                return source.readUtf8(length.toLong())
            } catch (error: EOFException) {
                error.printStackTrace()
            }
        }
        return ""
    }
}