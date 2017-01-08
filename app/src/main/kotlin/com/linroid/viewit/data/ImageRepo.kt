package com.linroid.viewit.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Environment
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.scanner.ImageScanner
import rx.Observable
import rx.subjects.PublishSubject
import java.io.File
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
    private val subjects = HashMap<String, PublishSubject<Image>>()

    fun scan(appInfo: ApplicationInfo): PublishSubject<Image> {
        val subject = getSubject(appInfo.packageName)

        val externalData: File = context.externalCacheDir.parentFile.parentFile
        var observable = ImageScanner.scan(File(externalData, appInfo.packageName))

        val packInfo: PackageInfo = packageManager.getPackageInfo(appInfo.packageName, 0)
        val dataDir = packInfo.applicationInfo.dataDir
        observable = observable.concatWith(ImageScanner.scan(File(dataDir)))

        if (APP_EXTERNAL_PATHS.containsKey(appInfo.packageName)) {
            val sdcard = Environment.getExternalStorageDirectory()
            val dirs = ArrayList<File>()
            APP_EXTERNAL_PATHS[appInfo.packageName]?.forEach {
                if (it.isEmpty().not()) {
                    dirs.add(File(sdcard, it))
                }
            }
            observable = observable.concatWith(ImageScanner.scan(dirs))
        }
        observable.subscribe(subject)
        return subject
    }

    fun scanTest(appInfo: ApplicationInfo): Observable<Image> {
        val subject = getSubject(appInfo.packageName)

        val externalData: File = context.externalCacheDir.parentFile.parentFile
        var observable = ImageScanner.scan(File(externalData, appInfo.packageName))

        val packInfo: PackageInfo = packageManager.getPackageInfo(appInfo.packageName, 0)
        val dataDir = packInfo.applicationInfo.dataDir
        observable = observable.mergeWith(ImageScanner.scan(File(dataDir)))

        if (APP_EXTERNAL_PATHS.containsKey(appInfo.packageName)) {
            val sdcard = Environment.getExternalStorageDirectory()
            val dirs = ArrayList<File>()
            APP_EXTERNAL_PATHS[appInfo.packageName]?.forEach {
                dirs.add(File(sdcard, it))
            }
            observable = observable.mergeWith(ImageScanner.scan(dirs))
        }
        return observable
    }

    fun asObservable(appInfo: ApplicationInfo): PublishSubject<Image> {
        return getSubject(appInfo.packageName)
    }

    private fun getSubject(packageName: String): PublishSubject<Image> {
        val subject: PublishSubject<Image>?
        if (subjects.containsKey(packageName)) {
            subject = subjects[packageName]
        } else {
            subject = PublishSubject.create()
        }
        return subject!!
    }
}
