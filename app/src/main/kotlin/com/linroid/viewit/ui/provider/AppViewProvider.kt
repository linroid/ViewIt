package com.linroid.viewit.ui.provider

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import butterknife.bindView
import com.linroid.viewit.R
import com.linroid.viewit.data.model.AppHolder
import com.linroid.viewit.ui.BaseActivity
import com.linroid.viewit.ui.GalleryActivity
import me.drakeet.multitype.ItemViewProvider

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
internal class AppViewProvider(val activity: BaseActivity) : ItemViewProvider<AppHolder, AppViewProvider.ViewHolder>() {

    override fun onCreateViewHolder(
            inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_app, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, app: AppHolder) {
        holder.name.text = app.label
        holder.icon.setImageDrawable(app.icon)
        holder.root.setOnClickListener { view ->
            GalleryActivity.navTo(activity, app.info)
        }
        holder.root.setOnLongClickListener {
            Toast.makeText(holder.root.context, app.label, Toast.LENGTH_SHORT).show()
            true
        }
    }

    internal class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView by bindView(R.id.name)
        val icon: ImageView by bindView(R.id.icon)
        val root: View by bindView(R.id.root)
    }
}
