package com.linroid.viewit.ui.viewer

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.view.ViewGroup
import com.linroid.viewit.data.model.Image

/**
 * @author linroid <linroid@gmail.com>
 * @since 20/01/2017
 */
class ImageViewerPagerAdapter(fm: FragmentManager, val images: List<Image>?) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return ImageViewerFragment.newInstance()
    }

    override fun getCount(): Int {
        return images?.size ?: 0
    }

    override fun getItemPosition(item: Any?): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as ImageViewerFragment
        fragment.updateImage(images!![position])
        return fragment
    }
}