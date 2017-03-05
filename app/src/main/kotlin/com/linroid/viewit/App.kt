package com.linroid.viewit

import android.app.Application
import com.avos.avoscloud.AVAnalytics
import com.avos.avoscloud.AVOSCloud
import com.crashlytics.android.Crashlytics
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.glide.GlideImageLoader
import com.linroid.viewit.ioc.DaggerGlobalGraph
import com.linroid.viewit.ioc.GlobalGraph
import com.linroid.viewit.ioc.module.AndroidModule
import com.linroid.viewit.ioc.module.DataModule
import com.linroid.viewit.ioc.module.PrefModule
import com.linroid.viewit.ioc.module.RepoModule
import com.linroid.viewit.utils.BINARY_DIRECTORY
import com.linroid.viewit.utils.BINARY_SEARCH_IMAGE
import com.linroid.viewit.utils.OSUtils
import com.orm.SugarContext
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import java.io.File


/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
class App : Application() {
    companion object {
        @JvmStatic lateinit var graph: GlobalGraph
        @JvmStatic private lateinit var instance: App;
        fun get(): App = instance
    }

    fun graph(): GlobalGraph = graph

    override fun onCreate() {
        super.onCreate()

        graph = DaggerGlobalGraph.builder()
                .androidModule(AndroidModule(this))
                .repoModule(RepoModule())
                .dataModule(DataModule())
                .prefModule(PrefModule())
                .build();
        instance = this;
        BigImageViewer.initialize(GlideImageLoader.with(this));
        AVOSCloud.initialize(this, "08OVX4PAskiJf7j7G0l5ulGc-gzGzoHsz", "TwiiWxl2XWnTsU48wRfDbidq");
        setupDebug()
        installBinary()
        SugarContext.init(this)
        Fabric.with(this, Crashlytics())
    }

    override fun onTerminate() {
        super.onTerminate()
        SugarContext.terminate()
    }

    private fun installBinary() {
        val supportAbis = OSUtils.getSupportedAbis();
        val dir = assets.list(BINARY_DIRECTORY)
        val preferABI = OSUtils.findPreferAbi(supportAbis, dir)
        if (preferABI?.isNotEmpty() as Boolean) {
            val stream = assets.open(BINARY_DIRECTORY + File.separator + preferABI + File.separator + BINARY_SEARCH_IMAGE);
            graph.rxShell().installBinary(this, stream, BINARY_SEARCH_IMAGE, 1.0F)
                    .subscribe({ result ->
                        Timber.i("install binary $BINARY_SEARCH_IMAGE result: $result")
                    }, { error ->
                        Timber.e(error)
                    })

        }
    }

    private fun setupDebug() {
        if (!BuildConfig.DEBUG) {
            return
        }
        Timber.plant(object : Timber.DebugTree() {

            override fun log(priority: Int, t: Throwable?, message: String?, vararg args: Any?) {
                super.log(priority, t, "[${Thread.currentThread().name}]$message", *args)
            }
        })
        AVOSCloud.setDebugLogEnabled(true)
        AVAnalytics.setAnalyticsEnabled(false)
    }
}