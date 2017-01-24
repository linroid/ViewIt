package com.linroid.viewit.ioc.module

import android.content.Context
import android.content.pm.PackageManager
import com.linroid.viewit.data.ImageRepo
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * @author linroid <linroid@gmail.com>
 * @since 08/01/2017
 */
@Module
class RepoModule {
    @Singleton
    @Provides
    fun provideImageRepo(context: Context, packageManager: PackageManager): ImageRepo {
        return ImageRepo(context, packageManager)
    }
}
