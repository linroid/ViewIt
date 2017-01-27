package com.linroid.viewit.ioc

import android.content.Context
import com.linroid.rxshell.RxShell
import com.linroid.viewit.App
import com.linroid.viewit.data.ImageRepo
import com.linroid.viewit.ioc.module.AndroidModule
import com.linroid.viewit.ioc.module.DataModule
import com.linroid.viewit.ioc.module.PrefModule
import com.linroid.viewit.ioc.module.RepoModule
import com.linroid.viewit.ui.home.HomeActivity
import com.linroid.viewit.utils.RxOnce
import com.linroid.viewit.utils.pref.LongPreference
import dagger.Component
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

    fun getContext(): Context
    fun getSortTypePref(): LongPreference
    fun getRxShell(): RxShell
    fun getImageRepo(): ImageRepo
}