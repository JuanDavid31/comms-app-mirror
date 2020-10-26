package com.rallytac.engageandroid.legba.viewmodel;

import androidx.lifecycle.ViewModel;

import com.rallytac.engageandroid.legba.data.dto.Channel;
import com.rallytac.engageandroid.legba.data.dto.ChannelGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MissionViewModel extends ViewModel {

    private List<ChannelGroup> channelGroups = new ArrayList<>();

    private float toggleRadioChannelButtonRotation = 0;

    public float getToggleRadioChannelButtonRotation() {
        return toggleRadioChannelButtonRotation;
    }

    public void setToggleRadioChannelButtonRotation(float toggleRadioChannelButtonRotation) {
        this.toggleRadioChannelButtonRotation = toggleRadioChannelButtonRotation;
    }

    public List<ChannelGroup> getChannelsGroup() {
        return channelGroups;
    }

    public void setChannelGroups(List<ChannelGroup> channelGroups) {
        this.channelGroups = channelGroups;
    }
}
