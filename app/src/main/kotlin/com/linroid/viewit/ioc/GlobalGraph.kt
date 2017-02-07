package com.linroid.viewit.ioc

import android.content.Context
import android.content.SharedPreferences
import com.linroid.rxshell.RxShell
import com.linroid.viewit.App
import com.linroid.viewit.data.DBRepo
import com.linroid.viewit.data.NetRepo
import com.linroid.viewit.data.ScanRepo
import com.linroid.viewit.data.ScanRepoManager
import com.linroid.viewit.ioc.module.AndroidModule
import com.linroid.viewit.ioc.module.DataModule
import com.linroid.viewit.ioc.module.PrefModule
import com.linroid.viewit.ioc.module.RepoModule
import com.linroid.viewit.ui.favorite.FavoriteCreateActivity
import com.linroid.viewit.ui.home.HomeActivity
import com.linroid.viewit.utils.PREF_FILTER_SIZE
import com.linroid.viewit.utils.PREF_SORT_TYPE
import com.linroid.viewit.utils.RxOnce
import com.linroid.viewit.utils.pref.LongPreference
import dagger.Component
import io.realm.Realm
import javax.inject.Named
import javax.inject.Singleton

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
@Singleton
@Component(modules = arrayOf(AndroidModule::class, DataModule::class, RepoModule::class, PrefModule::class))
interface GlobalGraph {
    fun inject(app: App)
    fun inject(activity: HomeActivity)
    fun inject(activity: FavoriteCreateActivity)
    fun inject(scanRepo: ScanRepo)
    fun inject(rxOnce: RxOnce)

    fun context(): Context
    fun rxShell(): RxShell
    fun repoManager(): ScanRepoManager
    fun sharedPreferences(): SharedPreferences
    fun realm(): Realm
    fun dbRepo(): DBRepo
    fun netRepo(): NetRepo

    @Named(PREF_FILTER_SIZE)
    fun filterSizePref(): LongPreference

    @Named(PREF_SORT_TYPE)
    fun sortTypePref(): LongPreference

}