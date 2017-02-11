package com.linroid.viewit.ioc.module

import android.content.pm.ApplicationInfo
import com.linroid.viewit.data.repo.ScanRepo
import com.linroid.viewit.data.repo.ScanRepoManager
import com.linroid.viewit.ioc.quailifer.ActivityScope
import com.linroid.viewit.ui.viewer.ImageViewerActivity
import dagger.Module
import dagger.Provides
import rx.Observable

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
    fun provideObservable(scanRepo: ScanRepo): Observable<ScanRepo.ImageEvent> = scanRepo.registerImageEvent()

    @Provides
    @ActivityScope
    fun provideRepo(repoManager: ScanRepoManager): ScanRepo {
        return repoManager.getRepo(appInfo)
    }
}