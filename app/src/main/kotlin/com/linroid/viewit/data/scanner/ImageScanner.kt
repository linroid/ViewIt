package com.linroid.viewit.data.scanner

import com.bumptech.glide.load.resource.bitmap.ImageHeaderParser
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.utils.ImageMIME
import rx.Observable
import rx.Subscriber
import rx.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.util.*

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
object ImageScanner {

    fun scan(dirs: List<File>): Observable<Image> {
        return Observable.create<Image> { subscriber ->
            try {
                dirs.forEach {
                    Timber.d("search image at directory: ${it.absolutePath}")
                    searchImage(it, subscriber)
                }
            } catch (error: Exception) {
                Timber.e(error, "error occur during search image...")
                return@create
            }
            subscriber.onCompleted()
        }.subscribeOn(Schedulers.io())
    }

    fun scan(vararg dirs: File): Observable<Image> {
        return scan(dirs.toList())
    }

    private fun searchImage(file: File?, subscriber: Subscriber<in Image>) {
        if (file == null) {
            return;
        }
        if (!file.exists()) {
            return;
        }
        if (file.isFile) {
            val type = ImageMIME.getImageType(file)
            if (type != ImageHeaderParser.ImageType.UNKNOWN) {
                val image = Image(file, file.length(), type)
                subscriber.onNext(image);
            }
        } else if (file.isDirectory) {
            Timber.d("directory : ${file.absolutePath}")
            file.listFiles()?.forEach {
                searchImage(it, subscriber)
            }
        }
    }
}