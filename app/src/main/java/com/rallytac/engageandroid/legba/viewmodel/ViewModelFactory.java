package com.rallytac.engageandroid.legba.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.rallytac.engageandroid.EngageApplication;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private EngageApplication app;

    public ViewModelFactory(EngageApplication app){
        this.app = app;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass == MissionViewModel.class){
            return (T) new MissionViewModel(app);
        }
        if(modelClass == MissionsListViewModel.class){
            return (T) new MissionsListViewModel(app);
        }
        if (modelClass == ChannelHistoryViewModel.class){
            return (T) new ChannelHistoryViewModel(app);
        }
        throw new IllegalArgumentException("Impossible to instantiate class");
    }
}

