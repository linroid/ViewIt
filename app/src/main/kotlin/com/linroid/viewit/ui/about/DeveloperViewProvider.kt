package com.linroid.viewit.ui.about

import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.avos.avoscloud.AVAnalytics
import com.linroid.viewit.utils.EVENT_CLICK_ABOUT_DEVELOPER
import me.drakeet.multitype.ItemViewProvider
import me.drakeet.support.about.R

/**
 * @author linroid <linroid@gmail.com>
 * @since 30/01/2017
 */
class DeveloperViewProvider : ItemViewProvider<Developer, DeveloperViewProvider.ViewHolder>() {

    override fun onCreateViewHolder(
            inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        val root = inflater.inflate(R.layout.about_page_item_contributor, parent, false)
        return ViewHolder(root)
    }


    override fun onBindViewHolder(
            holder: ViewHolder, developer: Developer) {
        holder.avatar.setImageResource(developer.avatarResId)
        holder.name.text = developer.name
        holder.desc.text = developer.desc
        holder.itemView.setOnClickListener {
            AVAnalytics.onEvent(it.context, EVENT_CLICK_ABOUT_DEVELOPER)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(developer.page)
            it.context.startActivity(intent)
        }
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var avatar: ImageView = itemView.findViewById(R.id.avatar) as ImageView
        var name: TextView = itemView.findViewById(R.id.name) as TextView
        var desc: TextView = itemView.findViewById(R.id.desc) as TextView
        lateinit var url: String
    }
}