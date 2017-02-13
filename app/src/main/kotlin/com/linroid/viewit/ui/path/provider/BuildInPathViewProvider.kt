package com.linroid.viewit.ui.path.provider

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
 * @since 12/02/2017
 */
class BuildInPathViewProvider : ItemViewProvider<BuildInPath, BuildInPathViewProvider.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, path: BuildInPath) {
        holder.nameTV.text = path.name
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_build_in_path, parent, false))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTV: TextView by bindView(R.id.name)
    }
}