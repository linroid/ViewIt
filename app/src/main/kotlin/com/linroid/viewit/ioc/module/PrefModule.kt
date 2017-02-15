package com.linroid.viewit.ioc.module

import android.content.SharedPreferences
import com.linroid.viewit.data.repo.ImageRepo.Companion.SORT_BY_PATH
import com.linroid.viewit.utils.PREF_FILTER_SIZE
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
    @Named(PREF_SORT_TYPE)
    fun provideSortTypePref(pref: SharedPreferences): LongPreference {
        return LongPreference(pref, PREF_SORT_TYPE, SORT_BY_PATH)
    }

    @Provides
    @Singleton
    @Named(PREF_FILTER_SIZE)
    fun provideFilterSizePref(pref: SharedPreferences): LongPreference {
        return LongPreference(pref, PREF_FILTER_SIZE, 0)
    }
}