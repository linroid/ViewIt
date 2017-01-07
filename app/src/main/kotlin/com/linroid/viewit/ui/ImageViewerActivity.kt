package com.linroid.viewit.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import butterknife.bindView
import com.github.piasy.biv.view.BigImageView
import com.linroid.viewit.R
import java.io.File

/**
 * @author linroid <linroid@gmail.com>
 * @since 08/01/2017
 */
const val ARG_IMAGE_FILE: String = "image_file";

class ImageViewerActivity() : BaseActivity() {
    lateinit var imageFile: File
    val imageView: BigImageView by bindView(R.id.bigImage)

    companion object {
        fun navTo(source: BaseActivity, file: File) {
            val intent = Intent(source, ImageViewerActivity::class.java);
            intent.putExtra(ARG_IMAGE_FILE, file.absolutePath)
            source.startActivity(intent)
        }
    }

    override fun provideContentLayoutId(): Int = R.layout.activity_viewer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageFile = File(intent.getStringExtra(ARG_IMAGE_FILE))
        imageView.showImage(Uri.fromFile(imageFile))
    }
}