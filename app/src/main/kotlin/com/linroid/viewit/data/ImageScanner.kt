package com.linroid.viewit.data

import okio.ByteString
import okio.Okio
import rx.Observable
import rx.Subscriber
import rx.schedulers.Schedulers
import timber.log.Timber
import java.io.File

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
val PNG_HEADER: ByteString = ByteString.decodeHex("89504e470d0a1a0a");

object ImageScanner {
    fun scan(dir: File): Observable<File> {
        return Observable.create<File> { subscriber ->
            try {
                searchImage(dir, subscriber)
            } catch (error: Exception) {
                Timber.e(error, "error occur during search image...")
                return@create
            }
            subscriber.onCompleted()
        }.subscribeOn(Schedulers.io())
    }

    private fun searchImage(file: File?, subscriber: Subscriber<in File>) {
        if (file == null) {
            return;
        }
        if (!file.exists()) {
            return;
        }
        if (file.isFile) {
            if (isImage(file)) {
                subscriber.onNext(file);
            }
        } else if (file.isDirectory) {
            file.listFiles().forEach {
                searchImage(it, subscriber)
            }
        }
    }

    private fun isImage(file: File): Boolean {
        val pngSource = Okio.buffer(Okio.source(file))
        try {
            val header = pngSource.readByteString(PNG_HEADER.size().toLong());
            return header == PNG_HEADER
        } catch (error: Exception) {
            Timber.e(error)
            return false;
        } finally {
            pngSource.close()
        }
    }
}