package com.linroid.viewit.data.scanner

import com.linroid.viewit.data.model.Image
import rx.Observable
import rx.Subscriber
import rx.schedulers.Schedulers
import timber.log.Timber
import java.io.File

/**
 * @author linroid <linroid@gmail.com>
 * @since 09/01/2017
 */
abstract class ImageScanner {
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

    abstract fun searchImage(file: File, subscriber: Subscriber<in Image>)
}