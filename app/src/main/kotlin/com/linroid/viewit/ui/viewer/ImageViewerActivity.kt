package com.linroid.viewit.ui.viewer

import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import butterknife.bindView
import com.linroid.viewit.App
import com.linroid.viewit.R
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.repo.ScanRepo
import com.linroid.viewit.ioc.DaggerViewerGraph
import com.linroid.viewit.ioc.ViewerGraph
import com.linroid.viewit.ioc.module.ViewerModule
import com.linroid.viewit.ui.BaseActivity
import com.linroid.viewit.ui.ImmersiveActivity
import com.linroid.viewit.utils.ARG_APP_INFO
import com.linroid.viewit.utils.ARG_POSITION
import com.linroid.viewit.utils.FILE_PROVIDER
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

/**
 * @author linroid <linroid@gmail.com>
 * @since 08/01/2017
 */
class ImageViewerActivity() : ImmersiveActivity(), View.OnClickListener {

    @Inject lateinit var scanRepo: ScanRepo

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
        fun navTo(source: BaseActivity, scanRepo: ScanRepo, images: List<Image>, position: Int) {
            scanRepo.viewerHolderImages = images
            val intent = Intent(source, ImageViewerActivity::class.java);
            intent.putExtra(ARG_APP_INFO, scanRepo.appInfo)
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
        val image = scanRepo.viewerHolderImages!![viewPager.currentItem]
        when (v.id) {
            R.id.action_share -> {
                shareImage(image)
            }
            R.id.action_save -> {
                saveImage(image)
            }
            R.id.action_delete -> {
                deleteImage(image)
            }
        }
    }

    private fun deleteImage(image: Image) {
        AlertDialog.Builder(this)
                .setTitle(R.string.title_warning_delete_image)
                .setMessage(getString(R.string.msg_warning_delete_image, packageManager.getApplicationLabel(appInfo)))
                .setNegativeButton(android.R.string.cancel, { dialog: DialogInterface, i: Int ->
                    dialog.dismiss()
                })
                .setPositiveButton(R.string.delete_anyway, { dialog: DialogInterface, i: Int ->
                    dialog.dismiss()
                    scanRepo.deleteImage(image, appInfo)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                toastShort(getString(R.string.msg_delete_image_success))
                            }, { error ->
                                toastShort(getString(R.string.msg_delete_image_failed))
                            })
                })
                .show()

    }

    private fun saveImage(image: Image) {
        scanRepo.saveImage(image, appInfo).subscribe({ savedFile ->
            val values: ContentValues = ContentValues();
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, image.mimeType());
            values.put(MediaStore.MediaColumns.DATA, savedFile.absolutePath);
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            val relativePath = "/sdcard" + savedFile.absolutePath.substringAfter(Environment.getExternalStorageDirectory().absolutePath);
            Snackbar.make(viewPager, getString(R.string.toast_save_image_success, relativePath), Snackbar.LENGTH_SHORT)
                    .setAction(R.string.snack_action_open_saved_image, {
                        val intent: Intent = Intent();
                        intent.action = Intent.ACTION_VIEW;
                        intent.setDataAndType(uri, image.mimeType());
                        startActivity(intent);
                    }).setActionTextColor(ContextCompat.getColor(this, R.color.colorPrimary)).show();

        }, { error ->
            Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun shareImage(image: Image) {
        val photoURI = FileProvider.getUriForFile(this, FILE_PROVIDER, image.file())
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI)
        shareIntent.type = image.mimeType()
        startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.title_share_image)))
    }

    private fun loadData() {
        adapter = ImageViewerPagerAdapter(supportFragmentManager, scanRepo.viewerHolderImages)
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = adapter
        viewPager.currentItem = position
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState?.putInt(ARG_POSITION, position)
        outState?.putParcelable(ARG_APP_INFO, appInfo)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.image_viewer, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun shouldHideComponents() {
        actionsContainer.visibility = View.GONE
    }

    override fun shouldShowComponents() {
        actionsContainer.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        scanRepo.viewerHolderImages = null
    }
}