package com.linroid.viewit.ui.gallery

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.linroid.viewit.R
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.model.ImageTree
import com.linroid.viewit.data.repo.ImageRepo
import com.linroid.viewit.data.repo.local.FavoriteRepo
import com.linroid.viewit.ui.BaseFragment
import com.linroid.viewit.ui.gallery.provider.ImageCategory
import com.linroid.viewit.ui.gallery.provider.ImageCategoryViewProvider
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
abstract class GalleryChildFragment : BaseFragment(), ImageViewProvider.ImageListener {

    @Inject lateinit protected var imageRepo: ImageRepo
    @Inject lateinit protected var favoriteRepo: FavoriteRepo
    @Inject lateinit protected var appInfo: ApplicationInfo
    @Inject lateinit protected var activity: GalleryActivity
    @field:[Inject Named(PREF_SORT_TYPE)]
    lateinit var sortTypePref: LongPreference
    @field:[Inject Named(PREF_FILTER_SIZE)]
    lateinit var filterSizePref: LongPreference

    private lateinit var imageViewProvider: ImageViewProvider
    protected val items = ArrayList<Any>()
    protected var adapter = MultiTypeAdapter(items)
    private var subscription: Subscription? = null

    // 应该在子类中初始化
    protected lateinit var imageCategory: ImageCategory

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageViewProvider = ImageViewProvider(activity, imageRepo, this)
        adapter.register(Image::class.java, imageViewProvider)
        adapter.register(ImageCategory::class.java, ImageCategoryViewProvider(sortTypePref, filterSizePref))
    }

    override fun onViewImage(image: Image) {
        ImageViewerActivity.navTo(activity, imageRepo,
                imageCategory.items!!,
                imageCategory.items!!.indexOf(image))
    }

    override fun onStartSelectMode() {
        ImageMultiOptionsController(activity, imageViewProvider, appInfo, imageRepo, imageCategory).attach()
    }

    protected fun updateImageTree(tree: ImageTree?) {
        Timber.i("updateImageTree: ${tree?.toString()}")
        var filterCount = 0
        subscription.unsubscribeIfNotNull()
        subscription = Observable.just(tree)
                .mergeWith(
                        // 监听过滤和排序的变化
                        filterSizePref.listen()
                                .mergeWith(sortTypePref.listen())
                                .map {
                                    filterCount = 0
                                    return@map tree
                                }
                                .filterNotNull()
                )
                .observeOn(Schedulers.computation())
                .map { tree?.allImages() ?: emptyList() }
                .flatMap {
                    filterCount = 0
                    Observable.from(it)
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
                }
                .observeOn(AndroidSchedulers.mainThread())
                .bindUntilEvent(this, FragmentEvent.DESTROY_VIEW)
                .subscribe { images ->
                    if (filterCount == 0) {
                        imageCategory.apply {
                            label = getString(R.string.label_category_tree_images, images.size)
                        }
                    } else {
                        imageCategory.apply {
                            label = getString(R.string.label_category_tree_images_with_filter, images.size, images.size + filterCount)
                        }
                    }
                    imageCategory.totalCount = images.size + filterCount
                    imageCategory.items = images
                }

    }
}