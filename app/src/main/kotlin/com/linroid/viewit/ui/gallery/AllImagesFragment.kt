package com.linroid.viewit.ui.gallery

import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import butterknife.bindView
import com.linroid.viewit.R
import com.linroid.viewit.data.ImageRepo
import com.linroid.viewit.data.SORT_BY_PATH
import com.linroid.viewit.data.SORT_BY_SIZE
import com.linroid.viewit.data.SORT_BY_TIME
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.model.ImageTree
import com.linroid.viewit.ui.BaseFragment
import com.linroid.viewit.ui.gallery.provider.Category
import com.linroid.viewit.ui.gallery.provider.CategoryViewProvider
import com.linroid.viewit.ui.gallery.provider.ImageViewProvider
import com.linroid.viewit.utils.ARG_IMAGE_TREE_PATH
import com.linroid.viewit.utils.PREF_FILTER_SIZE
import com.linroid.viewit.utils.PREF_SORT_TYPE
import com.linroid.viewit.utils.pref.LongPreference
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import me.drakeet.multitype.MultiTypeAdapter
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.filterNotNull
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Named

/**
 * @author linroid <linroid@gmail.com>
 * @since 31/01/2017
 */
class AllImagesFragment : BaseFragment() {
    private val SPAN_COUNT = 4

    @field:[Inject Named(PREF_SORT_TYPE)]
    lateinit var sortTypePref: LongPreference
    @field:[Inject Named(PREF_FILTER_SIZE)]
    lateinit var filterSizePref: LongPreference

    @Inject lateinit var imageRepo: ImageRepo
    @Inject lateinit var appInfo: ApplicationInfo
    @Inject lateinit var activity: GalleryActivity

    private val items = ArrayList<Any>()
    private var adapter = MultiTypeAdapter(items)
    private lateinit var treePath: String

    private val recyclerView: RecyclerView by bindView(R.id.recyclerView)

    companion object {
        fun newInstance(tree: ImageTree): AllImagesFragment {
            val args = Bundle()
            args.putString(ARG_IMAGE_TREE_PATH, tree.dir)
            val fragment = AllImagesFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun provideLayoutId(): Int = R.layout.fragment_all_images

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        treePath = arguments!!.getString(ARG_IMAGE_TREE_PATH)!!
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if (activity is GalleryActivity) {
            activity.graph().inject(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_all_images, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.expandActionView()
        when (item.groupId) {
            R.id.action_sort -> {
                handleSortAction(item)
                return true
            }
            R.id.action_filter -> {
                handleFilterAction(item)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        when (sortTypePref.get()) {
            SORT_BY_PATH -> {
                menu.findItem(R.id.action_sort_by_default).isChecked = true
            }
            SORT_BY_SIZE -> {
                menu.findItem(R.id.action_sort_by_size).isChecked = true
            }
            SORT_BY_TIME -> {
                menu.findItem(R.id.action_sort_by_time).isChecked = true
            }
        }
        when (filterSizePref.get()) {
            0L -> {
                menu.findItem(R.id.action_filter_none).isChecked = true
            }
            30L -> {
                menu.findItem(R.id.action_filter_30K).isChecked = true
            }
            100L -> {
                menu.findItem(R.id.action_filter_100K).isChecked = true
            }
            300L -> {
                menu.findItem(R.id.action_filter_300K).isChecked = true
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    private fun handleFilterAction(item: MenuItem) {
        var size: Long = 0
        when (item.itemId) {
            R.id.action_filter_none -> size = 0
            R.id.action_filter_30K -> size = 30
            R.id.action_filter_100K -> size = 100
            R.id.action_filter_300K -> size = 300
        }
        if (size == filterSizePref.get()) {
            return
        }
        filterSizePref.set(size)
        supportInvalidateOptionsMenu()
    }

    private fun handleSortAction(item: MenuItem) {
        var type = SORT_BY_PATH
        when (item.itemId) {
            R.id.action_sort_by_default -> {
                type = SORT_BY_PATH
            }
            R.id.action_sort_by_size -> {
                type = SORT_BY_SIZE
            }
            R.id.action_sort_by_time -> {
                type = SORT_BY_TIME
            }
        }
        if (type == sortTypePref.get()) {
            return
        }
        sortTypePref.set(type)
        supportInvalidateOptionsMenu()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.register(Image::class.java, ImageViewProvider(activity, imageRepo, appInfo))
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

        registerComponents(view)
    }

    private fun registerComponents(view: View) {
//        imageRepo.registerImageEvent()
//                .bindToLifecycle(view)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ event ->
//                    when (event.type) {
//                        UPDATE_EVENT -> {
//                            images.clear()
//                            images.addAll(event.effectedImages)
//
//                            adapter.notifyDataSetChanged()
//                            recyclerView.smoothScrollToPosition(0)
//                        }
//                        REMOVE_EVENT -> {
//                            images.removeAll(event.effectedImages)
//                            adapter.notifyItemRangeRemoved(event.position, event.effectCount)
//                        }
//                        INSERT_EVENT -> {
//                            images.addAll(event.position, event.effectedImages)
//                            adapter.notifyItemRangeInserted(event.position, event.effectCount)
//                        }
//                    }
//                    updateImageCount()
//                })
        imageRepo.registerTreeBuilder()
                .mergeWith(
                        filterSizePref.listen()
                                .mergeWith(sortTypePref.listen())
                                .map { imageRepo.getImageTree() }
                                .filterNotNull()
                )
                .subscribe({
                    Timber.i("image tree updated")
                    updateImageTree(it, view)
                }, { error ->
                    Timber.e(error)
                }, {
                    Timber.d("images update completed")
                })
    }

    private fun updateImageTree(tree: ImageTree, view: View) {
        var filterCount = 0
        Observable.just(tree).map { it.getChildTree(treePath)?.getAllImages() }
                .flatMap { Observable.from(it) }
                .filter { image ->
                    val res = image.size >= filterSizePref.get() * 1024 //KB
                    if (!res) {
                        ++filterCount
                    }
                    return@filter res
                }
                .sorted { image, image2 ->
                    when (sortTypePref.get()) {
                        SORT_BY_PATH -> return@sorted -image.path.compareTo(image2.path)
                        SORT_BY_SIZE -> return@sorted -image.size.compareTo(image2.size)
                        SORT_BY_TIME -> return@sorted -image.lastModified.compareTo(image2.lastModified)
                    }
                    return@sorted 0
                }
                .subscribeOn(Schedulers.computation())
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(view)
                .subscribe {
                    items.clear()
                    if (filterCount == 0) {
                        items.add(Category(getString(R.string.label_category_tree_images, it.size)))
                    } else {
                        items.add(Category(getString(R.string.label_category_tree_images, it.size),
                                getString(R.string.label_category_action_filter, filterCount),
                                View.OnClickListener { activity.findViewById(R.id.action_filter)?.performClick() }))
                    }
                    items.addAll(it)
                    adapter.notifyDataSetChanged()
                }

    }

    private fun updateImageCount() {
        if (imageRepo.images.size > 0) {
            getSupportActionBar()?.subtitle = getString(R.string.subtitle_displayed_images, imageRepo.images.size, imageRepo.images.size)
        } else {
            getSupportActionBar()?.subtitle = getString(R.string.image_not_found)
        }
    }

}