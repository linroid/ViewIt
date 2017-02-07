package com.linroid.viewit.data

import android.content.Context
import android.content.pm.ApplicationInfo
import java.util.*

/**
 * @author linroid <linroid@gmail.com>
 * @since 29/01/2017
 */
class ScanRepoManager(val context: Context) {
    private val repos = HashMap<String, ScanRepo>()

    fun getRepo(appInfo: ApplicationInfo): ScanRepo {
        if (repos.containsKey(appInfo.packageName)) {
            return repos[appInfo.packageName]!!
        } else {
            val repo = ScanRepo(context, appInfo)
            repos[appInfo.packageName] = repo
            return repo
        }
    }

    fun removeRepo(appInfo: ApplicationInfo): ScanRepo? {
        return repos.remove(appInfo.packageName)
    }
}