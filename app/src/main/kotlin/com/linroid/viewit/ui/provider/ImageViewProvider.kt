package com.linroid.viewit.ui.provider

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import butterknife.bindView
import com.bumptech.glide.Glide
import com.linroid.viewit.R
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.ui.BaseActivity
import com.linroid.viewit.ui.ImageViewerActivity
import me.drakeet.multitype.ItemViewProvider

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
class ImageViewProvider(val activity: BaseActivity) : ItemViewProvider<Image, ImageViewProvider.ViewHolder>() {
    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_image, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, image: Image) {
        Glide.with(holder.image.context).load(image.path).centerCrop().into(holder.image)
        holder.itemView.setOnClickListener {
            ImageViewerActivity.navTo(activity, image.path)
        }
        holder.itemView.setOnLongClickListener {
            Toast.makeText(holder.itemView.context, image.path.absolutePath, Toast.LENGTH_SHORT).show()
            true
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView by bindView(R.id.image)
    }
}
