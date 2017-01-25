package com.linroid.viewit.ui.viewer

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import butterknife.bindView
import com.bumptech.glide.Glide
import com.github.piasy.biv.view.BigImageView
import com.linroid.viewit.R
import com.linroid.viewit.data.ImageRepo
import com.linroid.viewit.data.model.Image
import com.linroid.viewit.data.model.ImageType
import com.linroid.viewit.ui.BaseFragment
import com.linroid.viewit.ui.ImmersiveActivity
import com.linroid.viewit.utils.ARG_POSITION
import com.linroid.viewit.utils.RootUtils
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * @author linroid <linroid@gmail.com>
 * @since 11/01/2017
 */
class ImageViewerFragment : BaseFragment() {
    @Inject lateinit var observable: Observable<Image>
    @Inject lateinit var imageRepo: ImageRepo
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

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if (activity is ImageViewerActivity) {
            activity.graph.inject(this)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = arguments?.getInt(ARG_POSITION) as Int
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater?.inflate(R.layout.fragment_image_viewer, container, false)
        return view!!;
    }

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
        var isGif = false;
        observable.elementAt(position)
                .map {
                    isGif = it.type == ImageType.GIF
                    return@map File(it.path)
                }
                .flatMap {
                    if (RootUtils.isRootFile(act, it.path)) {
                        return@flatMap imageRepo.mountFile(it.path, appInfo);
                    }
                    return@flatMap Observable.just(it)
                }
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

}