package com.linroid.viewit.data.scanner

import com.bumptech.glide.load.resource.bitmap.ImageHeaderParser
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.utils.ImageMIME
import rx.Subscriber
import timber.log.Timber
import java.io.File

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
object SdcardImageScanner : ImageScanner() {

    override fun searchImage(packageName: String, file: File, subscriber: Subscriber<in Image>) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile) {
            val type = ImageMIME.getImageType(file)
            if (type != ImageHeaderParser.ImageType.UNKNOWN) {
                val image = Image(file.absolutePath, file.length(), type)
                subscriber.onNext(image);
            }
        } else if (file.isDirectory) {
            Timber.d("directory : ${file.absolutePath}")
            file.listFiles()?.forEach {
                searchImage(packageName, it, subscriber)
            }
        }
    }
}