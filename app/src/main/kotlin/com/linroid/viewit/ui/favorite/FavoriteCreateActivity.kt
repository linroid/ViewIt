package com.linroid.viewit.ui.favorite

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import butterknife.bindView
import com.linroid.viewit.App
import com.linroid.viewit.R
import com.linroid.viewit.data.DBRepo
import com.linroid.viewit.data.RecommendationRepo
import com.linroid.viewit.ui.BaseActivity
import com.linroid.viewit.utils.ARG_APP_INFO
import com.linroid.viewit.utils.ARG_IMAGE_TREE_PATH
import com.linroid.viewit.utils.PathUtils
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject

/**
 * @author linroid <linroid@gmail.com>
 * @since 01/02/2017
 */
class FavoriteCreateActivity : BaseActivity() {

    @Inject
    lateinit var DBRepo: DBRepo
    @Inject
    lateinit var recommendationRepo: RecommendationRepo

    private lateinit var treePath: String
    private lateinit var appInfo: ApplicationInfo

    private val nameField: EditText by bindView(R.id.et_name)
    private val pathField: EditText by bindView(R.id.et_path)
    private val matchedPathsLabel: TextView by bindView(R.id.matched_paths)
    private val tipsLabel: TextView by bindView(R.id.tips)

    companion object {
        fun navTo(context: Context, treePath: String, appInfo: ApplicationInfo) {
            val args = Bundle()
            args.putString(ARG_IMAGE_TREE_PATH, treePath)
            args.putParcelable(ARG_APP_INFO, appInfo)
            val intent = Intent(context, FavoriteCreateActivity::class.java)
            intent.putExtras(args)
            context.startActivity(intent)
        }
    }


    override fun provideContentLayoutId(): Int = R.layout.activity_favorite_create

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = intent.extras
        treePath = args.getString(ARG_IMAGE_TREE_PATH)
        appInfo = args.getParcelable(ARG_APP_INFO)

        pathField.setText(PathUtils.formatToVariable(treePath, appInfo))

        App.graph.inject(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.favorite_create, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save_favorite -> {
                if (checkInputs()) {
                    performSave()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkInputs(): Boolean {
        if (pathField.text.isNullOrBlank()) {
            return false
        }
        if (nameField.text.isNullOrBlank()) {
            return false
        }
        return true
    }

    private fun performSave() {
        val pattern = pathField.text.toString()
        val name = nameField.text.toString()
        val favorite = DBRepo.create(appInfo, pattern, name)
        Timber.i("save favorite success!${favorite.toString()}")
        recommendationRepo.uploadFavorite(favorite, appInfo)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Timber.i("upload favorite success!${it.toString()}")
                    toastShort(R.string.msg_create_favorite_success)
                    finish()
                }, { error ->
                    Timber.e(error)
                })
    }
}