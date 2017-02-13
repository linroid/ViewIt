package com.linroid.viewit.ui.path

import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import com.linroid.viewit.App
import com.linroid.viewit.R
import com.linroid.viewit.data.model.CloudScanPath
import com.linroid.viewit.data.model.ScanPath
import com.linroid.viewit.data.repo.cloud.CloudPathRepo
import com.linroid.viewit.data.repo.local.PathRepo
import com.linroid.viewit.ui.BaseActivity
import com.linroid.viewit.ui.BaseListActivity
import com.linroid.viewit.ui.gallery.provider.Category
import com.linroid.viewit.ui.gallery.provider.CategoryViewProvider
import com.linroid.viewit.ui.path.provider.BuildInPath
import com.linroid.viewit.ui.path.provider.BuildInPathViewProvider
import com.linroid.viewit.ui.path.provider.CloudScanPathViewProvider
import com.linroid.viewit.ui.path.provider.ScanPathViewProvider
import com.linroid.viewit.utils.ARG_APP_INFO
import com.linroid.viewit.utils.onMain
import com.linroid.viewit.widget.divider.CategoryItemDecoration
import com.linroid.viewit.widget.divider.DividerDecoration
import com.nononsenseapps.filepicker.FilePickerActivity
import com.nononsenseapps.filepicker.Utils
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject


/**
 * @author linroid <linroid@gmail.com>
 * @since 10/02/2017
 */
class PathManagerActivity : BaseListActivity() {
    @Inject lateinit var localPathRepo: PathRepo
    @Inject lateinit var cloudPathRepo: CloudPathRepo

    val items = Items()
    val adapter = MultiTypeAdapter(items)
    val buildInCategory = Category<BuildInPath>(null, adapter, items, App.get().getString(R.string.category_built_in_path))
    val cloudCategory = Category<CloudScanPath>(buildInCategory, adapter, items, App.get().getString(R.string.category_cloud_path))
    val localCategory = Category<ScanPath>(cloudCategory, adapter, items, App.get().getString(R.string.category_local_path))
    private lateinit var appInfo: ApplicationInfo

    companion object {
        private val REQ_PICK_DIRECTORY = 0x1

        fun navTo(source: BaseActivity, info: ApplicationInfo) {
            val intent = Intent(source, PathManagerActivity::class.java);
            intent.putExtra(ARG_APP_INFO, info)
            source.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        App.graph.inject(this)
        appInfo = intent.getParcelableExtra(ARG_APP_INFO)
        super.onCreate(savedInstanceState)
        loadPaths()
    }

    override fun setupRecyclerView(recyclerView: RecyclerView) {
        adapter.register(Category::class.java, CategoryViewProvider())
        adapter.register(BuildInPath::class.java, BuildInPathViewProvider())
        adapter.register(CloudScanPath::class.java, CloudScanPathViewProvider(appInfo))
        adapter.register(ScanPath::class.java, ScanPathViewProvider(appInfo))
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(CategoryItemDecoration(recyclerView))
        recyclerView.addItemDecoration(DividerDecoration(recyclerView.context, DividerDecoration.VERTICAL_LIST))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.path_manager, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_path -> {
                performPickDirectory()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadPaths() {
        buildInCategory.items = ArrayList<BuildInPath>().apply {
            add(BuildInPath(getString(R.string.path_format_internal_data)))
            add(BuildInPath(getString(R.string.path_format_external_data)))
        }
        cloudPathRepo.list(appInfo)
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(this)
                .subscribe({ cloudPaths ->
                    cloudCategory.items = cloudPaths
                }, { error ->
                    Timber.e(error, "list cloud path")
                })
        localPathRepo.list(appInfo)
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(this)
                .subscribe({ localPaths ->
                    localCategory.items = localPaths
                }, { error ->
                    Timber.e(error, "list local path")
                })
    }

    private fun performPickDirectory() {
        val intent = Intent(this, FilePickerActivity::class.java)
        intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true)
        intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false)
        intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR)
        intent.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().path)
        startActivityForResult(intent, REQ_PICK_DIRECTORY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_PICK_DIRECTORY && resultCode == Activity.RESULT_OK) {
            Timber.i("pick directory success!")
            val paths = data!!.getStringArrayListExtra(FilePickerActivity.EXTRA_PATHS)
            if (paths != null) {
                val pickedPaths = paths.map { it ->
                    val uri = Uri.parse(it)
                    val file = Utils.getFileForUri(uri)
                    return@map file.absolutePath
                }
                performSavePickedPath(pickedPaths)
            }
        }
    }

    private fun performSavePickedPath(pickedPaths: List<String>) {
        Observable.from(pickedPaths)
                .flatMap { path -> localPathRepo.create(appInfo, path) }
                .toList()
                .subscribeOn(Schedulers.io())
                .onMain()
                .subscribe({ result ->
                    Timber.i("performSavePickedPath succeed $result")
                    toastLong(R.string.msg_save_picked_path_successed)
                }, { error ->
                    Timber.e(error, "save picked paths:$pickedPaths")
                })
    }
}