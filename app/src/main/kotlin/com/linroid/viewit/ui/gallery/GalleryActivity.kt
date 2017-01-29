package com.linroid.viewit.ui.gallery

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import butterknife.bindView
import com.linroid.viewit.App
import com.linroid.viewit.R
import com.linroid.viewit.data.*
import com.linroid.viewit.ioc.DaggerGalleryGraph
import com.linroid.viewit.ioc.module.GalleryModule
import com.linroid.viewit.ui.BaseActivity
import com.linroid.viewit.utils.ARG_APP_INFO
import com.linroid.viewit.utils.PREF_FILTER_SIZE
import com.linroid.viewit.utils.PREF_SORT_TYPE
import com.linroid.viewit.utils.pref.LongPreference
import com.linroid.viewit.widget.AnimatedSetView
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import me.drakeet.multitype.MultiTypeAdapter
import permissions.dispatcher.*
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
@RuntimePermissions
class GalleryActivity : BaseActivity() {
    @Inject lateinit var imageRepo: ImageRepo
    @Inject lateinit var images: MutableList<Any>
    @Inject lateinit var adapter: MultiTypeAdapter
    @field:[Inject Named(PREF_SORT_TYPE)]
    lateinit var sortTypePref: LongPreference
    @field:[Inject Named(PREF_FILTER_SIZE)]
    lateinit var filterSizePref: LongPreference

    lateinit var appInfo: ApplicationInfo
    lateinit var appName: CharSequence

    val galleryView: RecyclerView by bindView(R.id.recycler)
    val animView: AnimatedSetView by  bindView(R.id.loading_anim)

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
        DaggerGalleryGraph.builder()
                .globalGraph(App.get().graph())
                .galleryModule(GalleryModule(this, appInfo))
                .build()
                .inject(this)
        initView()
        GalleryActivityPermissionsDispatcher.scanImagesWithCheck(this);
    }

    private fun initView() {
        val gridLayoutManager = GridLayoutManager(this, 4);
        galleryView.layoutManager = gridLayoutManager
        galleryView.adapter = adapter
        galleryView.itemAnimator = DefaultItemAnimator()
        galleryView.setHasFixedSize(true)
        registerImages()
        supportActionBar?.subtitle = "扫描中..."
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_gallery, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        when (sortTypePref.get()) {
            SORT_BY_DEFAULT -> {
                menu.findItem(R.id.action_sort_by_default).isChecked = true
            }
            SORT_BY_SIZE -> {
                menu.findItem(R.id.action_sort_by_size).isChecked = true
            }
            SORT_BY_TIME -> {
                menu.findItem(R.id.action_sort_by_time).isChecked = true
            }
        }
        when (filterSizePref.get()) {
            0L -> {
                menu.findItem(R.id.action_filter_none).isChecked = true
            }
            30L -> {
                menu.findItem(R.id.action_filter_30K).isChecked = true
            }
            100L -> {
                menu.findItem(R.id.action_filter_100K).isChecked = true
            }
            300L -> {
                menu.findItem(R.id.action_filter_300K).isChecked = true
            }
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.groupId) {
            R.id.action_sort -> {
                handleSortAction(item)
                return true
            }
            R.id.action_filter -> {
                handleFilterAction(item)
                return true
            }
        }
        when (item.itemId) {
            R.id.action_view_app_info -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:" + appInfo.packageName)
                startActivity(intent)
                return true
            }

        }

        return super.onOptionsItemSelected(item)
    }

    private fun handleFilterAction(item: MenuItem) {
        var size: Long = 0
        when (item.itemId) {
            R.id.action_filter_none -> size = 0
            R.id.action_filter_30K -> size = 30
            R.id.action_filter_100K -> size = 100
            R.id.action_filter_300K -> size = 300
        }
        if (size == filterSizePref.get()) {
            return
        }
        filterSizePref.set(size)
        supportInvalidateOptionsMenu()
    }

    private fun handleSortAction(item: MenuItem) {
        var type = SORT_BY_DEFAULT
        when (item.itemId) {
            R.id.action_sort_by_default -> {
                type = SORT_BY_DEFAULT
            }
            R.id.action_sort_by_size -> {
                type = SORT_BY_SIZE
            }
            R.id.action_sort_by_time -> {
                type = SORT_BY_TIME
            }
        }
        if (type == sortTypePref.get()) {
            return
        }
        sortTypePref.set(type)
        supportInvalidateOptionsMenu()
    }

    private fun registerImages() {
        filterSizePref.listen()
                .mergeWith(sortTypePref.listen())
                .flatMap { imageRepo.refresh() }
                .bindToLifecycle(this)
                .subscribe { Timber.i("refresh images success") }
        imageRepo.register()
                .bindToLifecycle(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ event ->
                    when (event.type) {
                        UPDATE_EVENT -> {
                            images.clear()
                            images.addAll(event.images)
                            adapter.notifyDataSetChanged()
                            updateImageCount()
                        }
                        REMOVE_EVENT -> {
                            images.removeAt(event.position)
                            adapter.notifyItemRemoved(event.position);
                            updateImageCount()
                        }
                        INSERT_EVENT -> {
                            images.add(event.position, event.images[event.position])
                            adapter.notifyItemInserted(event.position);
                            updateImageCount()
                        }
                    }
                })
    }

    private fun updateImageCount() {
        if (imageRepo.originImages.size > 0) {
            supportActionBar?.subtitle = getString(R.string.subtitle_displayed_images, images.size, imageRepo.originImages.size)
        } else {
            supportActionBar?.subtitle = getString(R.string.image_not_found)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    @SuppressLint("StringFormatMatches")
    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun scanImages() {
        showLoading()
        var count = 0;
        imageRepo.scan()
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
                    val msg = if (count > 0) getString(R.string.msg_scan_completed_with_images, count, imageRepo.originImages.size - count) else getString(R.string.msg_scan_completed_without_image)
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    hideLoading()
                    updateImageCount()
                })
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
}