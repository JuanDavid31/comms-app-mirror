package com.upstart13.legba.util;

import androidx.fragment.app.Fragment;

public class DimUtils {
    public static int convertDpToPx(Fragment fragment, double dp){
        final float scale = fragment.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
