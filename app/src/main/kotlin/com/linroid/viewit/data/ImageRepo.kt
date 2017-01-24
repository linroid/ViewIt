package com.linroid.viewit.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Environment
import com.linroid.rxshell.RxShell
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.scanner.RootImageScanner
import com.linroid.viewit.data.scanner.SdcardImageScanner
import com.linroid.viewit.utils.APP_EXTERNAL_PATHS
import rx.Observable
import rx.subjects.ReplaySubject
import java.io.File
import java.util.*

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

    fun mountFile(path: String, appInfo: ApplicationInfo): Observable<File> {
        val packageCacheDir: File = File(cacheDir, appInfo.packageName)
        val packInfo: PackageInfo = packageManager.getPackageInfo(appInfo.packageName, 0)
        val dataDir: String = packInfo.applicationInfo.dataDir
        val relativePath: String = path.substringAfter(dataDir)

        val cacheFile = File(packageCacheDir, relativePath)
        if (cacheFile.exists()) {
            return Observable.just(cacheFile)
        }
        val targetDir = cacheFile.parentFile
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        return RxShell.instance()
                .copyFile(path, cacheFile.absolutePath)
//                .flatMap { RxShell.instance().chown(cacheFile.absolutePath, uid, uid) }
                .map { cacheFile }
    }

}
