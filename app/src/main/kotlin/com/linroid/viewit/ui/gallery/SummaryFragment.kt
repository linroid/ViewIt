package com.linroid.viewit.ui.gallery

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import butterknife.bindView
import com.linroid.viewit.R
import com.linroid.viewit.data.NetRepo
import com.linroid.viewit.data.model.Favorite
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.model.ImageTree
import com.linroid.viewit.data.model.Recommendation
import com.linroid.viewit.ui.gallery.provider.*
import com.linroid.viewit.utils.PathUtils
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import me.drakeet.multitype.MultiTypeAdapter
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject

/**
 * @author linroid <linroid@gmail.com>
 * @since 31/01/2017
 */
class SummaryFragment : GalleryChildFragment() {
    val SPAN_COUNT = 3

    private val items = ArrayList<Any>()
    private var adapter = MultiTypeAdapter(items)

    private lateinit var recommendCategory: Category
    private lateinit var favoriteCategory: Category
    private lateinit var treeCategory: Category
    private lateinit var imageCategory: Category

    @Inject lateinit internal var netRepo: NetRepo

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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GalleryActivity) {
            context.graph().inject(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.register(Image::class.java, ImageViewProvider(activity, scanRepo, appInfo))
        adapter.register(ImageTree::class.java, ImageTreeViewProvider(activity, File.separator, appInfo, scanRepo))
        adapter.register(Category::class.java, CategoryViewProvider())
        adapter.register(Favorite::class.java, FavoriteViewProvider(activity, appInfo, scanRepo))
        adapter.register(Recommendation::class.java, RecommendationViewProvider(activity, appInfo, scanRepo))
        recommendCategory = Category(null, getString(R.string.label_category_recommend), items)
        favoriteCategory = Category(recommendCategory, getString(R.string.label_category_favorite), items)
        treeCategory = Category(favoriteCategory, getString(R.string.label_category_tree), items)
        imageCategory = Category(treeCategory, getString(R.string.label_category_tree_images, 0), items)


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

        scanRepo.registerTreeBuilder()
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(view)
                .subscribe {
                    refresh(it)
                }
    }

    private fun refresh(tree: ImageTree) {
        resetData()
        // tree
        val treeItems = ArrayList<ImageTree>()
        tree.children.forEach { subPath, imageTree ->
            treeItems.add(imageTree.nonEmptyChild())
        }
        treeCategory.apply {
            items = treeItems
            actionClickListener = View.OnClickListener { activity.viewImages(tree) }
            action = getString(R.string.label_category_action_all_images, tree.allImagesCount())
        }

        // images
        imageCategory.label = getString(R.string.label_category_action_all_images, tree.images.size)
        imageCategory.items = tree.images

        // recommendation
        netRepo.listRecommendations(appInfo)
                .doOnNext { recommendations ->
                    recommendations.forEach {
                        it.tree = tree.match(PathUtils.formatToDevice(it.pattern, appInfo))
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(this)
                .subscribe({ recommendations ->
                    recommendCategory.items = recommendations
                }, { error ->
                    Timber.e(error, "listFavorites recommendation")
                })

        // favorites
        dbRepo.listFavorites(appInfo)
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(this)
                .doOnNext { favorites ->
                    favorites.forEachIndexed { i, favorite ->
                        val path = PathUtils.formatToDevice(favorite.path, appInfo);
                        favorite.tree = tree.find(path)
                    }
                }
                .subscribe({ favorites ->
                    favoriteCategory.items = favorites
                    adapter.notifyDataSetChanged()
                }, { error ->
                    Timber.e(error, "listFavorites favorite")
                }, {

                })
        adapter.notifyDataSetChanged()
    }

    private fun resetData() {
        favoriteCategory.items = null
        recommendCategory.items = null
    }

}