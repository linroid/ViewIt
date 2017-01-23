package com.linroid.viewit

import android.app.Application
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.glide.GlideImageLoader
import com.linroid.rxshell.RxShell
import com.linroid.viewit.ioc.DaggerGlobalGraph
import com.linroid.viewit.ioc.GlobalGraph
import com.linroid.viewit.ioc.module.AndroidModule
import com.linroid.viewit.ioc.module.DataModule
import com.linroid.viewit.ioc.module.RepoModule
import com.linroid.viewit.utils.BINARY_DIRECTORY
import com.linroid.viewit.utils.BINARY_SEARCH_IMAGE
import com.linroid.viewit.utils.OSUtils
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
class App : Application() {
    companion object {
        @JvmStatic lateinit var graph: GlobalGraph
        @JvmStatic lateinit var instance: App;
        fun get(): App = instance
    }

    fun graph(): GlobalGraph = graph

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        graph = DaggerGlobalGraph.builder()
                .androidModule(AndroidModule(this))
                .repoModule(RepoModule())
                .dataModule(DataModule())
                .build();
        instance = this;
        BigImageViewer.initialize(GlideImageLoader.with(this));
        installBinary();
    }

    private fun installBinary() {
        val supportAbis = OSUtils.getSupportedAbis();
        val dir = assets.list(BINARY_DIRECTORY)
        val preferABI = OSUtils.findPreferAbi(supportAbis, dir)
        if (preferABI?.isNotEmpty() as Boolean) {
            val stream = assets.open(BINARY_DIRECTORY + File.separator + preferABI + File.separator + BINARY_SEARCH_IMAGE);
            RxShell.instance()
                    .installBinary(this, stream, BINARY_SEARCH_IMAGE, 1.0F)
                    .delaySubscription(3, TimeUnit.SECONDS)
                    .subscribe({ result ->
                        Timber.i("install binary $BINARY_SEARCH_IMAGE result: $result")
                    }, { error ->
                        Timber.e(error)
                    })

        }
    }
}
