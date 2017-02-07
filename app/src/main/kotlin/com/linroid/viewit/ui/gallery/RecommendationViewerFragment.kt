package com.linroid.viewit.ui.gallery

import android.content.pm.ApplicationInfo
import android.os.Bundle
import com.linroid.viewit.data.model.Recommendation
import com.linroid.viewit.utils.ARG_IMAGE_TREE_PATH
import com.linroid.viewit.utils.PathUtils

/**
 * @author linroid <linroid@gmail.com>
 * @since 05/02/2017
 */
class RecommendationViewerFragment : ImagesViewerFragment() {
    companion object {
        fun newInstance(recommendation: Recommendation, appInfo: ApplicationInfo): RecommendationViewerFragment {
            val args = Bundle()
            args.putString(ARG_IMAGE_TREE_PATH, PathUtils.formatToDevice(recommendation.pattern, appInfo))
            val fragment = RecommendationViewerFragment()
            fragment.arguments = args
            return fragment
        }
    }
}