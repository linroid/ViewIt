package com.linroid.viewit.ui.favorite

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.avos.avoscloud.AVAnalytics
import com.linroid.viewit.R
import com.linroid.viewit.data.repo.cloud.CloudFavoriteRepo
import com.linroid.viewit.data.repo.local.FavoriteRepo
import com.linroid.viewit.ui.BaseDialogFragment
import com.linroid.viewit.ui.gallery.GalleryActivity
import com.linroid.viewit.utils.ARG_APP_INFO
import com.linroid.viewit.utils.ARG_IMAGE_TREE_PATH
import com.linroid.viewit.utils.EVENT_SAVE_FAVORITE
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject


/**
 * @author linroid <linroid@gmail.com>
 * @since 01/02/2017
 */
class CreateFavoriteFragment : BaseDialogFragment() {

    @Inject
    lateinit var favoriteRepo: FavoriteRepo
    @Inject
    lateinit var cloudFavoriteRepo: CloudFavoriteRepo

    private lateinit var path: String
    private lateinit var appInfo: ApplicationInfo

    private lateinit var nameField: EditText
    private lateinit var shareCheckbox: CheckBox

    companion object {
        fun newInstance(context: Context, treePath: String, appInfo: ApplicationInfo): CreateFavoriteFragment {
            val args = Bundle()
            args.putString(ARG_IMAGE_TREE_PATH, treePath)
            args.putParcelable(ARG_APP_INFO, appInfo)
            val fragment = CreateFavoriteFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        path = arguments.getString(ARG_IMAGE_TREE_PATH)
        appInfo = arguments.getParcelable(ARG_APP_INFO)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(activity).inflate(R.layout.layout_dialog_create_favorite, null, false)
        nameField = view.findViewById(R.id.et_name) as EditText
        shareCheckbox = view.findViewById(R.id.checkbox_share) as CheckBox
        return AlertDialog.Builder(context)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.action_save_favorite, { dialog: DialogInterface, i: Int ->
                })
                .setCancelable(false)
                .setTitle(R.string.title_dialog_create_favorite)
                .setView(view)
                .create()
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        dialog.setOnShowListener {
            nameField.postDelayed({
                nameField.isFocusable = true
                nameField.isFocusableInTouchMode = true
                nameField.requestFocus()
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(nameField, InputMethodManager.SHOW_IMPLICIT)
            }, 300)
            if (dialog is AlertDialog) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    if (checkInputs()) {
                        performSave()
                    }
                }
            }
        }
    }

    private fun checkInputs(): Boolean {
        if (nameField.text.isNullOrBlank()) {
            return false
        }
        return true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GalleryActivity) {
            context.graph().inject(this)
        }
    }

    private fun performSave() {
        AVAnalytics.onEvent(context, EVENT_SAVE_FAVORITE, appInfo.packageName)
        val name = nameField.text.toString()
        favoriteRepo.create(appInfo, path, name)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    Timber.i("save favorite success!${it.toString()}")
                    dismiss()
                    Toast.makeText(context, R.string.msg_create_favorite_success, Toast.LENGTH_SHORT).show()
                }
                .flatMap {
                    if (shareCheckbox.isChecked) {
                        return@flatMap cloudFavoriteRepo.upload(it, appInfo)
                    } else {
                        return@flatMap Observable.just(it)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Timber.i("upload favorite success!${it.toString()}")
                }, { error ->
                    Timber.e(error)
                }, {
                    Timber.i("upload favorite success!")
                })
    }

}