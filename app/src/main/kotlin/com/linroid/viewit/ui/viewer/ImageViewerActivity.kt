package com.linroid.viewit.ui.viewer

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.support.v4.view.ViewPager
import butterknife.bindView
import com.linroid.viewit.App
import com.linroid.viewit.R
import com.linroid.viewit.data.ImageRepo
import com.linroid.viewit.ui.BaseActivity
import com.linroid.viewit.utils.ARG_APP_INFO
import com.linroid.viewit.utils.ARG_POSITION
import rx.android.schedulers.AndroidSchedulers
import java.io.File
import javax.inject.Inject

/**
 * @author linroid <linroid@gmail.com>
 * @since 08/01/2017
 */
class ImageViewerActivity() : BaseActivity() {
    lateinit var imageFile: File
    val viewPager: ViewPager by bindView(R.id.view_pager)
    @Inject lateinit var imageRepo: ImageRepo
    lateinit var info: ApplicationInfo
    lateinit var adapter: ImageViewerPagerAdapter
    var position: Int = 0

    companion object {
        fun navTo(source: BaseActivity, info: ApplicationInfo, position: Int) {
            val intent = Intent(source, ImageViewerActivity::class.java);
            intent.putExtra(ARG_APP_INFO, info)
            intent.putExtra(ARG_POSITION, position)
            source.startActivity(intent)
        }
    }

    override fun provideContentLayoutId(): Int = R.layout.activity_image_viewer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.get().graph().inject(this)
        val arguments: Bundle = intent.extras
        info = arguments.getParcelable(ARG_APP_INFO)
        position = arguments.getInt(ARG_POSITION)
        getObservable().observeOn(AndroidSchedulers.mainThread()).toList().subscribe({
            adapter = ImageViewerPagerAdapter(supportFragmentManager, it.size)
            viewPager.offscreenPageLimit = 2
            viewPager.adapter = adapter
            viewPager.currentItem = position
        })
    }

    fun getObservable() = imageRepo.asObservable(info)
}