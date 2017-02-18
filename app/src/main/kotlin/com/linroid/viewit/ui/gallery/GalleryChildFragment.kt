package com.linroid.viewit.ui.gallery

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.linroid.viewit.R
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.model.ImageTree
import com.linroid.viewit.data.repo.ImageRepo
import com.linroid.viewit.data.repo.ImageRepo.Companion.SORT_BY_PATH
import com.linroid.viewit.data.repo.ImageRepo.Companion.SORT_BY_SIZE
import com.linroid.viewit.data.repo.ImageRepo.Companion.SORT_BY_TIME
import com.linroid.viewit.data.repo.local.FavoriteRepo
import com.linroid.viewit.ui.BaseFragment
import com.linroid.viewit.ui.gallery.provider.Category
import com.linroid.viewit.ui.gallery.provider.ImageViewProvider
import com.linroid.viewit.ui.viewer.ImageViewerActivity
import com.linroid.viewit.utils.PREF_FILTER_SIZE
import com.linroid.viewit.utils.PREF_SORT_TYPE
import com.linroid.viewit.utils.pref.LongPreference
import com.linroid.viewit.utils.unsubscribeIfNotNull
import com.trello.rxlifecycle.android.FragmentEvent
import com.trello.rxlifecycle.kotlin.bindUntilEvent
import me.drakeet.multitype.MultiTypeAdapter
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.filterNotNull
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Named

/**
 * @author linroid <linroid@gmail.com>
 * @since 01/02/2017
 */
abstract class GalleryChildFragment : BaseFragment() {

    @Inject lateinit protected var imageRepo: ImageRepo
    @Inject lateinit protected var favoriteRepo: FavoriteRepo
    @Inject lateinit protected var appInfo: ApplicationInfo
    @Inject lateinit protected var activity: GalleryActivity
    @field:[Inject Named(PREF_SORT_TYPE)]
    lateinit var sortTypePref: LongPreference
    @field:[Inject Named(PREF_FILTER_SIZE)]
    lateinit var filterSizePref: LongPreference

    protected val items = ArrayList<Any>()
    protected var adapter = MultiTypeAdapter(items)
    private var subscription: Subscription? = null

    // 应该在子类中初始化
    protected lateinit var imageCategory: Category<Image>

    override fun onPause() {
        super.onPause()
        Timber.d("onPause")
        setHasOptionsMenu(false)
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume")
        setHasOptionsMenu(true)
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
        adapter.register(Image::class.java, ImageViewProvider(activity, imageRepo, object : ImageViewProvider.ImageListener {
            override fun onViewImage(image: Image) {
                ImageViewerActivity.navTo(activity, imageRepo,
                        imageCategory.items!!,
                        imageCategory.items!!.indexOf(image))
            }

        }))
//        registerComponents();
    }

//    private fun registerComponents() {
//        imageRepo.registerTreeBuilder()
//
//                .subscribe({
//                    Timber.i("image tree updated")
//                    updateImageTree(it)
//                }, { error ->
//                    Timber.e(error)
//                }, {
//                    Timber.d("images update completed")
//                })
//    }

    protected fun updateImageTree(tree: ImageTree) {
        Timber.i("updateImageTree: ${tree.toString()}")
        var filterCount = 0
        subscription.unsubscribeIfNotNull()
        subscription = Observable.just(tree)
//                .mergeWith(
//                        filterSizePref.listen()
//                                .mergeWith(sortTypePref.listen())
//                                .map {
//                                    filterCount = 0
//                                    return@map tree
//                                }
//                                .filterNotNull()
//                )
                .observeOn(Schedulers.computation())
                .map { tree.allImages() }
                .flatMap { Observable.from(it) }
                .bindUntilEvent(this, FragmentEvent.DESTROY_VIEW)
                .filter { image ->
                    val res = image.size >= filterSizePref.get() * 1024 //KB
                    if (!res) {
                        ++filterCount
                    }
                    return@filter res
                }
                .sorted { image, image2 ->
                    when (sortTypePref.get()) {
                        ImageRepo.SORT_BY_PATH -> return@sorted -image.path.compareTo(image2.path)
                        ImageRepo.SORT_BY_SIZE -> return@sorted -image.size.compareTo(image2.size)
                        ImageRepo.SORT_BY_TIME -> return@sorted -image.lastModified.compareTo(image2.lastModified)
                    }
                    return@sorted 0
                }
                .subscribeOn(Schedulers.computation())
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .bindUntilEvent(this, FragmentEvent.DESTROY_VIEW)
                .subscribe { images ->
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
                }

    }
}