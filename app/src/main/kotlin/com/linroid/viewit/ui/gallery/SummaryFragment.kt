package com.linroid.viewit.ui.gallery

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import butterknife.bindView
import com.linroid.viewit.R
import com.linroid.viewit.data.ImageRepo
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.model.ImageTree
import com.linroid.viewit.ui.BaseFragment
import com.linroid.viewit.ui.gallery.provider.Category
import com.linroid.viewit.ui.gallery.provider.CategoryViewProvider
import com.linroid.viewit.ui.gallery.provider.ImageTreeViewProvider
import com.linroid.viewit.ui.gallery.provider.ImageViewProvider
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import me.drakeet.multitype.MultiTypeAdapter
import rx.android.schedulers.AndroidSchedulers
import java.io.File
import java.util.*
import javax.inject.Inject

/**
 * @author linroid <linroid@gmail.com>
 * @since 31/01/2017
 */
class SummaryFragment : BaseFragment() {
    val SPAN_COUNT = 3

    @Inject lateinit var imageRepo: ImageRepo
    @Inject lateinit var appInfo: ApplicationInfo
    @Inject lateinit var activity: GalleryActivity

    private val items = ArrayList<Any>()
    private var adapter = MultiTypeAdapter(items)

    private val recyclerView: RecyclerView by bindView(R.id.recyclerView)

    companion object {
        fun newInstance(): SummaryFragment {
            val args = Bundle()
            val fragment = SummaryFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun provideLayoutId(): Int = R.layout.fragment_summary

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if (activity is GalleryActivity) {
            activity.graph().inject(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.register(Image::class.java, ImageViewProvider(activity, imageRepo, appInfo))
        adapter.register(ImageTree::class.java, ImageTreeViewProvider(activity, File.separator, appInfo, imageRepo))
        adapter.register(Category::class.java, CategoryViewProvider())

        val gridLayoutManager = GridLayoutManager(getActivity(), SPAN_COUNT)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (items[position] is Category) SPAN_COUNT else 1
            }

        }
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.setHasFixedSize(true)

        imageRepo.registerTreeBuilder()
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(view)
                .subscribe {
                    refresh(it)
                }
    }

    private fun refresh(tree: ImageTree) {
        items.clear()
        items.add(Category(getString(R.string.label_category_recommend)))
        tree.children.forEach { subPath, imageTree ->
            addTree(imageTree)
        }
        items.add(Category(getString(R.string.label_category_favorite)))
        tree.children.forEach { subPath, imageTree ->
            addTree(imageTree)
        }
        if (tree.children.size > 0) {
            items.add(Category(getString(R.string.label_category_tree),
                    getString(R.string.label_category_action_all_images, tree.allImages().size),
                    View.OnClickListener { activity.viewGallery(tree) }))

            tree.children.forEach { subPath, imageTree ->
                addTree(imageTree)
            }
        }
        if (tree.images.size > 0) {
            items.add(Category(getString(R.string.label_category_tree_images, tree.images.size)))
            tree.images.forEach {
                items.add(it)
            }
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