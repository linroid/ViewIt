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
    abstract fun scan(packageName: String, dirs: List<File>): Observable<Image>;

    fun scan(packageName: String, vararg dirs: File): Observable<Image> {
        return scan(packageName, dirs.toList())
    }
}