package com.linroid.viewit.data.repo

import android.content.Context
import android.content.pm.ApplicationInfo
import com.linroid.viewit.utils.MOUNTS_CACHE_DIR
import com.linroid.viewit.utils.daemon
import timber.log.Timber
import java.io.File
import java.util.*

/**
 * @author linroid <linroid@gmail.com>
 * @since 29/01/2017
 */
class ImageRepoManager(val context: Context) {
    private val cacheDir: File = File(context.cacheDir, MOUNTS_CACHE_DIR)

    private val repos = HashMap<String, ImageRepo>()

    init {
        cleanAsync()
    }

    fun getRepo(appInfo: ApplicationInfo): ImageRepo {
        if (repos.containsKey(appInfo.packageName)) {
            return repos[appInfo.packageName]!!
        } else {
            val repo = ImageRepo(context, File(cacheDir, appInfo.packageName), appInfo)
            repos[appInfo.packageName] = repo
            return repo
        }
    }

    fun removeRepo(appInfo: ApplicationInfo): ImageRepo? {
        val repo = repos.remove(appInfo.packageName)
        repo?.cleanAsync()
        return repo
    }

    fun cleanAsync() {
        daemon {
            Timber.w("cleanup all mounted files")
            if (cacheDir.exists()) {
                cacheDir.deleteRecursively()
            }
        }
    }
}