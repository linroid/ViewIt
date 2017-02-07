package com.linroid.viewit

import android.app.Application
import com.avos.avoscloud.AVOSCloud
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.glide.GlideImageLoader
import com.linroid.viewit.ioc.DaggerGlobalGraph
import com.linroid.viewit.ioc.GlobalGraph
import com.linroid.viewit.ioc.module.AndroidModule
import com.linroid.viewit.ioc.module.DataModule
import com.linroid.viewit.ioc.module.PrefModule
import com.linroid.viewit.ioc.module.RepoModule
import timber.log.Timber

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
    }

    private fun setupDebug() {
        if (!BuildConfig.DEBUG) {
            return
        }
        Timber.plant(object : Timber.DebugTree() {
            override fun formatMessage(message: String?, args: Array<out Any>?): String {
                return "[${Thread.currentThread().name}]${super.formatMessage(message, args)}"
            }
        })
        AVOSCloud.setDebugLogEnabled(true);
    }
}
