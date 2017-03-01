package com.linroid.viewit.ui.gallery.provider

import android.support.annotation.MenuRes
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import butterknife.bindView
import com.linroid.viewit.R
import com.linroid.viewit.data.repo.ImageRepo
import com.linroid.viewit.utils.pref.LongPreference
import me.drakeet.multitype.ItemViewProvider

/**
 * @author linroid <linroid@gmail.com>
 * @since 28/02/2017
 */
class ImageCategoryViewProvider(val sortTypePref: LongPreference, val filterSizePref: LongPreference)
    : ItemViewProvider<ImageCategory, ImageCategoryViewProvider.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, category: ImageCategory) {
        holder.labelTV.text = category.label
        holder.actionSortBtn.setOnClickListener { showActionPopupMenu(it, R.menu.image_sort) }
        holder.actionFilterBtn.setOnClickListener { showActionPopupMenu(it, R.menu.image_filter) }
        if (category.totalCount > 0) {
            holder.actionSortBtn.visibility     = View.VISIBLE
            holder.actionFilterBtn.visibility   = View.VISIBLE
        } else {
            holder.actionSortBtn.visibility     = View.GONE
            holder.actionFilterBtn.visibility   = View.GONE
        }
    }

    private fun showActionPopupMenu(view: View, @MenuRes menuId: Int) {
        val popupMenu = PopupMenu(view.context, view, Gravity.BOTTOM)
        popupMenu.inflate(menuId)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.groupId) {
                R.id.action_sort -> {
                    handleSortAction(item)
                }
                R.id.action_filter -> {
                    handleFilterAction(item)
                }
            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()

        val menu = popupMenu.menu;
        when (sortTypePref.get()) {
            ImageRepo.SORT_BY_PATH -> {
                menu.findItem(R.id.action_sort_by_default)?.isChecked = true
            }
            ImageRepo.SORT_BY_SIZE -> {
                menu.findItem(R.id.action_sort_by_size)?.isChecked = true
            }
            ImageRepo.SORT_BY_TIME -> {
                menu.findItem(R.id.action_sort_by_time)?.isChecked = true
            }
        }
        when (filterSizePref.get()) {
            0L -> {
                menu.findItem(R.id.action_filter_none)?.isChecked = true
            }
            30L -> {
                menu.findItem(R.id.action_filter_30K)?.isChecked = true
            }
            100L -> {
                menu.findItem(R.id.action_filter_100K)?.isChecked = true
            }
            300L -> {
                menu.findItem(R.id.action_filter_300K)?.isChecked = true
            }
        }

    }

    private fun handleFilterAction(item: MenuItem) {
        var size: Long = 0
        when (item.itemId) {
            R.id.action_filter_none -> size = 0
            R.id.action_filter_30K -> size = 30
            R.id.action_filter_100K -> size = 100
            R.id.action_filter_300K -> size = 300
        }
        if (size == filterSizePref.get()) {
            return
        }
        filterSizePref.set(size)
    }

    private fun handleSortAction(item: MenuItem) {
        var type = ImageRepo.SORT_BY_PATH
        when (item.itemId) {
            R.id.action_sort_by_default -> {
                type = ImageRepo.SORT_BY_PATH
            }
            R.id.action_sort_by_size -> {
                type = ImageRepo.SORT_BY_SIZE
            }
            R.id.action_sort_by_time -> {
                type = ImageRepo.SORT_BY_TIME
            }
        }
        if (type == sortTypePref.get()) {
            return
        }
        sortTypePref.set(type)
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_image_category, parent, false))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val labelTV: TextView by bindView(R.id.label)
        val actionSortBtn: ImageButton by bindView(R.id.action_sort)
        val actionFilterBtn: ImageButton by bindView(R.id.action_filter)
    }
}