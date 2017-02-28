package com.linroid.viewit.ui.viewer

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import com.linroid.viewit.R
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.ui.BaseDialogFragment
import com.linroid.viewit.utils.ARG_IMAGE
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author linroid <linroid@gmail.com>
 * @since 28/02/2017
 */
class ImageDetailDialog : BaseDialogFragment() {
    companion object {
        fun show(image: Image, fm: FragmentManager) {
            val args = Bundle()
            args.putParcelable(ARG_IMAGE, image);
            val dialog = ImageDetailDialog()
            dialog.arguments = args
            dialog.show(fm, "image-detail-${image.path}")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val image: Image = arguments.getParcelable(ARG_IMAGE)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_image_detail, null)
        setupView(view, image)
        return AlertDialog.Builder(context)
                .setTitle(R.string.title_dialog_image_detail)
                .setView(view)
                .setNegativeButton(android.R.string.ok, null)
                .create()
    }

    private fun setupView(view: View, image: Image) {
        val recyclerView = view.findViewById(R.id.recyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        val items = Items()
        val adapter = MultiTypeAdapter(items)
        adapter.register(ImageProperty::class.java, ImagePropertyViewProvider())
        items.add(ImageProperty("路径", image.path))
        items.add(ImageProperty("类型", image.type.mime))
        items.add(ImageProperty("大小", Formatter.formatShortFileSize(context, image.size)))
//        items.add(ImageProperty("尺寸", image.))
        val dateFormatter = SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.getDefault())
        items.add(ImageProperty("时间", dateFormatter.format(Date(image.lastModified))))
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }
}