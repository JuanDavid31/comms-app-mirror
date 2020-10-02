package com.upstart13.legba.adapter;

import android.view.View;

import androidx.databinding.BindingAdapter;

import timber.log.Timber;

public class Adapters {
    @BindingAdapter("calc_width")
    public static void calHeight(View view, int dp){

        Timber.i(String.valueOf(dp));
        Timber.i("View width - " + view.getWidth());
    }
}
