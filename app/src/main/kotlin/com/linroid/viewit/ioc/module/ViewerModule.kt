package com.linroid.viewit.ioc.module

import android.content.pm.ApplicationInfo
import com.linroid.viewit.data.ImageRepo
import com.linroid.viewit.data.ImageRepoManager
import com.linroid.viewit.ioc.quailifer.ActivityScope
import com.linroid.viewit.ui.viewer.ImageViewerActivity
import dagger.Module
import dagger.Provides
import rx.Observable
import javax.inject.Singleton

/**
 * @author linroid <linroid@gmail.com>
 * @since 24/01/2017
 */
@Module
class ViewerModule(val activity: ImageViewerActivity, val appInfo: ApplicationInfo) {
    @Provides
    @ActivityScope
    fun provideImageViewerActivity(): ImageViewerActivity = activity;

    @Provides
    @ActivityScope
    fun provideAppInfo(): ApplicationInfo = appInfo;

    @Provides
    @ActivityScope
    fun provideObservable(imageRepo: ImageRepo): Observable<ImageRepo.ImageEvent> = imageRepo.register()

    @Provides
    @ActivityScope
    fun provideRepo(repoManager: ImageRepoManager): ImageRepo {
        return repoManager.getRepo(appInfo)
    }
}