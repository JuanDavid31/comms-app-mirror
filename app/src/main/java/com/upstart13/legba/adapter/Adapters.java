package com.upstart13.legba.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.BindingAdapter;

import com.upstart13.legba.util.DimUtils;

import timber.log.Timber;

public class Adapters {
    @BindingAdapter("calc_width")
    public static void calHeight(View view, int dp){

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

        layoutParams.width = 300;

        view.setLayoutParams(layoutParams);

        Timber.i(String.valueOf(dp));
        Timber.i("View width - " + ((View)view.getParent()).getWidth());
    }
}