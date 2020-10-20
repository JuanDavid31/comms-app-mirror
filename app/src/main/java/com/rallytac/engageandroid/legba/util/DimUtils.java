package com.rallytac.engageandroid.legba.util;

import android.content.Context;

import androidx.fragment.app.Fragment;

public class DimUtils {
    public static int convertDpToPx(Fragment fragment, double dp){
        final float scale = fragment.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int convertDpToPx(Context context, double dp){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
