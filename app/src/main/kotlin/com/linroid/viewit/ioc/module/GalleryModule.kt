package com.linroid.viewit.ioc.module

import android.content.pm.ApplicationInfo
import com.linroid.viewit.data.ScanRepo
import com.linroid.viewit.data.ScanRepoManager
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.ioc.quailifer.ActivityScope
import com.linroid.viewit.ui.gallery.GalleryActivity
import com.linroid.viewit.ui.gallery.provider.ImageViewProvider
import dagger.Module
import dagger.Provides
import me.drakeet.multitype.MultiTypeAdapter
import java.util.*

/**
 * @author linroid <linroid@gmail.com>
 * @since 24/01/2017
 */
@Module
class GalleryModule(val activity: GalleryActivity, val info: ApplicationInfo) {
    @Provides
    @ActivityScope
    fun provideInfo(): ApplicationInfo = info

    @Provides
    @ActivityScope
    fun provideGalleryActivity(): GalleryActivity = activity;

    @Provides
    @ActivityScope
    fun provideImageViewProvider(scanRepo: ScanRepo): ImageViewProvider
            = ImageViewProvider(activity, scanRepo, info)

    @Provides
    @ActivityScope
    fun provideAdapter(images: MutableList<Any>, galleryProvider: ImageViewProvider): MultiTypeAdapter {
        val adapter = MultiTypeAdapter(images)
        adapter.register(Image::class.java, galleryProvider)
        return adapter
    }

    @Provides
    @ActivityScope
    fun provideImages(): MutableList<Any> = ArrayList()

    @Provides
    @ActivityScope
    fun provideRepo(repoManager: ScanRepoManager): ScanRepo {
        return repoManager.getRepo(info)
    }
}