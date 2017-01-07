package com.linroid.viewit.ui

import android.Manifest
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.NavUtils
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.widget.Toast
import butterknife.bindView
import com.linroid.viewit.R
import com.linroid.viewit.data.ImageScanner
import com.linroid.viewit.ui.provider.ImageViewProvider
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import me.drakeet.multitype.MultiTypeAdapter
import permissions.dispatcher.*
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

const val ARG_APP_INFO: String = "app_info";

@RuntimePermissions
class GalleryActivity : BaseActivity() {

    lateinit var appInfo: ApplicationInfo
    lateinit var appName: CharSequence

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
        initView()
        GalleryActivityPermissionsDispatcher.scanImagesWithCheck(this);
    }

    private fun initView() {
        adapter.register(File::class.java, ImageViewProvider(this))
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

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun scanImages() {
        val sdcard = Environment.getExternalStorageDirectory()
        ImageScanner.scan(File(sdcard, "Pictures/Screenshots"))
                .buffer(2000, TimeUnit.MICROSECONDS)
                .bindToLifecycle(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ files ->
                    Timber.d("found image files :$files")
                    images.addAll(files)
                    supportActionBar?.title = "$appName (${images.size} 张)"
                    adapter.notifyItemInserted(images.size - 1);
                }, { error ->
                    Timber.e(error, "onError")
                }, {
                    Timber.d("onCompleted")
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