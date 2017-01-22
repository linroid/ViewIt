package com.linroid.viewit.data.scanner

import com.bumptech.glide.load.resource.bitmap.ImageHeaderParser
import com.linroid.rxshell.RxShell
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.utils.BINARY_SEARCH_IMAGE
import com.linroid.viewit.utils.ImageMIME
import com.spazedog.lib.rootfw4.RootFW
//import com.linroid.viewit.utils.RootFileInputStream
import rx.Subscriber
import timber.log.Timber
import java.io.File

/**
 * @author linroid <linroid@gmail.com>
 * @since 09/01/2017
 */
object RootImageScanner : ImageScanner() {
    override fun searchImage(packageName: String, file: File, subscriber: Subscriber<in Image>) {
        if (!RootFW.isConnected()) {
            RootFW.connect();
        }
        searchImage(file.absolutePath, subscriber)
        subscriber.onCompleted()
    }

    fun searchImage(file: String, subscriber: Subscriber<in Image>) {
        Timber.d("searchImage : $file");
        val rootFile = RootFW.getFile(file)
        if (rootFile.isDirectory) {
            rootFile.list?.forEach { sub ->
                searchImage(file + "/" + sub, subscriber)
            }
        } else {
            val type = ImageMIME.getImageType(RootFW.getFileInputStream(file))
            if (type != ImageHeaderParser.ImageType.UNKNOWN) {
                subscriber.onNext(Image(rootFile.absolutePath, rootFile.size(), type))
            }
        }

    }

//    fun searchImage(dir: String, subscriber: Subscriber<in Image>) {
//        Timber.d("searchImage : $dir")
//        if (!RootTools.exists(dir, true)) {
//            return;
//        }
//        val s = "IFS='\n';CURDIR='" + dir + "';" +
//                "for i in `ls \$CURDIR`; do if [ -d \$CURDIR/\$i ]; " +
//                "then echo \"d \$CURDIR/\$i\";else echo \"f \$CURDIR/\$i\";" +
//                " fi; " +
//                "done"
//
//        val command: Command = object : Command(0, s) {
//            override fun commandOutput(id: Int, line: String) {
//                super.commandOutput(id, line)
//                Timber.d(line)
//                val path = line.substring(2)
//                if (line.startsWith("d")) {
//                    searchImage(path, subscriber)
//                } else {
//                    val file = File(path)
//                    val type = ImageMIME.getImageType(RootFileInputStream(file))
//                    if (type != ImageHeaderParser.ImageType.UNKNOWN) {
//                        val image = Image(file, file.length(), type)
//                        subscriber.onNext(image);
//                    }
//                }
//            }
//
//            override fun commandTerminated(id: Int, reason: String?) {
//                super.commandTerminated(id, reason)
//            }
//
//            override fun commandCompleted(id: Int, exitcode: Int) {
//                super.commandCompleted(id, exitcode)
//            }
//        }
//        RootTools.getShell(true).add(command)
//    }
}