package com.rallytac.engageandroid.legba.viewmodel;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class MissionViewModel extends ViewModel {

    private float toggleRadioChannelButtonRotation = 0;

    private List<SpeakerState> speakerStates = new ArrayList<>();

    public float getToggleRadioChannelButtonRotation() {
        return toggleRadioChannelButtonRotation;
    }

    public void setToggleRadioChannelButtonRotation(float toggleRadioChannelButtonRotation) {
        this.toggleRadioChannelButtonRotation = toggleRadioChannelButtonRotation;
    }

    class SpeakerState {
        public String groupId;
        public boolean isSpeakerOn;

        public SpeakerState(){}

        public SpeakerState(String groupId, boolean isSpeakerOn) {
            this.groupId = groupId;
            this.isSpeakerOn = isSpeakerOn;
        }
    }
}
