package com.linroid.viewit.utils.pref

import android.content.SharedPreferences

/**
 * @author linroid <linroid@gmail.com>
 * @since 27/01/2017
 */
class LongPreference(pref: SharedPreferences, key: String, defaultVal: Long) : BasePreference<Long>(pref, key, defaultVal) {
    override fun getVal(): Long {
        return pref.getLong(key, defaultVal)
    }

    override fun setVal(editor: SharedPreferences.Editor, value: Long) {
        editor.putLong(key, value)
    }
}