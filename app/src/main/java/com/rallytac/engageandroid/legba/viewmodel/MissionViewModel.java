package com.rallytac.engageandroid.legba.viewmodel;

import android.app.Activity;

import androidx.lifecycle.ViewModel;

import com.rallytac.engageandroid.EngageApplication;
import com.rallytac.engageandroid.legba.data.dto.Channel;
import com.rallytac.engageandroid.legba.data.dto.ChannelDao;
import com.rallytac.engageandroid.legba.data.dto.ChannelGroup;
import com.rallytac.engageandroid.legba.data.dto.ChannelGroupDao;
import com.rallytac.engageandroid.legba.data.dto.ChannelsGroupsWithChannels;
import com.rallytac.engageandroid.legba.data.dto.ChannelsGroupsWithChannelsDao;
import com.rallytac.engageandroid.legba.data.dto.DaoSession;
import com.rallytac.engageandroid.legba.data.dto.Mission;
import com.rallytac.engageandroid.legba.data.dto.MissionDao;
import com.rallytac.engageandroid.legba.engage.GroupDiscoveryInfo;
import com.rallytac.engageandroid.legba.engage.GroupDiscoveryInfo.Identity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import timber.log.Timber;

public class MissionViewModel extends ViewModel {

    private Mission mission;

    private float toggleRadioChannelButtonRotation = 0;

    private MissionDao missionDao;
    private ChannelGroupDao channelGroupDao;
    private ChannelsGroupsWithChannelsDao channelsGroupsWithChannelsDao;
    HashMap<String, Set<Identity>> channelUsers = new HashMap<>();

    public MissionViewModel(EngageApplication app) {
        DaoSession daoSession = app.getDaoSession();
        missionDao = daoSession.getMissionDao();
        channelGroupDao = daoSession.getChannelGroupDao();
        channelsGroupsWithChannelsDao = daoSession.getChannelsGroupsWithChannelsDao();
    }

    public float getToggleRadioChannelButtonRotation() {
        return toggleRadioChannelButtonRotation;
    }

    public void setToggleRadioChannelButtonRotation(float toggleRadioChannelButtonRotation) {
        this.toggleRadioChannelButtonRotation = toggleRadioChannelButtonRotation;
    }

    public List<ChannelGroup> getChannelsGroup() {
        return mission.getChannelsGroups();
    }

    public void addChannelGroup(ChannelGroup channelGroup) {
        channelGroupDao.insertOrReplace(channelGroup);
        for(Channel channel: channelGroup.getChannels()) {
            channelsGroupsWithChannelsDao.insert(new ChannelsGroupsWithChannels(channelGroup.getId(), channel.getId()));
        }
        mission.update();
    }

    public void updateChannelGroup(ChannelGroup currentChannelGroup) {
        deleteChannelGroupsWithChannelsByChannelGroupId(currentChannelGroup.getId());
        addChannelGroup(currentChannelGroup);
    }

    public void deleteChannelGroup(int currentPage) {
        ChannelGroup channelGroup = mission.getChannelsGroups().get(currentPage);
        deleteChannelGroupsWithChannelsByChannelGroupId(channelGroup.getId());
        channelGroupDao.deleteByKey(channelGroup.getId());
        mission.getChannelsGroups().remove(currentPage);
        mission.update();
    }

    private void deleteChannelGroupsWithChannelsByChannelGroupId(Long channelGroupId) {
        channelsGroupsWithChannelsDao.queryBuilder()
                .where(ChannelsGroupsWithChannelsDao.Properties.ChannelGroupId.eq(channelGroupId))
                .buildDelete().executeDeleteWithoutDetachingEntities();
    }

    public void setupMission(Mission mission) {
        this.mission = mission;
        getMissionById(mission.getId())
                .ifPresent(updatedMission -> this.mission = updatedMission);
    }

    public Optional<Mission> getMissionById(String id) {
        return missionDao.loadAll()
                .stream()
                .filter(mission -> mission.getId().equals(id))
                .findFirst();
    }

    public Mission getMission() {
        return mission;
    }

    public List<Channel> getAudioChannels() {
        return mission.getChannels()
                .stream()
                .filter(channel -> channel.getEngageType() == Channel.EngageType.AUDIO)
                .collect(Collectors.toList());
    }

    public void addChannelUser(GroupDiscoveryInfo groupDiscoveryInfo) {
        groupDiscoveryInfo
                .groupAliases
                .forEach(groupAlias -> {
                    Set<Identity> identities = channelUsers.get(groupAlias.groupId);
                    if (identities == null) {
                        identities = new HashSet<>();
                        identities.add(groupDiscoveryInfo.identity);
                        channelUsers.put(groupAlias.groupId, identities);
                    } else {
                        identities.add(groupDiscoveryInfo.identity);
                    }
                    Timber.i("New identity added %s", groupDiscoveryInfo.identity);
                });
        updateChannels();
    }

    private void updateChannels(){
        getAudioChannels().forEach(channel -> channel.users = getUsersByChannelId(channel.getId()));
    }

    public void removeChannelUser(GroupDiscoveryInfo groupDiscoveryInfo) {
        groupDiscoveryInfo
                .groupAliases
                .forEach(groupAlias -> {
                    Set<Identity> identities = channelUsers.get(groupAlias.groupId);
                    if(identities != null){ identities.remove(groupDiscoveryInfo.identity); }
                });
        updateChannels();
    }

    public List<Identity> getUsersByChannelId(String channelId){
        Set<Identity> identities = channelUsers.get(channelId);
        if (identities != null){
            return new ArrayList<>(identities);
        }else {
            return new ArrayList<>();
        }

    }
}