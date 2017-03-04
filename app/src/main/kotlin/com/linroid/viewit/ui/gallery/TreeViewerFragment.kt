package com.linroid.viewit.ui.gallery

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import butterknife.bindView
import com.avos.avoscloud.AVAnalytics
import com.linroid.viewit.R
import com.linroid.viewit.data.model.ImageTree
import com.linroid.viewit.ui.gallery.provider.Category
import com.linroid.viewit.ui.gallery.provider.CategoryViewProvider
import com.linroid.viewit.ui.gallery.provider.ImageCategory
import com.linroid.viewit.ui.gallery.provider.ImageTreeViewProvider
import com.linroid.viewit.utils.ARG_IMAGE_TREE_PATH
import com.linroid.viewit.utils.EVENT_CLICK_CLOUD_FAVORITE
import com.linroid.viewit.utils.EVENT_CLICK_JUMP_TO_IMAGES
import com.linroid.viewit.utils.FormatUtils
import com.linroid.viewit.widget.divider.CategoryItemDecoration
import com.trello.rxlifecycle.android.FragmentEvent
import com.trello.rxlifecycle.kotlin.bindUntilEvent
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.*

/**
 * @author linroid <linroid@gmail.com>
 * @since 30/01/2017
 */
open class TreeViewerFragment : GalleryViewerFragment() {
    val SPAN_COUNT = 4

    companion object {
        fun newInstance(tree: ImageTree): TreeViewerFragment {
            val args = Bundle()
            args.putString(ARG_IMAGE_TREE_PATH, tree.dir)
            val fragment = TreeViewerFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var treeCategory: Category<ImageTree>

    val recyclerView: RecyclerView by bindView(R.id.recyclerView)
    val dirView: TextView by bindView(R.id.dir)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate:$arguments")
        setHasOptionsMenu(true)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GalleryActivity) {
            context.graph().inject(this)
        }
    }

    override fun provideLayoutId(): Int = R.layout.fragment_image_tree

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dirView.text = FormatUtils.formatPath(path, appInfo)

        adapter.register(ImageTree::class.java, ImageTreeViewProvider(activity, path, appInfo, imageRepo))
        adapter.register(Category::class.java, CategoryViewProvider())
        treeCategory = Category(null, adapter, items, getString(R.string.label_category_tree))
        imageCategory = ImageCategory(treeCategory, adapter, items, getString(R.string.label_category_tree_images, 0))

        val gridLayoutManager = GridLayoutManager(getActivity(), SPAN_COUNT)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (items[position] is Category<*>) SPAN_COUNT else 1
            }

        }
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(CategoryItemDecoration(recyclerView))

        imageRepo.registerTreeBuilder()
                .observeOn(AndroidSchedulers.mainThread())
                .bindUntilEvent(this, FragmentEvent.DESTROY_VIEW)
                .subscribe {
                    refresh(it.find(path))
                }
    }

    private fun refresh(tree: ImageTree?) {
        if (tree != null) {
            dirView.text = FormatUtils.formatPath(tree.dir, appInfo)
        }
        if (tree != null) {
            val treeItems = ArrayList<ImageTree>()
            for ((subPath, child) in tree.children) {
                treeItems.add(child.nonEmptyChild())
            }
            treeCategory.apply {
                items = treeItems
                if (treeItems.size > 4 * SPAN_COUNT) {
                    actionClickListener = View.OnClickListener {
                        AVAnalytics.onEvent(context, EVENT_CLICK_JUMP_TO_IMAGES, mapOf("count" to treeItems.size.toString(), "packageName" to appInfo.packageName))
                        recyclerView.smoothScrollToPosition(imageCategory.position + 2)
                    }
                    action = getString(R.string.label_category_action_scroll_to_images)
                } else {
                    action = null
                    actionClickListener = null
                }
            }
            updateImageTree(tree)
        } else {
            updateImageTree(null)
        }
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.gallery_tree_viewer, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }
}