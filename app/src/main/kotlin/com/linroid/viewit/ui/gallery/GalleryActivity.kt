package com.linroid.viewit.ui.gallery

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.widget.Toast
import butterknife.bindView
import com.linroid.viewit.App
import com.linroid.viewit.R
import com.linroid.viewit.data.ImageRepo
import com.linroid.viewit.data.file.RootFileManager
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.ui.BaseActivity
import com.linroid.viewit.ui.gallery.GalleryActivityPermissionsDispatcher
import com.linroid.viewit.ui.gallery.ImageViewProvider
import com.linroid.viewit.utils.RootUtils
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.item_image.*
import me.drakeet.multitype.MultiTypeAdapter
import permissions.dispatcher.*
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val ARG_APP_INFO: String = "app_info";

@RuntimePermissions
class GalleryActivity : BaseActivity() {

    lateinit var appInfo: ApplicationInfo
    lateinit var appName: CharSequence
    @Inject lateinit var imageRepo: ImageRepo
    @Inject lateinit var rootFileManager: RootFileManager

    val galleryView: RecyclerView by bindView(R.id.recycler)
    val images: MutableList<Any> = ArrayList();
    val adapter: MultiTypeAdapter = MultiTypeAdapter(images)

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
        appInfo = intent.getParcelableExtra(ARG_APP_INFO)
        appName = packageManager.getApplicationLabel(appInfo);
        supportActionBar?.title = appName
        App.get().graph().inject(this)
        initView()
        GalleryActivityPermissionsDispatcher.scanImagesWithCheck(this);
        if (RootUtils.isRootAvailable()) {
            RootUtils.requireRoot()
        }
    }

    private fun initView() {
        adapter.register(Image::class.java, ImageViewProvider(this, appInfo))
        val gridLayoutManager = GridLayoutManager(this, 4);
        galleryView.layoutManager = gridLayoutManager
        galleryView.adapter = adapter
        galleryView.itemAnimator = DefaultItemAnimator()
        galleryView.setHasFixedSize(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("StringFormatMatches")
    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun scanImages() {
        imageRepo.scan(appInfo)
                .buffer(500, TimeUnit.MILLISECONDS)
                .bindToLifecycle(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ files ->
                    if (files.size == 0) {
                        return@subscribe
                    }
                    images.addAll(files)
                    supportActionBar?.title = "$appName (${images.size} 张)"
                    adapter.notifyItemRangeInserted(images.size - files.size, images.size - 1);
                }, { error ->
                    Timber.e(error, "onError")
                }, {
                    Timber.d("onCompleted")
                    Toast.makeText(this, getString(R.string.msg_scan_completed, images.size), Toast.LENGTH_SHORT).show()
                })
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showRationaleForStorage(request: PermissionRequest) {
        AlertDialog.Builder(this)
                .setMessage(R.string.permission_storage_rationale)
                .setPositiveButton(R.string.button_allow, { dialog, button -> request.proceed() })
                .setNegativeButton(R.string.button_deny, { dialog, button -> request.cancel() })
                .show()
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showDeniedForStorage() {
        finish();
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