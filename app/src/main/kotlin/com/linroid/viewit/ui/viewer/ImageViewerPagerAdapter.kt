package com.linroid.viewit.ui.viewer

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 * @author linroid <linroid@gmail.com>
 * @since 20/01/2017
 */
class ImageViewerPagerAdapter(fm: FragmentManager, val imageCount: Int) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return ImageViewerFragment.newInstance(position)
    }

    override fun getCount(): Int {
        return imageCount
    }
}