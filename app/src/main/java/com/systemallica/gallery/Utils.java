package com.systemallica.gallery;

import android.content.Context;

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
        private final String[] okFileExtensions = new String[] { "mp4" };

        public boolean accept(File file) {
            for (String extension : okFileExtensions) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }
    }
}
