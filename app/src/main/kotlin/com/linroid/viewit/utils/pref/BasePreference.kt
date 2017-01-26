package com.linroid.viewit.utils.pref

import android.content.SharedPreferences

/**
 * @author linroid <linroid@gmail.com>
 * @since 27/01/2017
 */
abstract class BasePreference<T>(protected val pref: SharedPreferences, protected val key: String, protected val defaultVal: T) {
    private var cachedValue: T? = null
    fun get(): T {
        if (cachedValue == null) {
            cachedValue = getVal()
        }
        return cachedValue!!
    }

    fun set(value: T) {
        cachedValue = value
        val editor = pref.edit()
        setVal(editor, value)
        editor.apply()
    }

    abstract protected fun getVal(): T
    abstract protected fun setVal(editor: SharedPreferences.Editor, value: T)
}