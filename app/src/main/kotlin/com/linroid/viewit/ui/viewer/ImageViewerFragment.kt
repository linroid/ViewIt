package com.linroid.viewit.ui.viewer

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.bindView
import com.github.piasy.biv.view.BigImageView
import com.linroid.viewit.R
import com.linroid.viewit.ui.BaseFragment
import rx.android.schedulers.AndroidSchedulers
import java.io.File

/**
 * Created by Administrator on 2017/1/11.
 */
class ImageViewerFragment : BaseFragment() {
    val imageViewer: BigImageView by bindView(R.id.image_viewer)
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
        position = arguments?.getInt(ARG_POSITION) as Int
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater?.inflate(R.layout.fragment_image_viewer, container, false)
        return view!!;
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity
        if (activity is ImageViewerActivity) {
            activity.getObservable().elementAt(position)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        imageViewer.showImage(Uri.fromFile(File(it.path)))
                    }
        }
    }

}