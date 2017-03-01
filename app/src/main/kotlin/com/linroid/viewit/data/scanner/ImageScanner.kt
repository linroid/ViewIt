package com.linroid.viewit.data.scanner

import android.content.Context
import com.linroid.rxshell.RxShell
import com.linroid.viewit.App
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.model.ImageType
import com.linroid.viewit.ioc.quailifer.Root
import com.linroid.viewit.utils.BINARY_SEARCH_IMAGE
import com.linroid.viewit.utils.ImageMIME
import com.linroid.viewit.utils.RootUtils
import rx.Observable
import rx.Subscriber
import rx.lang.kotlin.filterNotNull
import rx.lang.kotlin.onErrorReturnNull
import rx.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject

/**
 * @author linroid <linroid@gmail.com>
 * @since 09/01/2017
 */
class ImageScanner @Inject constructor(private @Root val rootShell: RxShell,
                                       private val shell: RxShell,
                                       private val context: Context) {

    fun scan(packageName: String, dirs: List<File>): Observable<Image> {
        val observables = ArrayList<Observable<Image>>(dirs.size)
        dirs.forEach { file ->
            observables.add(scan(packageName, file))
        }
        return Observable.merge(observables)
    }

    fun scan(packageName: String, vararg dirs: File): Observable<Image> {
        return scan(packageName, dirs.toList())
    }

    fun scan(packageName: String, dir: File): Observable<Image> {
        val result: Observable<Image>
        if (RootUtils.isRootFile(dir)) {
            result = scanInternalDir(packageName, dir)
        } else {
            result = scanExternalDir(packageName, dir)
        }
        return result.doOnError { error -> Timber.e(error, "scan ${dir.absolutePath} image failed") }
                .onErrorReturnNull().filterNotNull()
    }

    private fun scanInternalDir(packageName: String, dir: File): Observable<Image> {
        return scanByBinary(rootShell, packageName, dir)
    }

    private fun scanExternalDir(packageName: String, dir: File): Observable<Image> {
//        if (shell.binaryExists(context, BINARY_SEARCH_IMAGE)) {
//            return scanByBinary(shell, packageName, dir)
//        }
        return scanByJava(packageName, dir)
    }

    private fun scanByBinary(rxShell: RxShell, packageName: String, dir: File): Observable<Image> {
        return rxShell.execBinary(App.get(), BINARY_SEARCH_IMAGE, dir.absolutePath)
                .filter { line -> line != null && line.split(" ").size == 4 }
                .map { line ->
                    val parser = line.split(" ")
                    val size = parser[0].toLong()
                    val lastModified = parser[1].toLong()
                    val typeVal = parser[2].toInt()
                    val type: ImageType = ImageType.from(typeVal)
                    val path = parser[3]
                    Image(File(path), size, lastModified, type)
                }
                .filter { image -> image.type != ImageType.UNKNOWN }
    }

    private fun scanByJava(packageName: String, dir: File): Observable<Image> {
        return Observable.create<Image> { subscriber ->
            try {
                searchImageByJava(packageName, dir, subscriber)
            } catch (error: Exception) {
                Timber.e(error, "error occur during search image...")
                return@create
            }
            subscriber.onCompleted()
        }.subscribeOn(Schedulers.io())
    }


    private fun searchImageByJava(packageName: String, file: File, subscriber: Subscriber<in Image>) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile) {
            val type = ImageMIME.getImageType(file)
            if (type != ImageType.UNKNOWN) {
                val image = Image(file, file.length(), file.lastModified(), type)
                subscriber.onNext(image);
            }
        } else if (file.isDirectory) {
            file.listFiles()?.forEach {
                searchImageByJava(packageName, it, subscriber)
            }
        }
    }

}