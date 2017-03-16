package com.linroid.viewit.ui.gallery.provider

import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import butterknife.bindView
import com.avos.avoscloud.AVAnalytics
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.linroid.viewit.R
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.repo.ImageRepo
import com.linroid.viewit.ui.gallery.GalleryActivity
import com.linroid.viewit.ui.gallery.ImageSelectProvider
import com.linroid.viewit.utils.EVENT_VIEW_IMAGE
import com.linroid.viewit.utils.RootUtils
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import me.drakeet.multitype.ItemViewProvider
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
class ImageViewProvider(val activity: GalleryActivity,
                        val imageRepo: ImageRepo,
                        val listener: ImageListener)
    : ItemViewProvider<Image, ImageViewProvider.ViewHolder>(), ImageSelectProvider {

    private var selectMode = false
    private val selectedItems = HashSet<Image>()

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_image, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, image: Image) {
        if (!RootUtils.isRootFile(image.source)) {
            Glide.with(holder.image.context).load(image.path).centerCrop().into(holder.image)
        } else {
            imageRepo.mountImage(image)
                    .observeOn(AndroidSchedulers.mainThread())
                    .bindToLifecycle(holder.itemView)
                    .subscribe({ image ->
                        Glide.with(holder.image.context)
                                .load(image.mountFile)
                                .centerCrop()
                                .listener(object : RequestListener<File, GlideDrawable> {
                                    override fun onException(e: Exception?, model: File, target: Target<GlideDrawable>, isFirstResource: Boolean): Boolean {
                                        Timber.e(e, "load image failed: ${model.absolutePath}")
                                        return false
                                    }

                                    override fun onResourceReady(resource: GlideDrawable, model: File, target: Target<GlideDrawable>, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                                        return false
                                    }
                                })
                                .into(holder.image)
                    }, { error ->
                        Timber.e(error)
                    })
        }
        holder.itemView.setOnClickListener {
            if (selectMode) {
                toggle(holder, image)
            } else {
                listener.onViewImage(image)
                AVAnalytics.onEvent(activity, EVENT_VIEW_IMAGE)
            }
        }
        holder.itemView.setOnLongClickListener {
            if (selectMode) {
                Toast.makeText(holder.itemView.context, image.path, Toast.LENGTH_SHORT).show()
            } else {
                startSelectMode()
                toggle(holder, image)
            }
            true
        }
        if (selectMode) {
            holder.checked = selectedItems.contains(image)
            holder.checkBox.visibility = View.VISIBLE
            holder.container.foreground = ColorDrawable(0x66FFFFFF);
        } else {
            holder.checkBox.visibility = View.GONE
            holder.container.foreground = null;
        }
    }

    fun toggle(holder: ViewHolder, image: Image) {
        val position = holder.adapterPosition
        if (selectedItems.contains(image)) {
            selectedItems.remove(image)
            holder.checked = false
        } else {
            selectedItems.add(image)
            holder.checked = true
        }
    }

    private fun startSelectMode() {
        selectMode = true
        selectedItems.clear()
        listener.onStartSelectMode()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: FrameLayout by bindView(R.id.container)
        val image: ImageView by bindView(R.id.image)
        val checkBox: CheckBox by bindView(R.id.checkbox)
        var checked: Boolean by Delegates.observable(false) { prop: KProperty<*>, oldVal: Boolean, newVal: Boolean ->
            checkBox.isChecked = newVal
        }

    }

    interface ImageListener {
        fun onViewImage(image: Image)
        fun onStartSelectMode()
    }

    override fun selectAll(items: List<Image>?) {
        items?.forEach {
            selectedItems.add(it)
        }
    }

    override fun unSelectAll() {
        selectedItems.clear()
    }

    override fun filter(filters: List<Image>?) {
        if (filters == null) {
            selectedItems.clear()
            return
        }
        val temp = HashSet<Image>()
        temp.addAll(selectedItems)
        temp.forEach {
            if (!filters.contains(it)) {
                selectedItems.remove(it)
            }
        }
        Timber.d("after apply filter: %d -> %d", temp.size, selectedItems.size)
    }

    override fun selectedItems(): Set<Image> {
        return selectedItems
    }

    override fun reset() {
        selectMode = false
        selectedItems.clear()
    }
}