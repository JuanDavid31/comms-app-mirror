package com.upstart13.legba.adapter;

import android.graphics.Rect;
import android.view.TouchDelegate;
import android.view.View;

import androidx.databinding.BindingAdapter;

public class BindingAdapters {

    @BindingAdapter("touch")
    public static void setTouchArea(View view, int dp){
        final View parent = (View) view.getParent();  // button: the view you want to enlarge hit area
        parent.post( new Runnable() {
            public void run() {
                final Rect rect = new Rect();
                view.getHitRect(rect);
                rect.top -= dp;    // increase top hit area
                rect.left -= dp;   // increase left hit area
                rect.bottom += dp; // increase bottom hit area
                rect.right += dp;  // increase right hit area
                parent.setTouchDelegate( new TouchDelegate( rect , view));
            }
        });
    }
}