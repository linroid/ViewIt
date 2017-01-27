package com.linroid.viewit.ui.gallery

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
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
import android.widget.Toast
import butterknife.bindView
import com.linroid.viewit.App
import com.linroid.viewit.R
import com.linroid.viewit.data.*
import com.linroid.viewit.ioc.DaggerGalleryGraph
import com.linroid.viewit.ioc.module.GalleryModule
import com.linroid.viewit.ui.BaseActivity
import com.linroid.viewit.utils.ARG_APP_INFO
import com.linroid.viewit.utils.pref.LongPreference
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import me.drakeet.multitype.MultiTypeAdapter
import permissions.dispatcher.*
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
@RuntimePermissions
class GalleryActivity : BaseActivity() {
    @Inject lateinit var imageRepo: ImageRepo
    @Inject lateinit var images: MutableList<Any>
    @Inject lateinit var adapter: MultiTypeAdapter
    @Inject lateinit var sortType: LongPreference

    lateinit var appInfo: ApplicationInfo
    lateinit var appName: CharSequence

    val galleryView: RecyclerView by bindView(R.id.recycler)

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
        registerImages();
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_gallery, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        when (sortType.get()) {
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

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
//            android.R.id.home -> {
//                NavUtils.navigateUpFromSameTask(this)
//                return true
//            }
            R.id.action_view_app_info -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:" + appInfo.packageName)
                startActivity(intent)
            }
            R.id.action_sort_by_default -> {
                resortImage(SORT_BY_DEFAULT)
            }
            R.id.action_sort_by_size -> {
                resortImage(SORT_BY_SIZE)
            }
            R.id.action_sort_by_time -> {
                resortImage(SORT_BY_TIME)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun resortImage(@ImageSortType type: Long) {
        if (type == sortType.get()) {
            return
        }
        sortType.set(type)
        supportInvalidateOptionsMenu()
        imageRepo.sort(appInfo, type).subscribe {
            Timber.i("sort image completed")
        }
    }

    private fun registerImages() {
        imageRepo.register(appInfo)
                .bindToLifecycle(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ event ->
                    supportActionBar?.title = getString(R.string.title_activity_gallery_scanned, appName, event.images.size)
                    when (event.type) {
                        UPDATE_EVENT -> {
                            images.clear()
                            images.addAll(event.images)
                            adapter.notifyDataSetChanged();
                        }
                        REMOVE_EVENT -> {
                            images.removeAt(event.position)
                            adapter.notifyItemRemoved(event.position);
                        }
                        INSERT_EVENT -> {
                            images.add(event.position, event.images.get(event.position))
                            adapter.notifyItemInserted(event.position);
                        }
                    }
                })
    }

    override fun onDestroy() {
        super.onDestroy()
        imageRepo.destroy(appInfo)
    }

    @SuppressLint("StringFormatMatches")
    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun scanImages() {
        val progress: ProgressDialog = ProgressDialog(this).apply {
            setMessage(getString(R.string.msg_scanning_image))
            isIndeterminate = true
            setCancelable(false)
        }
        progress.show()
        imageRepo.scan(appInfo, sortType.get())
                .bindToLifecycle(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                }, { error ->
                    Timber.e(error, "onError")
                    progress.dismiss()
                    Toast.makeText(this, getString(R.string.msg_scan_failed, error.message), Toast.LENGTH_SHORT).show()
                }, {
                    val msg = if (images.size > 0) getString(R.string.msg_scan_completed_with_images, images.size) else getString(R.string.msg_scan_completed_without_image)
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    progress.dismiss()
                })
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showRationaleForStorage(request: PermissionRequest) {
        request.proceed()
//        AlertDialog.Builder(this)
//                .setMessage(R.string.permission_storage_rationale)
//                .setPositiveButton(R.string.button_allow, { dialog, button -> request.proceed() })
//                .setNegativeButton(R.string.button_deny, { dialog, button -> request.cancel() })
//                .show()
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