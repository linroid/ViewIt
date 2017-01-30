package com.linroid.viewit.ui.gallery

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import butterknife.bindView
import com.linroid.viewit.R
import com.linroid.viewit.data.ImageRepo
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.model.ImageTree
import com.linroid.viewit.ui.BaseFragment
import com.linroid.viewit.utils.ARG_IMAGE_TREE_PATH
import com.linroid.viewit.utils.FormatUtils
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import me.drakeet.multitype.MultiTypeAdapter
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * @author linroid <linroid@gmail.com>
 * @since 30/01/2017
 */
class ImageTreeFragment : BaseFragment() {
    companion object {
        fun newInstance(tree: ImageTree): ImageTreeFragment {
            val args = Bundle()
            args.putString(ARG_IMAGE_TREE_PATH, tree.dir)
            val fragment = ImageTreeFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var treePath: String

    @Inject lateinit var imageRepo: ImageRepo
    @Inject lateinit var appInfo: ApplicationInfo
    @Inject lateinit var activity: GalleryActivity

    private val items = ArrayList<Any>()
    private var adapter = MultiTypeAdapter(items)

    val listView: RecyclerView by bindView(R.id.recycler)
    val dirView: TextView by bindView(R.id.dir)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate:$arguments")
        treePath = arguments!!.getString(ARG_IMAGE_TREE_PATH)!!
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if (activity is GalleryActivity) {
            activity.graph().inject(this)
        }

    }

    override fun provideLayoutId(): Int = R.layout.fragment_image_tree

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.register(Image::class.java, ImageViewProvider(activity, imageRepo, appInfo))
        adapter.register(ImageTree::class.java, ImageTreeViewProvider(activity, treePath, appInfo))

        listView.layoutManager = GridLayoutManager(getActivity(), 4)
        listView.adapter = adapter
        listView.itemAnimator = DefaultItemAnimator()
        listView.setHasFixedSize(true)

        imageRepo.registerTreeBuilder()
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(view)
                .subscribe {
                    refresh(it.getChildTree(treePath))
                }
    }

    private fun refresh(tree: ImageTree?) {
        dirView.text = FormatUtils.formatPath(tree?.dir, appInfo)
        items.clear()
        tree?.children?.forEach { subPath, imageTree ->
            addTree(imageTree)
        }
        tree?.images?.forEach {
            items.add(it)
        }
        adapter.notifyDataSetChanged()
    }

    private fun addTree(tree: ImageTree) {
        if (tree.children.size == 1 && tree.images.size == 0) {
            addTree(tree.firstChild())
        } else {
            items.add(tree)
        }
    }
}