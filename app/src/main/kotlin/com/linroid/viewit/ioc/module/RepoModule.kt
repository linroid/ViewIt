package com.linroid.viewit.ioc.module

import android.content.Context
import com.linroid.viewit.data.repo.ScanRepoManager
import com.linroid.viewit.data.repo.cloud.CloudFavoriteRepo
import com.linroid.viewit.data.repo.cloud.CloudPathRepo
import com.linroid.viewit.data.repo.local.FavoriteRepo
import com.linroid.viewit.data.repo.local.PathRepo
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * @author linroid <linroid@gmail.com>
 * @since 08/01/2017
 */
@Module
class RepoModule {
    @Singleton @Provides
    fun provideImageRepoManager(context: Context): ScanRepoManager {
        return ScanRepoManager(context)
    }

    @Singleton
    @Provides
    fun provideFavoriteRepo(): FavoriteRepo {
        return FavoriteRepo()
    }

    @Singleton
    @Provides
    fun provideCloudFavoriteRepo(): CloudFavoriteRepo {
        return CloudFavoriteRepo()
    }

    @Singleton
    @Provides
    fun provideScanPathRepo(): PathRepo {
        return PathRepo()
    }

    @Singleton
    @Provides
    fun provideCloudPathRepo(): CloudPathRepo {
        return CloudPathRepo()
    }
}
