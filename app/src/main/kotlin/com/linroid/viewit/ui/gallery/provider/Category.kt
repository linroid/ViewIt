package com.linroid.viewit.ui.gallery.provider

import android.view.View

/**
 * @author linroid <linroid@gmail.com>
 * @since 31/01/2017
 */
data class Category(val label: CharSequence,
                    val action: CharSequence? = null,
                    val actionClickListener: View.OnClickListener? = null)