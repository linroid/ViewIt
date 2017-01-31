package com.linroid.viewit.data.model

import com.linroid.viewit.utils.PathUtils
import java.io.File
import java.util.*

/**
 * @author linroid <linroid@gmail.com>
 * @since 30/01/2017
 */
data class ImageTree(val dir: String, var parent: ImageTree? = null) {
    val images = ArrayList<Image>()
    val children = HashMap<String, ImageTree>()
    fun add(child: ImageTree) {
        val subDir = subDir(child.dir)
        if (dir == PathUtils.parent(child.dir)) {
            child.parent = this
            children.put(subDir, child)
            return
        }
        val subTree: ImageTree;
        if (children.containsKey(subDir)) {
            subTree = children[subDir]!!
        } else {
            subTree = ImageTree(PathUtils.append(dir, subDir))
        }
        subTree.add(child)
        children.put(subDir, subTree)
    }

    fun firstChild(): ImageTree {
        return children.values.first()
    }

    fun removeImage(image: Image) {
        val tree = getChildTree(PathUtils.parent(image.path))
        if (tree != null) {
            tree.images.remove(image)
        }
    }

    fun insertImage(image: Image) {
        var tree = getChildTree(PathUtils.parent(image.path))
        if (tree != null) {
            tree.images.add(image)
        } else {
            tree = ImageTree(PathUtils.parent(image.path))
            tree.images.add(image)
            add(tree)
        }
    }

    fun getChildTree(path: String): ImageTree? {
        if (dir == path) {
            return this
        }
        val subDir = subDir(path)
        if (children.containsKey(subDir)) {
            val child = children[subDir]
            return children[subDir]!!.getChildTree(path)
        }
        return null
    }

    private fun subDir(path: String): String {
        return path.substringAfter(dir).split(File.separator)[
                if (dir.endsWith(File.separator)) 0 else 1
                ]
    }

    fun getAllImages(): List<Image> {
        val list = ArrayList<Image>()
        getAllImages(list)
        return list
    }

    private fun getAllImages(list: MutableList<Image>) {
        if (images.size > 0) {
            list.addAll(images)
        }
        if (children.size > 0) {
            children.forEach { s, child -> child.getAllImages(list) }
        }
    }
}