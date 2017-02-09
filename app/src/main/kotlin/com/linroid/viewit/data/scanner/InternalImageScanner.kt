package com.linroid.viewit.data.scanner

//import com.linroid.viewit.utils.RootFileInputStream
import com.linroid.rxshell.RxShell
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.ioc.quailifer.Root
import rx.Observable
import java.io.File
import javax.inject.Inject

/**
 * @author linroid <linroid@gmail.com>
 * @since 09/01/2017
 */
class InternalImageScanner @Inject constructor(@Root val rxShell: RxShell) : ImageScanner() {
    override fun scan(packageName: String, dirs: List<File>): Observable<Image> {
        return scanByBinary(rxShell, packageName, dirs)
    }
}