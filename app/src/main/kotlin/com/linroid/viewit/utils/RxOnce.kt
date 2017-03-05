package com.linroid.viewit.utils

import android.content.Context
import com.linroid.viewit.App
import rx.Observable
import java.util.*

/**
 * @author linroid <linroid@gmail.com>
 * @since 27/01/2017
 */
object RxOnce {
    /**
     * 已运行的次数
     */
    private val processMap = HashMap<String, Int>()

    init {
        App.graph.inject(this)
    }

    /**
     * 整个 APP 生命周期中
     */
    fun app(key: String, maxTimes: Int = 1): Observable<Boolean> {
        val pref = App.get().getSharedPreferences("once", Context.MODE_PRIVATE)
        return Observable.just(key)
                .map { pref.getInt(key, 0) + 1 }
                .doOnNext {
                    if (it <= maxTimes) {
                        pref.edit().putInt(key, it).apply()
                    }
                }
                .map { it <= maxTimes }
    }

    /**
     * 进程的生命周期中
     */
    fun process(key: String, maxTimes: Int = 1): Observable<Int> {
        return Observable.just(key)
                .map { (processMap[key] ?: 0) + 1 }
                .filter { it <= maxTimes }
                .doOnNext { processMap.put(key, it) }
    }

}