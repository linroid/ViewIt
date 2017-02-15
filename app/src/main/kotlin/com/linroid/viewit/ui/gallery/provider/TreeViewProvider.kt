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
import com.linroid.viewit.data.model.ImageTree
import com.linroid.viewit.data.repo.ImageRepo
import com.linroid.viewit.ui.gallery.GalleryActivity
import com.linroid.viewit.utils.unsubscribeIfNotNull
import com.linroid.viewit.widget.ThumbnailView
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import me.drakeet.multitype.ItemViewProvider
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * @author linroid <linroid@gmail.com>
 * @since 30/01/2017
 */
abstract class TreeViewProvider<T>(val activity: GalleryActivity, val appInfo: ApplicationInfo, val imageRepo: ImageRepo)
    : ItemViewProvider<T, TreeViewProvider.TreeViewHolder>() {


    override fun onBindViewHolder(holder: TreeViewHolder, data: T) {
        holder.nameTV.text = obtainName(data)
        val tree = obtainTree(data)
        holder.itemView.setOnClickListener({
            if (tree != null) {
                activity.visitTree(tree)
            }
        })
        holder.loadImages(imageRepo, tree)
        holder.itemView.setOnClickListener {
            onClick(it, data)
        }
        holder.itemView.setOnLongClickListener {
            if (tree != null) {
                Toast.makeText(holder.itemView.context, tree.dir, Toast.LENGTH_LONG).show()
            }
            return@setOnLongClickListener true
        }
        val count = tree?.allImagesCount() ?: 0
        holder.imagesCountView.text = holder.itemView.context.getString(R.string.label_images_count, count)
    }

    protected abstract fun onClick(it: View, data: T)

    protected abstract fun obtainTree(data: T): ImageTree?

    protected abstract fun obtainName(data: T): CharSequence

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): TreeViewHolder {
        return TreeViewHolder(inflater.inflate(R.layout.item_image_tree, parent, false))
    }

    class TreeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTV: TextView by bindView(R.id.name)
        val thumbnailView: ThumbnailView by bindView(R.id.thumbnail)
        val imagesCountView: TextView by bindView(R.id.images_count)

        var subscription: Subscription? = null

        fun loadImages(imageRepo: ImageRepo, tree: ImageTree?) {
            subscription.unsubscribeIfNotNull()
            thumbnailView.clear()
            if (tree == null) {
                return
            }
            subscription = Observable.from(tree.thumbnailImages)
                    .delaySubscription(1, TimeUnit.SECONDS)
                    .flatMap {
                        if (it.file() == null) {
                            return@flatMap imageRepo.mountImage(it)
                        } else {
                            return@flatMap Observable.just(it)
                        }
                    }
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .bindToLifecycle(thumbnailView)
                    .subscribe({
                        thumbnailView.setImages(it)
                    }, { error ->
                        Timber.e(error)
                    })
        }
    }
}