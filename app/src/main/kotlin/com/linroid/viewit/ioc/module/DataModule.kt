package com.linroid.viewit.ioc.module

import android.content.Context
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.registerTypeAdapter
import com.github.salomonbrys.kotson.set
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.linroid.rxshell.RxShell
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.model.ImageTree
import com.linroid.viewit.data.model.ImageType
import com.linroid.viewit.ioc.quailifer.Root
import dagger.Module
import dagger.Provides
import timber.log.Timber
import java.io.File
import javax.inject.Singleton

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
@Module
class DataModule {
//
//    @Provides
//    @Singleton
//    fun provideRetrofit(gson: Gson, httpClient: OkHttpClient): Retrofit {
//        return Retrofit.Builder().apply {
//            addCallAdapterFactory(RxJavaCallAdapterFactory.create())
//            addConverterFactory(GsonConverterFactory.create(gson))
//        }.build();
//    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        Timber.d("provideGson")
        return GsonBuilder()
                .setDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'")
                .registerTypeAdapter<ImageTree> {
                    val imagesType = object : TypeToken<List<Image>>() {}.type
                    val childrenType = object : TypeToken<Map<String, ImageTree>>() {}.type

                    serialize { serializer ->
                        JsonObject().apply {
                            serializer.src.let {
                                set("dir", it.dir)
                                set("scannedImages", serializer.context.serialize(it.images, imagesType))
                                set("children", serializer.context.serialize(it.children, childrenType))
                            }
                        }
                    }
                    deserialize {
                        val tree = ImageTree(it.json["dir"].asString, null)
                        it.json["scannedImages"].asJsonArray.forEach { item ->
                            tree.images.add(it.context.deserialize(item))
                        }
                        val obj = it.json["children"].asJsonObject
                        for ((key, value) in obj.entrySet()) {
                            val child: ImageTree = it.context.deserialize(value)
                            child.parent = tree
                            tree.children.put(key, child)
                        }
                        return@deserialize tree
                    }
                }
                .registerTypeAdapter<Image> {
                    serialize {
                        JsonObject().apply {
                            it.src.let {
                                set("source", it.source.absolutePath)
                                set("size", it.size)
                                set("lastModified", it.lastModified)
                                set("type", it.type.value)
                            }
                        }
                    }
                    deserialize {
                        Image(
                                source = File(it.json["source"].asString),
                                size = it.json["size"].asLong,
                                lastModified = it.json["lastModified"].asLong,
                                type = ImageType.from(it.json["type"].asInt)
                        )
                    }
                }
                .create()
    }

//    @Provides
//    @Singleton
//    fun provideOkHttp(context: Context, logging: HttpLoggingInterceptor): OkHttpClient {
//        return OkHttpClient.Builder().apply {
//            val cacheDir = File(context.cacheDir, "http")
//            cache(Cache(cacheDir, 10 * 1024 * 1024L))
//            connectTimeout(10L, TimeUnit.SECONDS)
//            writeTimeout(10L, TimeUnit.SECONDS)
//            readTimeout(30L, TimeUnit.SECONDS)
//            addInterceptor(logging)
//        }.build()
//    }

//    @Singleton
//    @Provides
//    fun provideHttpLogging(): HttpLoggingInterceptor {
//        val logging = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { Timber.tag("OkHttp").d(it) })
//        logging.level = HttpLoggingInterceptor.Level.BASIC
//        return logging;
//    }

    @Singleton
    @Provides
    fun provideShell(context: Context): RxShell = RxShell(false)

    @Root
    @Singleton
    @Provides
    fun provideRootRxShell(context: Context): RxShell = RxShell(true)
}