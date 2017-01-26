package com.linroid.viewit.ioc.module

import android.content.SharedPreferences
import com.linroid.viewit.utils.PREF_SORT_TYPE
import com.linroid.viewit.utils.pref.LongPreference
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

/**
 * @author linroid <linroid@gmail.com>
 * @since 27/01/2017
 */
@Module
class PrefModule {
    @Provides
    @Singleton
    fun provideSortTypePref(pref: SharedPreferences): LongPreference {
        return LongPreference(pref, PREF_SORT_TYPE, 0)
    }
}