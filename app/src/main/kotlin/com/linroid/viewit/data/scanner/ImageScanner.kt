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
    fun scan(packageName: String, dirs: List<File>): Observable<Image> {
        return Observable.create<Image> { subscriber ->
            try {
                dirs.forEach {
                    Timber.d("search image at directory: ${it.absolutePath}")
                    searchImage(packageName, it, subscriber)
                }
            } catch (error: Exception) {
                Timber.e(error, "error occur during search image...")
                return@create
            }
            subscriber.onCompleted()
        }.subscribeOn(Schedulers.io())
    }

    fun scan(packageName: String, vararg dirs: File): Observable<Image> {
        return scan(packageName, dirs.toList())
    }

    abstract fun searchImage(packageName: String, file: File, subscriber: Subscriber<in Image>)
}