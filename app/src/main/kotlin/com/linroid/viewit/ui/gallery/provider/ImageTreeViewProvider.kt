package com.linroid.viewit.ui.gallery.provider

import android.content.pm.ApplicationInfo
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import butterknife.bindView
import com.linroid.viewit.R
import com.linroid.viewit.data.ImageRepo
import com.linroid.viewit.data.model.ImageTree
import com.linroid.viewit.ui.gallery.GalleryActivity
import com.linroid.viewit.utils.FormatUtils
import com.linroid.viewit.utils.PathUtils
import com.linroid.viewit.widget.ThumbnailView
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import me.drakeet.multitype.ItemViewProvider
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber

/**
 * @author linroid <linroid@gmail.com>
 * @since 30/01/2017
 */
class ImageTreeViewProvider(val activity: GalleryActivity, val visitPath: String, val appInfo: ApplicationInfo, val imageRepo: ImageRepo)
    : ItemViewProvider<ImageTree, ImageTreeViewProvider.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, tree: ImageTree) {
        holder.nameTV.text = FormatUtils.formatPath(PathUtils.relative(visitPath, tree.dir), appInfo)
        holder.itemView.setOnClickListener({
            activity.visitTree(tree)
        })
        Observable.from(tree.thumbnailImages())
                .flatMap { imageRepo.mountImage(it) }
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(holder.thumbnailView)
                .subscribe({
                    holder.thumbnailView.setImages(it)
                }, { error ->
                    Timber.e(error)
                })
        holder.itemView.setOnLongClickListener {
            Toast.makeText(holder.itemView.context, tree.dir, Toast.LENGTH_LONG).show()
            true
        }
        holder.imagesCountView.text = holder.itemView.context.getString(R.string.label_images_count, tree.allImagesCount())
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_image_tree, parent, false))
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTV: TextView by bindView(R.id.name)
        val thumbnailView: ThumbnailView by bindView(R.id.thumbnail)
        val imagesCountView: TextView by bindView(R.id.images_count)
    }
}