package com.linroid.viewit.ioc.module

import android.content.Context
import android.content.res.AssetManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.linroid.rxshell.RxShell
import com.linroid.viewit.data.DBRepo
import com.linroid.viewit.data.NetRepo
import com.linroid.viewit.utils.BINARY_DIRECTORY
import com.linroid.viewit.utils.BINARY_SEARCH_IMAGE
import com.linroid.viewit.utils.DB_VERSION
import com.linroid.viewit.utils.OSUtils
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
@Module
class DataModule {

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson, httpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder().apply {
            addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            addConverterFactory(GsonConverterFactory.create(gson))
        }.build();
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
                .setDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'")
                .create();
    }

    @Provides
    @Singleton
    fun provideOkHttp(context: Context, logging: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder().apply {
            val cacheDir = File(context.cacheDir, "http")
            cache(Cache(cacheDir, 10 * 1024 * 1024L))
            connectTimeout(10L, TimeUnit.SECONDS)
            writeTimeout(10L, TimeUnit.SECONDS)
            readTimeout(30L, TimeUnit.SECONDS)
            addInterceptor(logging)
        }.build()
    }

    @Singleton
    @Provides
    fun provideHttpLogging(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { Timber.tag("OkHttp").d(it) })
        logging.level = HttpLoggingInterceptor.Level.BASIC
        return logging;
    }

    @Singleton
    @Provides
    fun provideRxShell(context: Context): RxShell {
        val shell = RxShell(true)
        val supportAbis = OSUtils.getSupportedAbis();
        val assets: AssetManager = context.assets
        val dir = assets.list(BINARY_DIRECTORY)
        val preferABI = OSUtils.findPreferAbi(supportAbis, dir)
        if (preferABI?.isNotEmpty() as Boolean) {
            val stream = assets.open(BINARY_DIRECTORY + File.separator + preferABI + File.separator + BINARY_SEARCH_IMAGE);
            shell.installBinary(context, stream, BINARY_SEARCH_IMAGE, 1.0F)
                    .subscribe({ result ->
                        Timber.i("install binary $BINARY_SEARCH_IMAGE result: $result")
                    }, { error ->
                        Timber.e(error)
                    })

        }
        return shell
    }

    @Singleton
    @Provides
    fun provideRealm(context: Context): Realm {
        Realm.init(context)
        Timber.d("provideRealm")
        return Realm.getInstance(RealmConfiguration.Builder()
                .schemaVersion(DB_VERSION)
                .deleteRealmIfMigrationNeeded()
                .build())
    }

    @Singleton
    @Provides
    fun provideDBRepo(realm: Realm): DBRepo {
        return DBRepo(realm)
    }

    @Singleton
    @Provides
    fun provideNetRepo(): NetRepo {
        return NetRepo()
    }
}