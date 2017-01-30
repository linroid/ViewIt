package com.linroid.viewit.ui.gallery

import android.content.pm.ApplicationInfo
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.bindView
import com.linroid.viewit.R
import com.linroid.viewit.data.model.ImageTree
import com.linroid.viewit.utils.FormatUtils
import com.linroid.viewit.utils.PathUtils
import me.drakeet.multitype.ItemViewProvider

/**
 * @author linroid <linroid@gmail.com>
 * @since 30/01/2017
 */
class ImageTreeViewProvider(val activity: GalleryActivity, val visitPath: String, val appInfo:ApplicationInfo)
    : ItemViewProvider<ImageTree, ImageTreeViewProvider.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, tree: ImageTree) {
        holder.nameTV.text = FormatUtils.formatPath(PathUtils.relative(visitPath, tree.dir), appInfo)
        holder.itemView.setOnClickListener({
            activity.visitTree(tree)
        })
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_image_tree, parent, false))
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTV: TextView by bindView(R.id.name)
    }
}