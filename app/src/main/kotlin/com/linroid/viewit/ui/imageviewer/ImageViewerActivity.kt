package com.linroid.viewit.ui.imageviewer

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Bundle
import butterknife.bindView
import com.github.piasy.biv.view.BigImageView
import com.linroid.viewit.App
import com.linroid.viewit.R
import com.linroid.viewit.data.ImageRepo
import com.linroid.viewit.data.model.AppInfo
import com.linroid.viewit.ui.BaseActivity
import rx.android.schedulers.AndroidSchedulers
import java.io.File
import javax.inject.Inject

/**
 * @author linroid <linroid@gmail.com>
 * @since 08/01/2017
 */
const val ARG_POSITION: String = "position";
const val ARG_APP_INFO: String = "app_info";

class ImageViewerActivity() : BaseActivity() {
    lateinit var imageFile: File
    val imageView: BigImageView by bindView(R.id.bigImage)
    @Inject lateinit var imageRepo: ImageRepo
    lateinit var info: ApplicationInfo
    var position: Int = 0

    companion object {
        fun navTo(source: BaseActivity, info: ApplicationInfo, position: Int) {
            val intent = Intent(source, ImageViewerActivity::class.java);
            intent.putExtra(ARG_APP_INFO, info)
            intent.putExtra(ARG_POSITION, position)
            source.startActivity(intent)
        }
    }

    override fun provideContentLayoutId(): Int = R.layout.activity_viewer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.get().graph().inject(this)
        var arguments: Bundle = intent.extras
        info = arguments.getParcelable(ARG_APP_INFO)
        position = arguments.getInt(ARG_POSITION)

        imageView.showImage(Uri.fromFile(imageFile))
        imageRepo.asObservable(info)
                .toList()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {

                }
    }
}