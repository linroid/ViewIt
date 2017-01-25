package com.linroid.viewit.ui.viewer

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.view.ViewGroup
/**
 * @author linroid <linroid@gmail.com>
 * @since 20/01/2017
 */
class ImageViewerPagerAdapter(fm: FragmentManager, val imageCount: Int) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return ImageViewerFragment.newInstance(position)
    }

    override fun getCount(): Int {
        return imageCount
    }

    override fun getItemPosition(`object`: Any?): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as ImageViewerFragment
        fragment.updatePosition(position)
        return fragment
    }
}