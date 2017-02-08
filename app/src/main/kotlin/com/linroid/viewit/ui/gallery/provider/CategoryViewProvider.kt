package com.linroid.viewit.ui.gallery.provider

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.bindView
import com.linroid.viewit.R
import me.drakeet.multitype.ItemViewProvider

/**
 * @author linroid <linroid@gmail.com>
 * @since 31/01/2017
 */
class CategoryViewProvider : ItemViewProvider<Category<*>, CategoryViewProvider.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, category: Category<*>) {
        holder.labelTV.text = category.label
        if (category.action.isNullOrEmpty()) {
            holder.actionBtn.visibility = View.GONE
            holder.actionBtn.setOnClickListener(null)
        } else {
            holder.actionBtn.visibility = View.VISIBLE
            holder.actionBtn.text = category.action
            holder.actionBtn.setOnClickListener(category.actionClickListener)
        }
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_category, parent, false))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val labelTV: TextView by bindView(R.id.label)
        val actionBtn: TextView by bindView(R.id.action)
    }
}