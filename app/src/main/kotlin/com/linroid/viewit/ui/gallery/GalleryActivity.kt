package com.linroid.viewit.ui.gallery

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import butterknife.bindView
import com.linroid.viewit.App
import com.linroid.viewit.R
import com.linroid.viewit.data.ScanRepo
import com.linroid.viewit.data.ScanRepoManager
import com.linroid.viewit.data.model.Favorite
import com.linroid.viewit.data.model.ImageTree
import com.linroid.viewit.data.model.Recommendation
import com.linroid.viewit.ioc.DaggerGalleryGraph
import com.linroid.viewit.ioc.GalleryGraph
import com.linroid.viewit.ioc.module.GalleryModule
import com.linroid.viewit.ui.BaseActivity
import com.linroid.viewit.utils.ARG_APP_INFO
import com.linroid.viewit.utils.AndroidNavUtil
import com.linroid.viewit.widget.AnimatedSetView
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import permissions.dispatcher.*
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
@RuntimePermissions
class GalleryActivity : BaseActivity() {
    private val STACK_NAME = "gallery-stack"
    @Inject lateinit var scanRepo: ScanRepo
    @Inject lateinit var repoManager: ScanRepoManager
    lateinit var appInfo: ApplicationInfo
    lateinit var appName: CharSequence

    val animView: AnimatedSetView by  bindView(R.id.loading_anim)
    private lateinit var graph: GalleryGraph

    companion object {
        fun navTo(source: BaseActivity, info: ApplicationInfo) {
            val intent = Intent(source, GalleryActivity::class.java);
            intent.putExtra(ARG_APP_INFO, info)
            source.startActivity(intent)
        }
    }

    override fun provideContentLayoutId(): Int = R.layout.activity_gallery


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arguments = intent.extras
        appInfo = arguments.getParcelable(ARG_APP_INFO)
        appName = packageManager.getApplicationLabel(appInfo);
        supportActionBar?.title = appName
        graph = DaggerGalleryGraph.builder()
                .globalGraph(App.get().graph())
                .galleryModule(GalleryModule(this, appInfo))
                .build()
        graph.inject(this)
        initView()
        GalleryActivityPermissionsDispatcher.scanImagesWithCheck(this);
        showSummary()
    }

    fun graph(): GalleryGraph = graph

    private fun initView() {
        supportActionBar?.subtitle = getString(R.string.subtitle_scanning)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.gallery, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_view_app_info -> {
                AndroidNavUtil.openAppDetail(this, appInfo)
                return true
            }
            R.id.action_launch_app -> {
                AndroidNavUtil.launchApp(this, appInfo)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        repoManager.removeRepo(appInfo)
    }

    @SuppressLint("StringFormatMatches")
    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun scanImages() {
        showLoading()
        var count = 0;
        scanRepo.scan()
                .bindToLifecycle(this)
                .buffer(100, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    count += it.size
                    supportActionBar?.subtitle = getString(R.string.subtitle_scanned_images, count)
                }, { error ->
                    Timber.e(error, "onError")
                    Toast.makeText(this, getString(R.string.msg_scan_failed, error.message), Toast.LENGTH_SHORT).show()
                    hideLoading()
                }, {
                    //                    val msg = if (count > 0) getString(R.string.msg_scan_completed_with_images, count, scanRepo.images.size - count) else getString(R.string.msg_scan_completed_without_image)
//                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    hideLoading()
                })
    }

    private fun showSummary() {
        supportFragmentManager.beginTransaction()
                .add(R.id.container, SummaryFragment.newInstance(), "summary")
                .commit()
    }

    fun visitTree(tree: ImageTree) {
        Timber.d("visitTree:$tree")
        addToStack(TreeViewerFragment.newInstance(tree), "tree:${tree.dir}")
    }

    fun viewImages(tree: ImageTree) {
        Timber.d("viewImages:$tree")
        addToStack(ImagesViewerFragment.newInstance(tree), "images:${tree.dir}")
    }

    fun viewFavorite(favorite: Favorite) {
        Timber.d("viewFavorite:$favorite")
        addToStack(FavoriteViewerFragment.newInstance(favorite, appInfo), "favorite:${favorite.path}")
    }

    fun visitRecommendation(recommendation: Recommendation) {
        Timber.d("viewRecommendation:$recommendation")
        addToStack(RecommendationViewerFragment.newInstance(recommendation, appInfo), "recommendation:${recommendation.pattern}")
    }

    private fun addToStack(fragment: Fragment, name: String) {
        val count = supportFragmentManager.backStackEntryCount;
        val action = supportFragmentManager.beginTransaction()
        action.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        val topFragment = supportFragmentManager.fragments?.findLast { it != null }
        action.setCustomAnimations(R.anim.fragment_none, R.anim.fragment_none, R.anim.fragment_none, R.anim.fragment_none)
        if (topFragment != null && topFragment is GalleryChildFragment) {
            action.hide(topFragment)
        }
        action.add(R.id.container, fragment, name)
                .addToBackStack(STACK_NAME)
                .commit()
    }

    private fun showLoading() {
        animView.start()
        animView.visibility = VISIBLE
        animView.animate().alpha(1F).setDuration(300).start()
    }

    private fun hideLoading() {
        animView.visibility = VISIBLE
        animView.animate().alpha(0F).setDuration(300).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                animView.visibility = GONE
                animView.stop()
            }
        }).start()
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showRationaleForStorage(request: PermissionRequest) {
        request.proceed()
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showDeniedForStorage() {
        finish()
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showNeverAskForStorage() {
        Toast.makeText(this, R.string.permission_storage_neverask, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        GalleryActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}