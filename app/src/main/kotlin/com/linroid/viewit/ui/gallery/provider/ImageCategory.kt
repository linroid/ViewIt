package com.linroid.viewit.ui.gallery.provider

import com.linroid.viewit.data.model.Image
import me.drakeet.multitype.MultiTypeAdapter
import java.util.*

/**
 * @author linroid <linroid@gmail.com>
 * @since 28/02/2017
 */
class ImageCategory(prev: Category<*>?,
                    adapter: MultiTypeAdapter,
                    listItems: ArrayList<Any>,
                    label: CharSequence) : Category<Image>(prev, adapter, listItems, label, null, null, true) {

    var totalCount: Int = 0
}