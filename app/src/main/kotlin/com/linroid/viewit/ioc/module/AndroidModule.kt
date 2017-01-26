package com.linroid.viewit.ioc.module

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.preference.PreferenceManager
import com.linroid.viewit.App
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
@Module
class AndroidModule(val app: App) {
    @Provides
    fun provideApp(): App = app;

    @Provides
    fun provideApplicationContext(): Context = app;

    @Singleton
    @Provides
    fun provideResources(context: Context): Resources = context.resources;

    @Provides
    fun providePackageManager(context: Context): PackageManager = context.packageManager

    @Provides
    @Singleton
    fun provideSharedPreference(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
}
