package com.linroid.viewit.data.repo

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Environment
import android.support.annotation.IntDef
import com.linroid.rxshell.RxShell
import com.linroid.viewit.App
import com.linroid.viewit.R
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.model.ImageTree
import com.linroid.viewit.data.scanner.ExternalImageScanner
import com.linroid.viewit.data.scanner.InternalImageScanner
import com.linroid.viewit.ioc.quailifer.Root
import com.linroid.viewit.utils.*
import com.linroid.viewit.utils.pref.LongPreference
import rx.Observable
import rx.lang.kotlin.BehaviorSubject
import rx.lang.kotlin.PublishSubject
import rx.lang.kotlin.filterNotNull
import rx.lang.kotlin.onErrorReturnNull
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Named

/**
 * @author linroid <linroid@gmail.com>
 * @since 08/01/2017
 */
class ScanRepo(val context: Context, val appInfo: ApplicationInfo) {
    companion object {
        const val UPDATE_EVENT = 0x1L
        const val REMOVE_EVENT = 0x2L
        const val INSERT_EVENT = 0x3L

        @IntDef(UPDATE_EVENT, REMOVE_EVENT, INSERT_EVENT)
        annotation class ImageEventType

        const val SORT_BY_PATH = 0x1L
        const val SORT_BY_SIZE = 0x2L
        const val SORT_BY_TIME = 0x3L

        @IntDef(SORT_BY_PATH, SORT_BY_SIZE, SORT_BY_TIME)
        annotation class ImageSortType
    }

    @Inject
    lateinit var packageManager: PackageManager
    @Inject @Root
    lateinit var rxShell: RxShell
    @Inject
    lateinit var internalScanner: InternalImageScanner
    @Inject
    lateinit var externalScanner: ExternalImageScanner

    @field:[Inject Named(PREF_SORT_TYPE)]
    lateinit var sortTypePref: LongPreference

    @field:[Inject Named(PREF_FILTER_SIZE)]
    lateinit var filterSizePref: LongPreference

    private val eventBus = PublishSubject<ImageEvent>()
    private val treeBuilder = BehaviorSubject<ImageTree>()
    private val cacheDir: File = File(context.cacheDir, "mounts")
    val images = ArrayList<Image>()

    var viewerHolderImages: List<Image>? = null

    init {
        App.graph.inject(this)
        createTreeBuilder()
    }

    fun scan(): Observable<Image> {
        val sortType = sortTypePref.get()
        Timber.d("scan images for ${appInfo.packageName}, sortTypePref:$sortType")
        val externalData: File = context.externalCacheDir.parentFile.parentFile
        var observable = externalScanner.scan(appInfo.packageName, File(externalData, appInfo.packageName))

        if (RootUtils.isRootAvailable()) {
            val packInfo: PackageInfo = packageManager.getPackageInfo(appInfo.packageName, 0)
            val dataDir = packInfo.applicationInfo.dataDir
            val internal = internalScanner.scan(appInfo.packageName, File(dataDir))
                    .doOnError { error -> Timber.e(error, "scan internal image failed") }
                    .onErrorReturnNull().filterNotNull()
            observable = observable.concatWith(internal)
        }

        if (APP_EXTERNAL_PATHS.containsKey(appInfo.packageName)) {
            val sdcard = Environment.getExternalStorageDirectory()
            val dirs = ArrayList<File>()
            APP_EXTERNAL_PATHS[appInfo.packageName]?.forEach {
                if (it.isEmpty().not()) {
                    dirs.add(File(sdcard, it))
                }
            }
            observable = observable.concatWith(externalScanner.scan(appInfo.packageName, dirs))
        }

        val scanned: MutableList<Image> = ArrayList()
        return observable
                .doOnNext {
                    scanned.add(it)
                }
                .doOnCompleted {
                    Timber.d("doOnCompleted")
                    images.clear()
                    images.addAll(scanned)
                    eventBus.onNext(ImageEvent(UPDATE_EVENT, 0, images.size, images))
                }
    }

    fun registerImageEvent(): PublishSubject<ImageEvent> {
        return eventBus
    }

    fun mountImage(image: Image): Observable<Image> {
        val packageCacheDir: File = File(cacheDir, appInfo.packageName)
        val packInfo: PackageInfo = packageManager.getPackageInfo(appInfo.packageName, 0)
        val dataDir: String = packInfo.applicationInfo.dataDir
        val relativePath: String = image.source.absolutePath.substringAfter(dataDir)

        val cacheFile = File(packageCacheDir, relativePath)
        image.mountFile = cacheFile
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
        return rxShell.copyFile(image.path, cacheFile.absolutePath)
//                .flatMap { RxShell.instance().chown(cacheFile.absolutePath, uid, uid) }
                .map { image }
    }

    fun saveImage(image: Image, appInfo: ApplicationInfo): Observable<File> {
        return Observable.just(image)
                .flatMap { if (it.file() == null) mountImage(image) else Observable.just(image) }
                .flatMap {
                    if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
                        throw IllegalStateException(context.getString(R.string.msg_save_image_failed_without_sdcard));
                    }
                    val targetName = "${packageManager.getApplicationLabel(appInfo)}_${System.currentTimeMillis()}.${image.postfix()}"
                    @SuppressLint("SdCardPath")
                    val pictureDirectory = "/sdcard${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}";
                    val saveDirectory = File(pictureDirectory, context.getString(R.string.app_name));
                    val desFile: File;
                    if (!saveDirectory.exists()) {
                        saveDirectory.mkdirs();
                    }
                    desFile = File(saveDirectory, targetName);
                    if (!desFile.exists()) {
                        desFile.createNewFile();
                    }
                    FileUtils.copyFile(image.file()!!, desFile);
                    return@flatMap Observable.just(desFile)
                }.subscribeOn(Schedulers.io())
    }

    fun deleteImage(image: Image, appInfo: ApplicationInfo): Observable<Boolean> {
        return Observable.just(image.path)
                .flatMap { path ->
                    if (RootUtils.isRootFile(path)) {
                        return@flatMap rxShell.deleteFile(path)
                    } else {
                        File(image.path).delete()
                        return@flatMap Observable.just(true)
                    }
                }
                .subscribeOn(Schedulers.io())
                .doOnNext {
                    val position = images.indexOf(image)
                    images.remove(image)
                    eventBus.onNext(ImageEvent(REMOVE_EVENT, position, 1, arrayListOf(image)))
                }
    }

    fun registerTreeBuilder(): Observable<ImageTree> {
        return treeBuilder
    }

    fun getImageTree(): ImageTree? = treeBuilder.value

    private fun createTreeBuilder() {
        eventBus.observeOn(Schedulers.computation()).subscribe {
            Timber.d("should update treeBuilder")
            when (it.type) {
                UPDATE_EVENT -> {
                    val map = HashMap<String, ImageTree>()
                    val rootTree = ImageTree(File.separator)
                    images.forEach {
                        val dir = it.source.parent
                        var tree = map[dir]
                        if (tree == null) {
                            tree = ImageTree(it.source.parent)
                            map[dir] = tree
                        }
                        tree.images.add(it)
                    }
                    map.forEach { key, imageTree ->
                        rootTree.add(imageTree)
                    }
                    treeBuilder.onNext(rootTree)
                }
                INSERT_EVENT -> {
                    if (treeBuilder.hasValue()) {
                        val value = treeBuilder.value
                        it.effectedImages.forEach { image ->
                            value.insertImage(image)
                        }
                        treeBuilder.onNext(value)
                    }
                }
                REMOVE_EVENT -> {
                    if (treeBuilder.hasValue()) {
                        val value = treeBuilder.value
                        it.effectedImages.forEach { image ->
                            value.removeImage(image)
                        }
                        treeBuilder.onNext(value)
                    }
                }
            }
        }
    }

    data class ImageEvent(@ImageEventType val type: Long, val position: Int, val effectCount: Int, val effectedImages: List<Image>)

}
