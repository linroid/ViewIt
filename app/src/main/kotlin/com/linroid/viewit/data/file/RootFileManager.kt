package com.linroid.viewit.data.file

import android.content.Context
import com.linroid.viewit.data.model.Image
import com.stericson.RootTools.RootTools
import java.io.File

/**
 * Created by Administrator on 2017/1/11.
 */
class RootFileManager(val context: Context) {
    val cacheDir: File = File(context.cacheDir, "mounts")

    fun mountImage(image: Image): File {
        var mountedFile = File(cacheDir, image.packageName + "/" + image.path);
        if (mountedFile.exists()) {
            return mountedFile
        }
        RootTools.copyFile(image.source.absolutePath, mountedFile.absolutePath, true, false)
        return mountedFile
    }
}