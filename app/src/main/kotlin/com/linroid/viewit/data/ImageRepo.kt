package com.linroid.viewit.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Environment
import android.support.annotation.IntDef
import com.linroid.rxshell.RxShell
import com.linroid.viewit.R
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.scanner.RootImageScanner
import com.linroid.viewit.data.scanner.SdcardImageScanner
import com.linroid.viewit.utils.APP_EXTERNAL_PATHS
import com.linroid.viewit.utils.FileUtils
import com.linroid.viewit.utils.RootUtils
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.io.File
import java.util.*

/**
 * @author linroid <linroid@gmail.com>
 * @since 08/01/2017
 */
const val UPDATE_EVENT = 0x1L
const val REMOVE_EVENT = 0x2L
const val INSERT_EVENT = 0x3L

@IntDef(UPDATE_EVENT, REMOVE_EVENT, INSERT_EVENT)
annotation class ImageEventType

class ImageRepo(private val context: Context, private val packageManager: PackageManager) {
    private val subjects = HashMap<String, PublishSubject<ImageEvent>>()
    private val cacheDir: File = File(context.cacheDir, "mounts")
    private val imagesMap = HashMap<String, MutableList<Image>>()

    fun scan(appInfo: ApplicationInfo): Observable<List<Image>> {
        val subject = getSubject(appInfo.packageName)
        val images = getImages(appInfo.packageName)

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
        return observable.sorted { image, image2 -> -image.size.compareTo(image2.size) }
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    images.clear()
                    images.addAll(it)
                    subject.onNext(ImageEvent(UPDATE_EVENT, 0, it.size, images))
                }
    }

    fun register(appInfo: ApplicationInfo): PublishSubject<ImageEvent> {
        return getSubject(appInfo.packageName)
    }

    fun destroy(appInfo: ApplicationInfo) {
        val packageName = appInfo.packageName
        if (subjects.containsKey(packageName)) {
            val subject = subjects[packageName]
            subjects.remove(packageName)
        }
    }

    private fun getSubject(packageName: String): PublishSubject<ImageEvent> {
        val subject: PublishSubject<ImageEvent>?
        if (subjects.containsKey(packageName)) {
            subject = subjects[packageName]
        } else {
            subject = PublishSubject.create()
            subjects.put(packageName, subject)
        }
        return subject!!
    }

    fun getImages(appInfo: ApplicationInfo): MutableList<Image> {
        return getImages(appInfo.packageName)
    }

    fun getImageAt(position: Int, appInfo: ApplicationInfo): Image {
        return getImages(appInfo.packageName)[position]
    }

    private fun getImages(packageName: String): MutableList<Image> {
        val images: MutableList<Image>?
        if (imagesMap.containsKey(packageName)) {
            images = imagesMap[packageName]
        } else {
            images = ArrayList<Image>()
            imagesMap.put(packageName, images)
        }
        return images!!
    }

    fun mountImage(image: Image, appInfo: ApplicationInfo): Observable<Image> {
        val packageCacheDir: File = File(cacheDir, appInfo.packageName)
        val packInfo: PackageInfo = packageManager.getPackageInfo(appInfo.packageName, 0)
        val dataDir: String = packInfo.applicationInfo.dataDir
        val relativePath: String = image.path.substringAfter(dataDir)

        val cacheFile = File(packageCacheDir, relativePath)
        image.mountPath = cacheFile.absolutePath
        if (cacheFile.exists()) {
            return Observable.just(image)
        }
        val targetDir = cacheFile.parentFile
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        if (!cacheFile.exists()) {
            cacheFile.createNewFile()
        }
        return RxShell.instance()
                .copyFile(image.path, cacheFile.absolutePath)
//                .flatMap { RxShell.instance().chown(cacheFile.absolutePath, uid, uid) }
                .map { image }
    }

    fun saveImage(image: Image, appInfo: ApplicationInfo): Observable<File> {
        return Observable.create<File> { subscriber ->
            if (Environment.getExternalStorageState() != android.os.Environment.MEDIA_MOUNTED) {
                subscriber.onError(IllegalStateException(context.getString(R.string.msg_save_image_failed_without_sdcard)));
                return@create
            }
            val targetName = "${packageManager.getApplicationLabel(appInfo)}_${System.currentTimeMillis()}.${image.postfix()}"
            @SuppressLint("SdCardPath")
            val pictureDirectory = "/sdcard${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}";
            val saveDirectory = File(pictureDirectory, context.getString(R.string.app_name));
            val desFile: File;
            try {
                if (!saveDirectory.exists()) {
                    saveDirectory.mkdirs();
                }
                desFile = File(saveDirectory, targetName);
                if (!desFile.exists()) {
                    desFile.createNewFile();
                }
                FileUtils.copyFile(image.file(), desFile);
                subscriber.onNext(desFile)
                subscriber.onCompleted()
            } catch (error: Exception) {
                subscriber.onError(error)
            }
        }.subscribeOn(Schedulers.io())
    }

    fun deleteImage(position: Int, appInfo: ApplicationInfo): Observable<Boolean> {
        val images = getImages(appInfo.packageName)
        val image = images[position]
        return Observable.just(image.path)
                .flatMap { path ->
                    if (RootUtils.isRootFile(context, path)) {
                        return@flatMap RxShell.instance().deleteFile(path)
                    } else {
                        File(image.path).delete()
                        return@flatMap Observable.just(true)
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    images.removeAt(position)
                    getSubject(appInfo.packageName).onNext(ImageEvent(REMOVE_EVENT, position, 1, images))
                }
    }

    data class ImageEvent(@ImageEventType val type: Long, val position: Int, val effectCount: Int, val images: List<Image>)

}
