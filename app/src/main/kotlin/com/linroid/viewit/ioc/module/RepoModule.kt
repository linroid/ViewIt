package com.linroid.viewit.ioc.module

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
    @Singleton @Provides
    fun provideImageRepo(): ImageRepo {
        return ImageRepo()
    }
}
