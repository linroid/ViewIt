package com.linroid.viewit.ui.gallery.provider

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import butterknife.bindView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.linroid.viewit.R
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.repo.ScanRepo
import com.linroid.viewit.ui.gallery.GalleryActivity
import com.linroid.viewit.utils.RootUtils
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import me.drakeet.multitype.ItemViewProvider
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.io.File

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
class ImageViewProvider(val activity: GalleryActivity,
                        val scanRepo: ScanRepo,
                        val listener: ImageListener)
    : ItemViewProvider<Image, ImageViewProvider.ViewHolder>() {


    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_image, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, image: Image) {
        if (!RootUtils.isRootFile(image.source)) {
            Glide.with(holder.image.context).load(image.path).centerCrop().into(holder.image)
        } else {
            scanRepo.mountImage(image)
                    .observeOn(AndroidSchedulers.mainThread())
                    .bindToLifecycle(holder.itemView)
                    .subscribe({ image ->
                        Glide.with(holder.image.context)
                                .load(image.mountFile)
                                .centerCrop()
                                .listener(object : RequestListener<File, GlideDrawable> {
                                    override fun onException(e: Exception, model: File, target: Target<GlideDrawable>, isFirstResource: Boolean): Boolean {
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
        holder.itemView.setOnClickListener { listener.onViewImage(image) }
        holder.itemView.setOnLongClickListener {
            Toast.makeText(holder.itemView.context, image.path, Toast.LENGTH_SHORT).show()
            true
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView by bindView(R.id.image)
    }

    interface ImageListener {
        fun onViewImage(image: Image)
    }
}
