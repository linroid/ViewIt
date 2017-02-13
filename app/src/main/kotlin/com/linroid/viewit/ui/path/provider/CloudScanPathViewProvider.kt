package com.linroid.viewit.ui.path.provider

import android.content.pm.ApplicationInfo
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.bindView

import com.linroid.viewit.R
import com.linroid.viewit.data.model.CloudScanPath
import com.linroid.viewit.utils.PathUtils

import me.drakeet.multitype.ItemViewProvider

/**
 * @author linroid <linroid@gmail.com>
 * @since 12/02/2017
 */
class CloudScanPathViewProvider(val appInfo: ApplicationInfo) : ItemViewProvider<CloudScanPath, CloudScanPathViewProvider.ViewHolder>() {

    override fun onCreateViewHolder(
            inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        val root = inflater.inflate(R.layout.item_cloud_scan_path, parent, false)
        return ViewHolder(root)
    }

    override fun onBindViewHolder(holder: ViewHolder, cloudScanPath: CloudScanPath) {
        holder.nameTV.text = PathUtils.formatToDevice(cloudScanPath.path, appInfo)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTV: TextView by bindView(R.id.name)
    }
}