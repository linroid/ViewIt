package com.linroid.viewit.ui.home

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import butterknife.bindView
import com.linroid.viewit.R
import com.linroid.viewit.ui.BaseActivity
import com.linroid.viewit.ui.gallery.GalleryActivity
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import me.drakeet.multitype.ItemViewProvider
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
internal class AppViewProvider(val activity: BaseActivity) : ItemViewProvider<ApplicationInfo, AppViewProvider.ViewHolder>() {

    val packageManager: PackageManager = activity.packageManager

    override fun onCreateViewHolder(
            inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_app, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, info: ApplicationInfo) {
        val label = packageManager.getApplicationLabel(info)
        holder.name.text = label
        holder.root.setOnClickListener { view ->
            GalleryActivity.navTo(activity, info)
        }
        holder.root.setOnLongClickListener {
            Toast.makeText(holder.root.context, label, Toast.LENGTH_SHORT).show()
            true
        }
        Observable.just(info)
                .map { packageManager.getApplicationIcon(info) }
                .subscribeOn(Schedulers.io())
                .bindToLifecycle(holder.icon)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { holder.icon.setImageDrawable(it) }
    }

    internal class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView by bindView(R.id.name)
        val icon: ImageView by bindView(R.id.icon)
        val root: View by bindView(R.id.root)
    }
}
