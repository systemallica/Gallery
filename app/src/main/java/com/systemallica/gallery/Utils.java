package com.systemallica.gallery;

import android.content.Context;

class Utils {
    static int dpToPx(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
