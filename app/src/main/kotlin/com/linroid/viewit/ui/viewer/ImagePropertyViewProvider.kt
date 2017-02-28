package com.linroid.viewit.ui.viewer

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.bindView
import com.linroid.viewit.R
import me.drakeet.multitype.ItemViewProvider

/**
 * @author linroid <linroid>@gmail.com>
 * @since 01/03/2017
 */
class ImagePropertyViewProvider : ItemViewProvider<ImageProperty, ImagePropertyViewProvider.ViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        val root = inflater.inflate(R.layout.item_image_property, parent, false)
        return ViewHolder(root)
    }

    override fun onBindViewHolder(holder: ViewHolder, imageProperty: ImageProperty) {
        holder.nameTV.text = imageProperty.name
        holder.valueTV.text = imageProperty.value
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTV: TextView by bindView(R.id.prop_name)
        val valueTV: TextView by bindView(R.id.prop_value)
    }
}