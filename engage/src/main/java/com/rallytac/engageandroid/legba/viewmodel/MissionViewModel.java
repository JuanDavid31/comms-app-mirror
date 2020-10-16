package com.rallytac.engageandroid.legba.viewmodel;

import androidx.lifecycle.ViewModel;

public class MissionViewModel extends ViewModel {

    private float toggleRadioChannelButtonRotation = 0;

    public float getToggleRadioChannelButtonRotation() {
        return toggleRadioChannelButtonRotation;
    }

    public void setToggleRadioChannelButtonRotation(float toggleRadioChannelButtonRotation) {
        this.toggleRadioChannelButtonRotation = toggleRadioChannelButtonRotation;
    }
}
