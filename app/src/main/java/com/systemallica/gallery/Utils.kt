package com.systemallica.gallery

import android.content.Context
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileFilter
import java.util.*
import kotlin.math.roundToInt

internal object Utils {
    fun dpToPx(dp: Int, context: Context): Int {
        val density = context.resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }

    fun isVideo(file: String): Boolean {
        return file.endsWith(".mp4") || file.endsWith(".3gp")
    }

    fun isVideoOrGif(file: String): Boolean {
        return file.endsWith(".mp4") || file.endsWith(".3gp") || file.endsWith(".gif")
    }

    fun isGif(file: String): Boolean {
        return file.endsWith(".gif")
    }

    fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    // Returns name of file without its extension
    fun getBaseName(file: File): String {
        var name = file.name
        val pos = name.lastIndexOf(".")
        if (pos > 0) {
            name = name.substring(0, pos)
        }
        return name
    }

    // Returns extension of file (including the dot)
    fun getExtension(file: File): String {
        var extension = file.name
        val pos = extension.lastIndexOf(".")
        if (pos > 0) {
            extension = extension.substring(pos, extension.length)
        }
        return extension
    }

    internal class SortFoldersByName : Comparator<FolderItem> {
        override fun compare(o1: FolderItem, o2: FolderItem): Int {
            return o1.title.compareTo(o2.title, ignoreCase = true)
        }
    }

    internal class SortFilesByDate : Comparator<File> {
        override fun compare(o1: File, o2: File): Int {
            return o2.lastModified().compareTo(o1.lastModified())
        }
    }

    internal class ImageFileFilter : FileFilter {
        private val okFileExtensions = arrayOf("jpg", "jpeg", "png", "gif")
        override fun accept(file: File): Boolean {
            for (extension in okFileExtensions) {
                if (file.name.toLowerCase(Locale.ROOT).endsWith(extension)) {
                    return true
                }
            }
            return false
        }
    }

    internal class VideoFileFilter : FileFilter {
        private val okFileExtensions = arrayOf("mp4", "3gp")
        override fun accept(file: File): Boolean {
            for (extension in okFileExtensions) {
                if (file.name.toLowerCase(Locale.ROOT).endsWith(extension)) {
                    return true
                }
            }
            return false
        }
    }

    internal class MediaFileFilter : FileFilter {
        private val okFileExtensions = arrayOf("jpg", "jpeg", "png", "gif", "mp4", "3gp")
        override fun accept(file: File): Boolean {
            for (extension in okFileExtensions) {
                if (file.name.toLowerCase(Locale.ROOT).endsWith(extension)) {
                    return true
                }
            }
            return false
        }
    }
}