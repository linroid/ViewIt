package com.linroid.viewit.data.scanner

import com.bumptech.glide.load.resource.bitmap.ImageHeaderParser
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.utils.ImageMIME
import com.linroid.viewit.utils.RootFileInputStream
import com.stericson.RootShell.execution.Command
import com.stericson.RootTools.RootTools
import rx.Subscriber
import timber.log.Timber
import java.io.File

/**
 * @author linroid <linroid@gmail.com>
 * @since 09/01/2017
 */
object RootImageScanner : ImageScanner() {
    override fun searchImage(packageName:String, file: File, subscriber: Subscriber<in Image>) {
//        searchImage(file.absolutePath, subscriber)

        subscriber.onNext(createTestImage("cache/image_cache/v2.ols100.1/97/0IwMVaT3jETAT_Bssret4vh30ZI.cnt"))
        subscriber.onNext(createTestImage("cache/image_cache/v2.ols100.1/97/7YawjKOH549bjVePrFlMv7Uwl_U.cnt"))
        subscriber.onNext(createTestImage("cache/image_cache/v2.ols100.1/97/Dl_noc5XDVebB8NMd0qKtSayM0U.cnt"))
        subscriber.onNext(createTestImage("cache/image_cache/v2.ols100.1/99/LmBAmqr-mae8lYIQuHsdPC0BYwY.cnt"))
        subscriber.onNext(createTestImage("cache/image_cache/v2.ols100.1/99/m_U-ulhiiN9L-hOmjM6ZDAxlU8A.cnt"))
    }

    private fun createTestImage(path : String) : Image {
        var file = File("/data/data/com.zhihu.android", path)
        return Image(file, path, 1024, "com.zhihu.android", ImageHeaderParser.ImageType.PNG)
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