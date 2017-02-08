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
import butterknife.bindView
import com.linroid.viewit.R
import com.linroid.viewit.data.ScanRepo.Companion.SORT_BY_PATH
import com.linroid.viewit.data.ScanRepo.Companion.SORT_BY_SIZE
import com.linroid.viewit.data.ScanRepo.Companion.SORT_BY_TIME
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.model.ImageTree
import com.linroid.viewit.ui.gallery.provider.Category
import com.linroid.viewit.ui.gallery.provider.CategoryViewProvider
import com.linroid.viewit.ui.gallery.provider.ImageViewProvider
import com.linroid.viewit.ui.viewer.ImageViewerActivity
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
open class ImagesViewerFragment : GalleryViewerFragment() {
    private val SPAN_COUNT = 4

    @field:[Inject Named(PREF_SORT_TYPE)]
    lateinit var sortTypePref: LongPreference
    @field:[Inject Named(PREF_FILTER_SIZE)]
    lateinit var filterSizePref: LongPreference

    private val items = ArrayList<Any>()
    private var adapter = MultiTypeAdapter(items)
    private lateinit var imageCategory: Category<Image>

    private val recyclerView: RecyclerView by bindView(R.id.recyclerView)

    companion object {
        fun newInstance(tree: ImageTree): ImagesViewerFragment {
            val args = Bundle()
            args.putString(ARG_IMAGE_TREE_PATH, tree.dir)
            val fragment = ImagesViewerFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun provideLayoutId(): Int = R.layout.fragment_all_images

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GalleryActivity) {
            context.graph().inject(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.gallery_images_viewer, menu)
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
        adapter.register(Image::class.java, ImageViewProvider(activity, scanRepo, object : ImageViewProvider.ImageListener {
            override fun onViewImage(image: Image) {
                ImageViewerActivity.navTo(activity, appInfo,
                        imageCategory.items!!, imageCategory.items!!.indexOf(image))
            }
        }))
        adapter.register(Category::class.java, CategoryViewProvider())
        imageCategory = Category<Image>(null, adapter, items, getString(R.string.label_category_tree_images, 0))

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

        registerComponents(view)
    }

    private fun registerComponents(view: View) {
//        scanRepo.registerImageEvent()
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
        scanRepo.registerTreeBuilder()
                .mergeWith(
                        filterSizePref.listen()
                                .mergeWith(sortTypePref.listen())
                                .map { scanRepo.getImageTree() }
                                .filterNotNull()
                )
                .subscribe({
                    Timber.i("image tree updated")
                    updateImageTree(it)
                }, { error ->
                    Timber.e(error)
                }, {
                    Timber.d("images update completed")
                })
    }

    private fun updateImageTree(tree: ImageTree) {
        var filterCount = 0
        Observable.just(tree).map { it.find(path)?.allImages() }
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
                .bindToLifecycle(this)
                .subscribe { images ->
//                    items.clear()
                    if (filterCount == 0) {
                        imageCategory.apply {
                            label = getString(R.string.label_category_tree_images, images.size)
                            action = null
                            actionClickListener = null
                        }
                    } else {
                        imageCategory.apply {
                            label = getString(R.string.label_category_tree_images, images.size)
                            action = getString(R.string.label_category_action_filter, filterCount)
                            actionClickListener = View.OnClickListener {
                                activity.findViewById(R.id.action_filter)?.performClick()
                            }
                        }
                    }
                    imageCategory.items = images
                    adapter.notifyDataSetChanged()
                }

    }

    private fun updateImageCount() {
        if (scanRepo.images.size > 0) {
            getSupportActionBar()?.subtitle = getString(R.string.subtitle_displayed_images, scanRepo.images.size, scanRepo.images.size)
        } else {
            getSupportActionBar()?.subtitle = getString(R.string.image_not_found)
        }
    }

}