package com.linroid.viewit

import android.app.Application
import com.linroid.viewit.ioc.DaggerGlobalGraph
import com.linroid.viewit.ioc.GlobalGraph
import timber.log.Timber

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
class App : Application() {
    companion object {
        @JvmStatic lateinit var graph: GlobalGraph
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        graph = DaggerGlobalGraph.create();
    }
}
