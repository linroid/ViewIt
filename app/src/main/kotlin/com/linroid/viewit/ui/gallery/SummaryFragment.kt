package com.linroid.viewit.ui.gallery

import android.content.Context
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import butterknife.bindView
import com.avos.avoscloud.AVAnalytics
import com.linroid.viewit.R
import com.linroid.viewit.data.model.CloudFavorite
import com.linroid.viewit.data.model.Favorite
import com.linroid.viewit.data.model.ImageTree
import com.linroid.viewit.data.repo.cloud.CloudFavoriteRepo
import com.linroid.viewit.ui.gallery.provider.*
import com.linroid.viewit.ui.path.PathManagerActivity
import com.linroid.viewit.utils.EVENT_CLICK_PATH_SETTINGS
import com.linroid.viewit.utils.EVENT_CLICK_PATH_SETTINGS_NO_IMAGE
import com.linroid.viewit.utils.PathUtils
import com.linroid.viewit.utils.RootUtils
import com.linroid.viewit.widget.divider.CategoryItemDecoration
import com.trello.rxlifecycle.android.FragmentEvent
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import com.trello.rxlifecycle.kotlin.bindUntilEvent
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject

/**
 * @author linroid <linroid@gmail.com>
 * @since 31/01/2017
 */
class SummaryFragment : GalleryChildFragment(), SwipeRefreshLayout.OnRefreshListener {
    val SPAN_COUNT = 3

    private lateinit var cloudFavoriteCategory: Category<CloudFavorite>
    private lateinit var favoriteCategory: Category<Favorite>
    private lateinit var treeCategory: Category<ImageTree>

    @Inject lateinit internal var cloudFavoriteRepo: CloudFavoriteRepo

    private val recyclerView: RecyclerView by bindView(R.id.recyclerView)
    private val refresher: SwipeRefreshLayout by bindView(R.id.refresher)
    private val noImageLayout: ViewGroup by bindView(R.id.no_image_found_layout)
    private val noImageMsgTV: TextView by bindView(R.id.no_image_msg)

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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GalleryActivity) {
            context.graph().inject(this)
        }
    }

    override fun onRefresh() {
        activity.refresh()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refresher.setOnRefreshListener(this)
        refresher.isEnabled = false

        adapter.register(ImageTree::class.java, ImageTreeViewProvider(activity, File.separator, appInfo, imageRepo))
        adapter.register(Category::class.java, CategoryViewProvider())
        adapter.register(Favorite::class.java, FavoriteViewProvider(activity, appInfo, imageRepo))
        adapter.register(CloudFavorite::class.java, CloudFavoriteViewProvider(activity, appInfo, imageRepo))

        cloudFavoriteCategory = Category(null, adapter, items, getString(R.string.label_category_recommend))
        favoriteCategory = Category(cloudFavoriteCategory, adapter, items, getString(R.string.label_category_favorite))
        treeCategory = Category(favoriteCategory, adapter, items, getString(R.string.label_category_tree))
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
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(view)
                .subscribe {
                    refresh(it)
                    if (savedInstanceState == null) {
                        if (imageRepo.hasScanned) {
                            refresher.isEnabled = true
                            refresher.isRefreshing = false
                        } else if (it != null) {
                            activity.hideLoading()
                            refresher.isRefreshing = true
                        }
                    } else {
                        activity.hideLoading()
                        refresher.isRefreshing = false
                    }
                }
        view.findViewById(R.id.btn_add_path).setOnClickListener {
            AVAnalytics.onEvent(context, EVENT_CLICK_PATH_SETTINGS_NO_IMAGE, appInfo.packageName)
            openPathManager()
        }
    }

    private fun refresh(tree: ImageTree) {
        // cloudFavorites
        cloudFavoriteRepo.list(appInfo)
                .doOnNext { cloudFavorites ->
                    cloudFavorites.forEach {
                        it.tree = tree.find(PathUtils.formatToDevice(it.path, appInfo))
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .bindUntilEvent(this, FragmentEvent.DESTROY_VIEW)
                .subscribe({ recommendations ->
                    cloudFavoriteCategory.items = recommendations
                    recyclerView.smoothScrollToPosition(0)
                }, { error ->
                    Timber.e(error, "list cloudFavorites")
                })

        // favorites
        favoriteRepo.listWithChangeObserver(appInfo)
                .observeOn(AndroidSchedulers.mainThread())
                .bindUntilEvent(this, FragmentEvent.DESTROY_VIEW)
                .doOnNext { favorites ->
                    favorites.forEachIndexed { i, favorite ->
                        val path = PathUtils.formatToDevice(favorite.path, appInfo);
                        favorite.tree = tree.find(path)
                    }
                }
                .subscribe({ favorites ->
                    favoriteCategory.items = favorites
                    recyclerView.smoothScrollToPosition(0)
                }, { error ->
                    Timber.e(error, "list favorites")
                }, {

                })

        // tree
        val treeItems = ArrayList<ImageTree>()
        for ((subPath, imageTree) in tree.children) {
            treeItems.add(imageTree.nonEmptyChild())
        }
        treeCategory.apply {
            items = treeItems
            if (treeItems.size > 4 * SPAN_COUNT) {
                actionClickListener = View.OnClickListener { recyclerView.smoothScrollToPosition(imageCategory.position + 2) }
                action = getString(R.string.label_category_action_scroll_to_images)
            } else {
                action = null
                actionClickListener = null
            }
        }

        // scannedImages
        updateImageTree(tree)


        if (imageRepo.hasScanned && tree.allImagesCount() == 0) {
            noImageLayout.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            if (!RootUtils.isRootAvailable()) {
                noImageMsgTV.text = getString(R.string.tips_to_add_path_no_root)
            } else {
                noImageMsgTV.text = getString(R.string.tips_to_add_path_no_image)
            }
        } else {
            noImageLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.gallery_summary, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_gallery_setting -> {
                AVAnalytics.onEvent(context, EVENT_CLICK_PATH_SETTINGS, appInfo.packageName)
                openPathManager()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openPathManager() {
        PathManagerActivity.navTo(activity, appInfo)
    }
}