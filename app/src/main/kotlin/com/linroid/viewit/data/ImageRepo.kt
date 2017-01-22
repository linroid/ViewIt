package com.linroid.viewit.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Environment
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.scanner.RootImageScanner
import com.linroid.viewit.data.scanner.SdcardImageScanner
import rx.Observable
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import rx.subjects.ReplaySubject
import rx.subjects.Subject
import java.io.File
import java.io.FileNotFoundException
import java.util.*

val APP_EXTERNAL_PATHS = mapOf(
        "com.tencent.mm" to arrayListOf("Tencent/MicroMsg"),
        "com.tencent.mobileqq" to arrayListOf("Tencent/QQ_Favorite", "Tencent/MobileQQ", "Tencent/QQ_Images", "Tencent/QQfile_recv")
)

/**
 * @author linroid <linroid@gmail.com>
 * @since 08/01/2017
 */
class ImageRepo(private val context: Context, private val packageManager: PackageManager) {
    private val subjects = HashMap<String, ReplaySubject<Image>>()
    private val cacheDir: File = File(context.cacheDir, "mounts")

    fun scan(appInfo: ApplicationInfo): ReplaySubject<Image> {
        val subject = getSubject(appInfo.packageName)

        val externalData: File = context.externalCacheDir.parentFile.parentFile
        var observable = SdcardImageScanner.scan(appInfo.packageName, File(externalData, appInfo.packageName))

        val packInfo: PackageInfo = packageManager.getPackageInfo(appInfo.packageName, 0)
        val dataDir = packInfo.applicationInfo.dataDir
        observable = observable.concatWith(RootImageScanner.scan(appInfo.packageName, File(dataDir)))

        if (APP_EXTERNAL_PATHS.containsKey(appInfo.packageName)) {
            val sdcard = Environment.getExternalStorageDirectory()
            val dirs = ArrayList<File>()
            APP_EXTERNAL_PATHS[appInfo.packageName]?.forEach {
                if (it.isEmpty().not()) {
                    dirs.add(File(sdcard, it))
                }
            }
            observable = observable.concatWith(SdcardImageScanner.scan(appInfo.packageName, dirs))
        }
        observable.subscribe(subject)
        return subject
    }

    fun asObservable(appInfo: ApplicationInfo): ReplaySubject<Image> {
        return getSubject(appInfo.packageName)
    }

    private fun getSubject(packageName: String): ReplaySubject<Image> {
        val subject: ReplaySubject<Image>?
        if (subjects.containsKey(packageName)) {
            subject = subjects[packageName]
        } else {
            subject = ReplaySubject.create()
            subjects.put(packageName, subject)
        }
        return subject!!
    }

}
