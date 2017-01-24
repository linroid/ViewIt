package com.linroid.viewit.ioc.module

import android.content.pm.ApplicationInfo
import com.linroid.viewit.data.ImageRepo
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.ioc.quailifer.ActivityScope
import com.linroid.viewit.ui.gallery.GalleryActivity
import com.linroid.viewit.ui.gallery.GalleryItemViewProvider
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
    fun provideGalleryActivity(): GalleryActivity = activity;

    @Provides
    @ActivityScope
    fun provideImageViewProvider(imageRepo: ImageRepo): GalleryItemViewProvider
            = GalleryItemViewProvider(activity, imageRepo, info)

    @Provides
    @ActivityScope
    fun provideAdapter(images: MutableList<Any>, galleryProvider: GalleryItemViewProvider): MultiTypeAdapter {
        val adapter = MultiTypeAdapter(images)
        adapter.register(Image::class.java, galleryProvider)
        return adapter
    }

    @Provides
    @ActivityScope
    fun provideImages(): MutableList<Any> = ArrayList()
}