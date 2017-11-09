package com.systemallica.gallery;

import android.content.Context;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;

class Utils {
    static int dpToPx(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    static class SortFoldersByName implements Comparator<FolderItem> {
        @Override
        public int compare(FolderItem o1, FolderItem o2) {
            return (o1.getTitle()).compareToIgnoreCase(o2.getTitle());
        }
    }

    static class SortFilesByDate implements Comparator<File> {
        @Override
        public int compare(File o1, File o2) {
            return Long.valueOf(o2.lastModified()).compareTo(o1.lastModified());
        }
    }

    static class ImageFileFilter implements FileFilter {
        private final String[] okFileExtensions = new String[] { "jpg", "jpeg", "png", "gif" };

        public boolean accept(File file) {
            for (String extension : okFileExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }
    }

    static class VideoFileFilter implements FileFilter {
        private final String[] okFileExtensions = new String[] { "mp4", "3gp" };

        public boolean accept(File file) {
            for (String extension : okFileExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }
    }

    static class MediaFileFilter implements FileFilter {
        private final String[] okFileExtensions = new String[] { "jpg", "jpeg", "png", "gif", "mp4", "3gp" };

        public boolean accept(File file) {
            for (String extension : okFileExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }
    }
    static boolean isVideo(String file){
        return(file.endsWith(".mp4") || file.endsWith(".3gp"));
    }

    static boolean isVideoOrGif(String file){
        return(file.endsWith(".mp4") || file.endsWith(".3gp") || file.endsWith(".gif"));
    }

    static boolean isGif(String file){
        return(file.endsWith(".gif"));
    }

    static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
}
