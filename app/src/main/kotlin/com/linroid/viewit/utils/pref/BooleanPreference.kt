package com.linroid.viewit.utils.pref

import android.content.SharedPreferences

/**
 * @author linroid <linroid@gmail.com>
 * @since 27/01/2017
 */
class BooleanPreference(pref: SharedPreferences, key: String, defaultVal: Boolean) : BasePreference<Boolean>(pref, key, defaultVal) {
    override fun getVal(): Boolean {
        return pref.getBoolean(key, defaultVal)
    }

    override fun setVal(editor: SharedPreferences.Editor, value: Boolean) {
        editor.putBoolean(key, value)
    }

}