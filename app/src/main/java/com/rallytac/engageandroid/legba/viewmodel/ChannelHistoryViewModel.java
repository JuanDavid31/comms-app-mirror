package com.rallytac.engageandroid.legba.viewmodel;

import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rallytac.engage.engine.Engine;
import com.rallytac.engageandroid.EngageApplication;
import com.rallytac.engageandroid.Utils;
import com.rallytac.engageandroid.legba.data.dto.Audio;
import com.rallytac.engageandroid.legba.data.dto.Channel;
import com.rallytac.engageandroid.legba.data.dto.ChannelDao;
import com.rallytac.engageandroid.legba.data.dto.DaoSession;
import com.rallytac.engageandroid.legba.data.dto.Mission;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ChannelHistoryViewModel extends ViewModel {

    private ChannelDao channelDao;
    private Channel channel;

    public ChannelHistoryViewModel(EngageApplication app) {
        DaoSession daoSession = app.getDaoSession();
        channelDao = daoSession.getChannelDao();
    }

    public String getChannelId() {
        return channel.getId();
    }

    public List<Audio> mapTimelineAudiosToAudio(String reportJson) {
        if (!Utils.isEmptyString(reportJson)) {
            try {
                JSONObject root = new JSONObject(reportJson);
                String timelineAudios = root.getJSONArray(Engine.JsonFields.TimelineReport.events).toString();

                Type listType = new TypeToken<List<Audio>>() {
                }.getType();

                return new Gson().fromJson(timelineAudios, listType);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyList();
    }

    public void setupChannel(String channelId) {
        channel = channelDao.load(channelId);
    }

    public String getChannelName() {
        return channel.getName();
    }

    public String getChannelImage() {
        return channel.getImage();
    }
}