package com.linroid.viewit.data.model

import com.linroid.viewit.utils.PathUtils
import com.linroid.viewit.utils.THUMBNAIL_MAX_COUNT
import com.linroid.viewit.utils.WildcardMatcher
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

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
            if (children.containsKey(subDir)) {
                children[subDir]!!.merge(child)
            } else {
                children.put(subDir, child)
            }
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

    private fun merge(other: ImageTree) {
        images.addAll(other.images)
        for ((subDir, imageTree) in other.children) {
            if (children.containsKey(subDir)) {
                children[subDir]!!.merge(imageTree)
            } else {
                children.put(subDir, imageTree)
            }
        }
    }

    fun nonEmptyChild(): ImageTree {
        if (images.size == 0) {
            if (children.size == 1) {
                return children.values.first().nonEmptyChild()
            }
        }
        return this
    }

    fun removeImage(image: Image) {
        val tree = find(PathUtils.parent(image.path))
        if (tree != null) {
            tree.images.remove(image)
        }
    }

    fun insertImage(image: Image) {
        var tree = find(PathUtils.parent(image.path))
        if (tree != null) {
            tree.images.add(image)
        } else {
            tree = ImageTree(PathUtils.parent(image.path))
            tree.images.add(image)
            add(tree)
        }
    }

    private fun subDir(path: String): String {
        return path.substringAfter(dir).split(File.separator)[
                if (dir.endsWith(File.separator)) 0 else 1
                ]
    }

    fun allImages(): List<Image> {
        val list = ArrayList<Image>()
        allImages(list)
        return list
    }

    private fun allImages(list: MutableList<Image>) {
        if (images.size > 0) {
            list.addAll(images)
        }
        if (children.size > 0) {
            for ((s, child) in children) {
                child.allImages(list)
            }
        }
    }

    val thumbnailImages: List<Image> by lazy {
        val list = ArrayList<Image>()
        thumbnailImages(list)
        return@lazy list
    }

//    fun thumbnailImages(): List<Image> {
//        val listWithChangObserver = ArrayList<Image>()
//        thumbnailImages(listWithChangObserver)
//        return listWithChangObserver
//    }

    private fun thumbnailImages(list: ArrayList<Image>) {
        list.addAll(images.take(THUMBNAIL_MAX_COUNT - list.size))
        if (list.size < THUMBNAIL_MAX_COUNT) {
            for ((s, tree) in children) {
                tree.thumbnailImages(list)
                if (list.size >= THUMBNAIL_MAX_COUNT) {
                    break
                }
            }
        }
    }

    fun allImagesCount(): Int {
        val count = AtomicInteger(0)
        allImagesCount(count)
        return count.get()
    }

    private fun allImagesCount(count: AtomicInteger) {
        count.addAndGet(images.size)
        for ((s, imageTree) in children) {
            imageTree.allImagesCount(count)
        }
    }

    private fun match(pattern: String): ImageTree? {
        if (WildcardMatcher.match(dir, pattern)) {
            return this
        }
        for ((s, tree) in children) {
            val found = tree.match(pattern)
            if (found != null) {
                return found
            }
        }
        return null
    }

    fun find(path: String): ImageTree? {
        if (path.contains("*")) {
            return match(path)
        }
        if (dir == path) {
            return this
        }
        val subDir = subDir(path)
        if (children.containsKey(subDir)) {
            val child = children[subDir]
            return children[subDir]!!.find(path)
        }
        return null
    }
}