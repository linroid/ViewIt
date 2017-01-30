package com.linroid.viewit.utils

import java.io.File

/**
 * @author linroid <linroid@gmail.com>
 * @since 30/01/2017
 */
object PathUtils {
    /**
     * 给路径添加一个子路径
     */
    fun append(dir: String, sub: String): String {
        if (dir.endsWith(File.separator)) {
            return dir + sub
        } else {
            return dir + File.separator + sub
        }
    }

    fun parent(path: String): String {
        return path.substringBeforeLast(File.separator)
    }

    fun relative(parent: String, path: String): String {
        if (parent == File.separator) {
            return path
        }
        val p = path.substringAfter(parent)
        if (p.startsWith(File.separatorChar)) {
            return p.substringAfter(File.separator)
        }
        return p
    }

    fun name(path: String): String {
        if (path.endsWith(File.separator)) {
            return path.substringBeforeLast(File.separator).substringAfterLast(File.separator)
        } else {
            return path.substringAfterLast(File.separator)
        }
    }
}