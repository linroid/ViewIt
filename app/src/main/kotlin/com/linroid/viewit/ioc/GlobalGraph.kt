package com.linroid.viewit.ioc

import android.content.Context
import android.content.SharedPreferences
import com.linroid.rxshell.RxShell
import com.linroid.viewit.App
import com.linroid.viewit.data.repo.ImageRepo
import com.linroid.viewit.data.repo.ImageRepoManager
import com.linroid.viewit.data.repo.cloud.CloudFavoriteRepo
import com.linroid.viewit.data.repo.local.FavoriteRepo
import com.linroid.viewit.ioc.module.AndroidModule
import com.linroid.viewit.ioc.module.DataModule
import com.linroid.viewit.ioc.module.PrefModule
import com.linroid.viewit.ioc.module.RepoModule
import com.linroid.viewit.ioc.quailifer.Root
import com.linroid.viewit.ui.home.HomeActivity
import com.linroid.viewit.ui.path.PathManagerActivity
import com.linroid.viewit.utils.PREF_FILTER_SIZE
import com.linroid.viewit.utils.PREF_SORT_TYPE
import com.linroid.viewit.utils.RxOnce
import com.linroid.viewit.utils.pref.LongPreference
import dagger.Component
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
    fun inject(imageRepo: ImageRepo)
    fun inject(rxOnce: RxOnce)
    fun inject(activity: PathManagerActivity)

    fun context(): Context
    @Root fun rootRxShell(): RxShell
    fun rxShell(): RxShell
    fun repoManager(): ImageRepoManager
    fun sharedPreferences(): SharedPreferences
    //    fun realm(): Realm
    fun dbRepo(): FavoriteRepo

    fun netRepo(): CloudFavoriteRepo

    @Named(PREF_FILTER_SIZE)
    fun filterSizePref(): LongPreference

    @Named(PREF_SORT_TYPE)
    fun sortTypePref(): LongPreference

}