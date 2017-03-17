package com.linroid.viewit.ui.gallery

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.pm.ApplicationInfo
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.support.v7.view.ActionMode
import android.view.*
import com.avos.avoscloud.AVAnalytics
import com.linroid.viewit.R
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.repo.ImageRepo
import com.linroid.viewit.ui.BaseActivity
import com.linroid.viewit.ui.gallery.provider.Category
import com.linroid.viewit.ui.gallery.provider.ImageCategory
import com.linroid.viewit.utils.*
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.onErrorReturnNull

/**
 * @author linroid <linroid@gmail.com>
 * @since 16/03/2017
 */
class ImageMultiOptionsController(val activity: BaseActivity,
                                  val provider: ImageSelectProvider,
                                  val appInfo: ApplicationInfo,
                                  val imageRepo: ImageRepo,
                                  val imageCategory: ImageCategory) : ActionMode.Callback, Category.OnItemsChangedListener<Image>, View.OnClickListener {
    private lateinit var widgetView: View

    fun attach() {
        val container = activity.findViewById(Window.ID_ANDROID_CONTENT) as ViewGroup
        activity.startSupportActionMode(this)
        widgetView = LayoutInflater.from(activity).inflate(R.layout.widget_image_multi_options, container, false)
        container.addView(widgetView)
        imageCategory.invalidate()
        imageCategory.registerChangedListener(this)
        widgetView.findViewById(R.id.action_share).setOnClickListener(this)
        widgetView.findViewById(R.id.action_delete).setOnClickListener(this)
        widgetView.findViewById(R.id.action_save).setOnClickListener(this)
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_select_all -> {
                provider.selectAll(imageCategory.items)
                imageCategory.invalidate()
            }
            R.id.action_unselect_all -> {
                provider.unSelectAll()
                imageCategory.invalidate()
            }
        }
        return true
    }

    private lateinit var mode: ActionMode

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        this.mode = mode
        activity.menuInflater.inflate(R.menu.image_multi_actions, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        widgetView.removeFromParent()
        provider.reset()
        imageCategory.invalidate()
        imageCategory.unregisterChangedListener(this)
    }

    override fun onChanged(oldVal: List<Image>?, newVal: List<Image>?) {
        provider.filter(newVal)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.action_share -> {
                AVAnalytics.onEvent(activity, EVENT_BATCH_SHARE_IMAGE)
                batchShareImage(provider.selectedItems())
            }
            R.id.action_save -> {
                AVAnalytics.onEvent(activity, EVENT_BATCH_SAVE_IMAGE)
                batchSaveImage(provider.selectedItems())
            }
            R.id.action_delete -> {
                AVAnalytics.onEvent(activity, EVENT_BATCH_DELETE_IMAGE)
                batchDeleteImage(provider.selectedItems())
            }
        }
    }

    private fun batchDeleteImage(selectedItems: Set<Image>) {
        if (selectedItems.isEmpty()) {
            activity.toastShort(R.string.msg_require_select_image)
            return
        }
        val progress: ProgressDialog = ProgressDialog(activity)
        progress.isIndeterminate = true
        progress.setMessage(activity.getString(R.string.msg_batch_deleting_image))
        progress.show()
        AlertDialog.Builder(activity)
                .setTitle(R.string.title_warning_delete_image)
                .setMessage(activity.getString(R.string.msg_warning_batch_delete_image, selectedItems.size, activity.packageManager.getApplicationLabel(appInfo)))
                .setNegativeButton(android.R.string.cancel, { dialog: DialogInterface, i: Int ->
                    dialog.dismiss()
                })
                .setPositiveButton(R.string.delete_anyway, { dialog: DialogInterface, i: Int ->
                    dialog.dismiss()
                    imageRepo.deleteImages(selectedItems.toList(), appInfo)
                            .observeOn(AndroidSchedulers.mainThread())
                            .onMain()
                            .onErrorReturnNull()
                            .subscribe({
                            }, { error ->
                                activity.toastShort(R.string.msg_delete_image_failed)
                                progress.dismiss()
                            }, {
                                activity.toastShort(activity.getString(R.string.msg_batch_delete_image_success, selectedItems.size))
                                mode.finish()
                                imageCategory.invalidate(

                                )
                                progress.dismiss()
                            })
                })
                .show()
    }

    private fun batchSaveImage(selectedItems: Set<Image>) {
        if (selectedItems.isEmpty()) {
            activity.toastShort(R.string.msg_require_select_image)
            return
        }
        val progress: ProgressDialog = ProgressDialog(activity)
        progress.isIndeterminate = true
        progress.setMessage(activity.getString(R.string.msg_batch_saving_image))
        progress.show()
        if (selectedItems.isEmpty()) {
            activity.toastShort(R.string.msg_require_select_image)
            return
        }
        imageRepo.saveImages(selectedItems.toList(), appInfo)
                .onMain()
                .onErrorReturnNull()
                .subscribe({ pair ->
                    if (pair == null) {
                        return@subscribe
                    }
                    val values: ContentValues = ContentValues();
                    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                    values.put(MediaStore.Images.Media.MIME_TYPE, pair.first.mimeType());
                    values.put(MediaStore.MediaColumns.DATA, pair.second.absolutePath);
                    activity.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                }, { error ->
                    activity.toastShort(error.message!!)
                    progress.dismiss()
                }, {
                    activity.toastLong(activity.getString(R.string.msg_batch_save_image_success, selectedItems.size))
                    mode.finish()
                    progress.dismiss()
                })
    }

    private fun batchShareImage(selectedItems: Set<Image>) {
        if (selectedItems.isEmpty()) {
            activity.toastShort(R.string.msg_require_select_image)
            return
        }
        mode.finish()
    }
}
