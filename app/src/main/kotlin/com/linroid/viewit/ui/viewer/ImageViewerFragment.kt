package com.linroid.viewit.ui.viewer

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import butterknife.bindView
import com.bumptech.glide.Glide
import com.github.piasy.biv.view.BigImageView
import com.linroid.viewit.R
import com.linroid.viewit.data.ScanRepo
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.model.ImageType
import com.linroid.viewit.ui.BaseFragment
import com.linroid.viewit.ui.ImmersiveActivity
import com.linroid.viewit.utils.ARG_POSITION
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject

/**
 * @author linroid <linroid@gmail.com>
 * @since 11/01/2017
 */
class ImageViewerFragment : BaseFragment() {
    @Inject lateinit var scanRepo: ScanRepo
    @Inject lateinit var appInfo: ApplicationInfo

    val bigImageViewer: BigImageView by bindView(R.id.big_image_viewer)
    val gifImageViewer: ImageView by bindView(R.id.gif_image_viewer)
    var position: Int = 0

    companion object {
        fun newInstance(position: Int): ImageViewerFragment {
            val fragment = ImageViewerFragment()
            val args = Bundle()
            args.putInt(ARG_POSITION, position)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = savedInstanceState ?: arguments
        position = args.getInt(ARG_POSITION)
        Timber.d("onCreate")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ARG_POSITION, position)
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if (activity is ImageViewerActivity) {
            activity.graph.inject(this)
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
        if (activity is ImageViewerActivity) {
            loadImage(activity as ImageViewerActivity)
        }
    }

    private fun loadImage(act: ImageViewerActivity) {
        Timber.i("position:$position")
        var isGif = false;
        val image = scanRepo.images[position]
        Observable.just(image)
                .flatMap { image ->
                    isGif = image.type == ImageType.GIF
                    if (image.file() == null) {
                        return@flatMap scanRepo.mountImage(image);
                    }
                    return@flatMap Observable.just(image)
                }
                .map(Image::file)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ file ->
                    if (isGif) {
                        Glide.with(act).load(file).asGif().into(gifImageViewer)
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

    fun updatePosition(newPos: Int) {
        position = newPos;
        arguments.putInt(ARG_POSITION, newPos)

        Timber.d("updatePosition")
    }

}