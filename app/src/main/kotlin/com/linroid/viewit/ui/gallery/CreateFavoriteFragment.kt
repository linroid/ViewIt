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
import com.linroid.viewit.R
import com.linroid.viewit.data.DBRepo
import com.linroid.viewit.data.NetRepo
import com.linroid.viewit.data.model.Favorite
import com.linroid.viewit.ui.BaseDialogFragment
import com.linroid.viewit.ui.gallery.GalleryActivity
import com.linroid.viewit.utils.ARG_APP_INFO
import com.linroid.viewit.utils.ARG_IMAGE_TREE_PATH
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject


/**
 * @author linroid <linroid@gmail.com>
 * @since 01/02/2017
 */
class CreateFavoriteFragment : BaseDialogFragment() {

    @Inject
    lateinit var dbRepo: DBRepo
    @Inject
    lateinit var netRepo: NetRepo

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
                    if (checkInputs()) {
                        performSave()
                    }
                })
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
        }
    }

    override fun onResume() {
        super.onResume()
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
        val name = nameField.text.toString()
        val favorite = dbRepo.createFavorite(appInfo, path, name)
        Toast.makeText(context, R.string.msg_create_favorite_success, Toast.LENGTH_SHORT).show()
        Timber.i("save favorite success!${favorite.toString()}")
        if (shareCheckbox.isChecked) {
            uploadFavorite(favorite)
        }
        dismiss()
    }

    private fun uploadFavorite(favorite: Favorite) {
        netRepo.uploadFavorite(favorite, appInfo)
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