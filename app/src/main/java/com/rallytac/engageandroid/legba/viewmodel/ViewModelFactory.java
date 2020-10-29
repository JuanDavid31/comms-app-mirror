package com.rallytac.engageandroid.legba.viewmodel;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private Activity activity;

    public ViewModelFactory(Activity activity){
        this.activity = activity;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass == MissionViewModel.class){
            return (T) new MissionViewModel(activity);
        }
        throw new IllegalArgumentException("Impossible to instantiate class");
    }
}

