package com.linroid.viewit.ui.home

import android.content.DialogInterface
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import butterknife.bindView
import com.avos.avoscloud.AVAnalytics
import com.linroid.viewit.R
import com.linroid.viewit.data.repo.local.AppUsageRepo
import com.linroid.viewit.ui.BaseActivity
import com.linroid.viewit.ui.gallery.GalleryActivity
import com.linroid.viewit.utils.EVENT_CLICK_APP
import com.linroid.viewit.utils.RootUtils
import com.linroid.viewit.utils.RxOnce
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import me.drakeet.multitype.ItemViewProvider
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
internal class AppViewProvider(val activity: BaseActivity, val usageRepo: AppUsageRepo) : ItemViewProvider<ApplicationInfo, AppViewProvider.ViewHolder>() {

    val packageManager: PackageManager = activity.packageManager

    override fun onCreateViewHolder(
            inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_app, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, info: ApplicationInfo) {
        val label = packageManager.getApplicationLabel(info)
        holder.name.text = label
        holder.root.setOnClickListener { view ->
            AVAnalytics.onEvent(view.context, EVENT_CLICK_APP, info.packageName)
            usageRepo.visitApp(info).subscribe {
                Timber.i("visitApp: $it")
            }
            RxOnce.app("require_root_prompt").subscribe({
                if (it) {
                    showRequireRootPromptDialog(info)
                } else {
                    GalleryActivity.navTo(activity, info)
                }
            })
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

    private fun showRequireRootPromptDialog(info: ApplicationInfo) {
        AlertDialog.Builder(activity)
                .setTitle(R.string.title_dialog_require_root_prompt)
                .setMessage(if (RootUtils.isRootAvailable()) R.string.msg_dialog_require_root else R.string.msg_dialog_no_root)
                .setPositiveButton(android.R.string.ok, { dialogInterface: DialogInterface, i: Int ->
                    GalleryActivity.navTo(activity, info)
                })
                .show()
    }

    internal class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView by bindView(R.id.name)
        val icon: ImageView by bindView(R.id.icon)
        val root: View by bindView(R.id.root)
    }
}
