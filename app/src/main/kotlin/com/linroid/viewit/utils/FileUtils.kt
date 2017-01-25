package com.linroid.viewit.utils

import okio.Okio
import java.io.File

/**
 * @author linroid <linroid@gmail.com>
 * @since 25/01/2017
 */
object FileUtils {
    fun copyFile(file: File, desFile: File) {
        val source = Okio.buffer(Okio.source(file))
        val sink = Okio.buffer(Okio.sink(desFile))
        source.readAll(sink)
        source.close()
        sink.close()
    }
}