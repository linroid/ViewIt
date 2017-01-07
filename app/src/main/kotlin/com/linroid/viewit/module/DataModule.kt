package com.linroid.viewit.module

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Component
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
@Component
class DataModule {
    @Provides
    fun provideRetrofit(gson: Gson): Retrofit {
        return Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    @Provides
    fun provideGson(): Gson {
        return GsonBuilder()
                .create();
    }

    @Provides
    fun provideOkHttp(): OkHttpClient {
        return OkHttpClient();
    }
}