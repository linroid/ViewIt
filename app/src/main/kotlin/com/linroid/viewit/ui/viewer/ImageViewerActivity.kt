package com.linroid.viewit.ui.viewer

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import butterknife.bindView
import com.linroid.viewit.App
import com.linroid.viewit.R
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.ioc.DaggerViewerGraph
import com.linroid.viewit.ioc.ViewerGraph
import com.linroid.viewit.ioc.module.ViewerModule
import com.linroid.viewit.ui.BaseActivity
import com.linroid.viewit.ui.ImmersiveActivity
import com.linroid.viewit.utils.ARG_APP_INFO
import com.linroid.viewit.utils.ARG_POSITION
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

/**
 * @author linroid <linroid@gmail.com>
 * @since 08/01/2017
 */
class ImageViewerActivity() : ImmersiveActivity(), View.OnClickListener {

    @Inject lateinit var observable: Observable<Image>
    private val actionSave: ImageButton by bindView(R.id.action_save)
    private val actionDelete: ImageButton by bindView(R.id.action_delete)
    private val actionShare: ImageButton by bindView(R.id.action_share)
    private val actionsContainer: LinearLayout by bindView(R.id.actions_container)

    lateinit private var appInfo: ApplicationInfo
    lateinit private var adapter: ImageViewerPagerAdapter
    lateinit internal var graph: ViewerGraph

    private var position: Int = 0
    private val viewPager: ViewPager by bindView(R.id.view_pager)

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
        val arguments: Bundle = savedInstanceState ?: intent.extras
        appInfo = arguments.getParcelable(ARG_APP_INFO)
        position = arguments.getInt(ARG_POSITION)
        graph = DaggerViewerGraph.builder()
                .globalGraph(App.get().graph())
                .viewerModule(ViewerModule(this, appInfo))
                .build()
        graph.inject(this)
        initViews();
        loadData();
    }

    private fun initViews() {
        actionShare.setOnClickListener(this)
        actionSave.setOnClickListener(this)
        actionDelete.setOnClickListener(this)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageSelected(position: Int) {
                this@ImageViewerActivity.position = position
            }
        })

        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.action_share -> {

            }
            R.id.action_save -> {

            }
            R.id.action_delete -> {

            }
        }
    }

    private fun loadData() {
        observable.observeOn(AndroidSchedulers.mainThread()).toList().subscribe({
            adapter = ImageViewerPagerAdapter(supportFragmentManager, it.size)
            viewPager.offscreenPageLimit = 2
            viewPager.adapter = adapter
            viewPager.currentItem = position
        })
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState?.putInt(ARG_POSITION, position)
        outState?.putParcelable(ARG_APP_INFO, appInfo)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_image_viewer, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun shouldHideComponents() {
        actionsContainer.visibility = View.GONE
    }

    override fun shouldShowComponents() {
        actionsContainer.visibility = View.VISIBLE
    }
}