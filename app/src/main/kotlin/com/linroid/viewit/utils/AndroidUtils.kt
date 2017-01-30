package com.linroid.viewit.utils

import android.content.res.Resources
import android.util.TypedValue

/**
 * @author linroid <linroid@gmail.com>
 * @since 31/01/2017
 */
object AndroidUtils {
    fun dp(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().displayMetrics)
    }

    fun sp(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dp, Resources.getSystem().displayMetrics)
    }
}