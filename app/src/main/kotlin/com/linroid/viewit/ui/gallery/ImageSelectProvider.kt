package com.linroid.viewit.ui.gallery

import com.linroid.viewit.data.model.Image

/**
 * @author linroid <linroid@gmail.com>
 * @since 16/03/2017
 */
interface ImageSelectProvider {
    fun selectAll(items: List<Image>?)
    fun unSelectAll()
    fun filter(filters: List<Image>?)
    fun selectedItems(): Set<Image>
    fun reset()
}