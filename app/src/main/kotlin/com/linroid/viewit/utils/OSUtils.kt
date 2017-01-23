package com.linroid.viewit.utils

import android.os.Build


/**
 * @author linroid <linroid@gmail.com>
 * @since 23/01/2017
 */
object OSUtils {
    fun getSupportedAbis(): Array<String> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return arrayOf(Build.CPU_ABI, Build.CPU_ABI2)
        } else {
            return Build.SUPPORTED_ABIS
        }
    }

    fun findPreferAbi(supportAbis: Array<String>, dir: Array<String>): String? {
        supportAbis.forEach { support ->
            dir.forEach {
                if (support == it) {
                    return it
                }
            }
        }
        return null
    }

}