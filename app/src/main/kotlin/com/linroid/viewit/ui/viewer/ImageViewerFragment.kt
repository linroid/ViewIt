package com.linroid.viewit.ui.viewer

import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import butterknife.bindView
import com.bumptech.glide.Glide
import com.github.piasy.biv.view.BigImageView
import com.linroid.viewit.R
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.model.ImageType
import com.linroid.viewit.data.repo.ImageRepo
import com.linroid.viewit.ui.BaseFragment
import com.linroid.viewit.ui.ImmersiveActivity
import com.linroid.viewit.utils.ARG_IMAGE
import com.linroid.viewit.utils.onMain
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import rx.Observable
import timber.log.Timber
import javax.inject.Inject

/**
 * @author linroid <linroid@gmail.com>
 * @since 11/01/2017
 */
class ImageViewerFragment : BaseFragment() {
    @Inject lateinit var imageRepo: ImageRepo
    @Inject lateinit var appInfo: ApplicationInfo

    val bigImageViewer: BigImageView by bindView(R.id.big_image_viewer)
    val gifImageViewer: ImageView by bindView(R.id.gif_image_viewer)
    lateinit var image: Image

    companion object {
        fun newInstance(): ImageViewerFragment {
            val fragment = ImageViewerFragment()
            fragment.arguments = Bundle()
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        image = args.getParcelable(ARG_IMAGE)
        Timber.d("onCreate")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ImageViewerActivity) {
            context.graph.inject(this)
        }
        Timber.d("onAttach")
    }

    override fun provideLayoutId(): Int = R.layout.fragment_image_viewer

    val imageClickListener = View.OnClickListener {
        if (activity is ImmersiveActivity) {
            (activity as ImmersiveActivity).toggle()
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bigImageViewer.setOnClickListener(imageClickListener)
        gifImageViewer.setOnClickListener(imageClickListener)
        loadImage()
    }

    private fun loadImage() {
        Timber.i("loadImage: ${image.path} at @$tag")
        var isGif = false
        Observable.just(image)
                .flatMap { image ->
                    isGif = image.type == ImageType.GIF
                    if (image.file() == null) {
                        return@flatMap imageRepo.mountImage(image);
                    }
                    return@flatMap Observable.just(image)
                }
                .map(Image::file)
                .onMain()
                .bindToLifecycle(this)
                .subscribe({ file ->
                    if (isGif) {
                        Glide.with(activity).load(file).asGif().into(gifImageViewer)
                        gifImageViewer.visibility = View.VISIBLE
                        bigImageViewer.visibility = View.GONE
                    } else {
                        bigImageViewer.showImage(Uri.fromFile(file))
                        gifImageViewer.visibility = View.GONE
                        bigImageViewer.visibility = View.VISIBLE
                    }
                }, { error ->
                    Timber.e(error, "view image failed")
                })
    }

    fun updateImage(arg: Image) {
        image = arg
        arguments.putParcelable(ARG_IMAGE, image)
        if (view != null) {
            loadImage()
        }
        Timber.d("updateImage: ${arg.path}")
    }

}