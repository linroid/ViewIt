package com.linroid.viewit.ui.path.provider

import android.content.pm.ApplicationInfo
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.bindView
import com.avos.avoscloud.AVAnalytics

import com.linroid.viewit.R
import com.linroid.viewit.data.model.ScanPath
import com.linroid.viewit.utils.EVENT_CLICK_PATH_DELETE
import com.linroid.viewit.utils.EVENT_CLICK_PATH_EDIT
import com.linroid.viewit.utils.PathUtils

import me.drakeet.multitype.ItemViewProvider

/**
 * @author linroid <linroid@gmail.com>
 * @since 12/02/2017
 */
class ScanPathViewProvider(val appInfo: ApplicationInfo, val listener: OnDeleteScanPathListener) : ItemViewProvider<ScanPath, ScanPathViewProvider.ViewHolder>() {
    var deleteMode = false

    override fun onCreateViewHolder(
            inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        val root = inflater.inflate(R.layout.item_scan_path, parent, false)
        return ViewHolder(root)
    }

    override fun onBindViewHolder(holder: ViewHolder, scanPath: ScanPath) {
        holder.nameTV.text = PathUtils.formatToDevice(scanPath.path, appInfo)
        holder.deleteBtn.visibility = if (deleteMode) View.VISIBLE else View.GONE
        holder.deleteBtn.setOnClickListener {
            AVAnalytics.onEvent(it.context, EVENT_CLICK_PATH_DELETE)
            listener.onDeleteScanPath(scanPath)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTV: TextView by bindView(R.id.name)
        val deleteBtn: TextView by bindView(R.id.btn_delete)
    }

    interface OnDeleteScanPathListener {
        fun onDeleteScanPath(scanPath: ScanPath)
    }
}