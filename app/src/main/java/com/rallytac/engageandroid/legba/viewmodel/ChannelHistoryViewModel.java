package com.rallytac.engageandroid.legba.viewmodel;

import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rallytac.engage.engine.Engine;
import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.SimpleUiMainActivity;
import com.rallytac.engageandroid.Utils;
import com.rallytac.engageandroid.legba.data.dto.Audio;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChannelHistoryViewModel extends ViewModel {

    private String channelId;

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelId() {
        return channelId;
    }

    public List<Audio> mapTimelineAudiosToAudio(String reportJson) {
        if (!Utils.isEmptyString(reportJson)) {
            try {
                JSONObject root = new JSONObject(reportJson);
                String events = root.getJSONArray(Engine.JsonFields.TimelineReport.events).toString();

                Type listType = new TypeToken<List<Audio>>() {
                }.getType();

                return new Gson().fromJson(events, listType);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyList();
    }
}