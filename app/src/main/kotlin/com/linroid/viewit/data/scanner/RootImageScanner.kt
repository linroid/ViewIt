package com.linroid.viewit.data.scanner

//import com.linroid.viewit.utils.RootFileInputStream
import com.linroid.rxshell.RxShell
import com.linroid.viewit.App
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.model.ImageType
import com.linroid.viewit.utils.BINARY_SEARCH_IMAGE
import rx.Observable
import java.io.File
import java.util.*

/**
 * @author linroid <linroid@gmail.com>
 * @since 09/01/2017
 */
object RootImageScanner : ImageScanner() {
    override fun scan(packageName: String, dirs: List<File>): Observable<Image> {
        val observables = ArrayList<Observable<String>>(dirs.size)
        dirs.forEach { file ->
            observables.add(RxShell.instance().execBinary(App.get(), BINARY_SEARCH_IMAGE, file.absolutePath))
        }
        return Observable.merge(observables)
                .filter { line -> line != null && line.split(" ").size == 4 }
                .map { line ->
                    val parser = line.split(" ")
                    val size = parser[0].toLong()
                    val lastModified = parser[1].toLong()
                    val typeVal = parser[2].toInt()
                    val type: ImageType = ImageType.from(typeVal)
                    val path = parser[3]
                    Image(path, size, lastModified, type)
                }
                .filter { image -> image.type != ImageType.UNKNOWN }
    }
}