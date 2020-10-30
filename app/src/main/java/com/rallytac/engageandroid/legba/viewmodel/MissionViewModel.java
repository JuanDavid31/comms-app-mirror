package com.rallytac.engageandroid.legba.viewmodel;

import android.app.Activity;

import androidx.lifecycle.ViewModel;

import com.rallytac.engageandroid.EngageApplication;
import com.rallytac.engageandroid.legba.data.dto.Channel;
import com.rallytac.engageandroid.legba.data.dto.ChannelDao;
import com.rallytac.engageandroid.legba.data.dto.ChannelElementDao;
import com.rallytac.engageandroid.legba.data.dto.ChannelGroup;
import com.rallytac.engageandroid.legba.data.dto.ChannelGroupDao;
import com.rallytac.engageandroid.legba.data.dto.ChannelsGroupsWithChannels;
import com.rallytac.engageandroid.legba.data.dto.ChannelsGroupsWithChannelsDao;
import com.rallytac.engageandroid.legba.data.dto.DaoSession;
import com.rallytac.engageandroid.legba.data.dto.Mission;
import com.rallytac.engageandroid.legba.data.dto.MissionDao;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MissionViewModel extends ViewModel {

    private Mission mission;

    private float toggleRadioChannelButtonRotation = 0;

    private MissionDao missionDao;
    private ChannelGroupDao channelGroupDao;
    private ChannelDao channelDao;
    private ChannelElementDao channelElementDao;
    private ChannelsGroupsWithChannelsDao channelsGroupsWithChannelsDao;

    public MissionViewModel(Activity activity) {
        DaoSession daoSession = ((EngageApplication) activity.getApplication()).getDaoSession();
        missionDao = daoSession.getMissionDao();
        channelGroupDao = daoSession.getChannelGroupDao();
        channelDao = daoSession.getChannelDao();
        channelElementDao = daoSession.getChannelElementDao();
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

    public void addChannelsGroup(ChannelGroup... channelGroup) {
        for (ChannelGroup current : channelGroup) {
            addChannelGroup(current);
        }
    }

    public void addChannelGroup(ChannelGroup channelGroup) {
        channelGroupDao.insertOrReplace(channelGroup);
        for(Channel channel: channelGroup.getChannels()) {
            channelsGroupsWithChannelsDao.insert(new ChannelsGroupsWithChannels(channelGroup.getName(), channel.getId()));
        }
        mission.update();
    }

    public void updateChannelGroup(ChannelGroup currentChannelGroup, String lastName) {
        deleteChannelGroupsWithChannelsByChannelGroupId(lastName);
        if(!lastName.equals(currentChannelGroup.getName())) {
            channelGroupDao.deleteByKey(lastName);
        }

        addChannelGroup(currentChannelGroup);
    }

    public void deleteChannelGroup(int currentPage) {
        ChannelGroup channelGroup = mission.getChannelsGroups().get(currentPage);
        deleteChannelGroupsWithChannelsByChannelGroupId(channelGroup.getName());
        channelGroupDao.deleteByKey(channelGroup.getName());
        mission.getChannelsGroups().remove(currentPage);
        mission.update();
    }

    private void deleteChannelGroupsWithChannelsByChannelGroupId(String channelGroupName) {
        channelsGroupsWithChannelsDao.queryBuilder()
                .where(ChannelsGroupsWithChannelsDao.Properties.ChannelGroupId.eq(channelGroupName))
                .buildDelete().executeDeleteWithoutDetachingEntities();
    }

    public void setupMission(Mission mission) {
        this.mission = mission;
        getMissionById(mission.getId())
                .ifPresent(updatedMission -> this.mission = updatedMission);
    }

    public Optional<Mission> getMissionById(String id) {
        if(missionDao.loadAll().size() == 0) {
            channelElementDao.insertOrReplaceInTx(mission.getChannels().get(0).getChannelElements());
            channelDao.insertOrReplaceInTx(mission.getChannels());
            missionDao.insertOrReplace(mission);
        }

        return missionDao.loadAll()
                .stream()
                .filter(mission -> mission.getId().equals(id))
                .findFirst();
    }

    public Mission getMission() {
        return mission;
    }

    public List<Channel> getAllChannels(){
        return mission.getChannels();
    }

    private List<ChannelGroup> getInitialChannelsGroup() {
        if (getChannelsGroup().size() > 0) {
            return getChannelsGroup();
        }
        List<Channel> page1List = mission.getChannels().stream().limit(1).collect(Collectors.toList());
        List<Channel> page2List = mission.getChannels().stream().limit(2).collect(Collectors.toList());
        List<Channel> page3List = mission.getChannels().stream().limit(3).collect(Collectors.toList());
        List<Channel> page4List = mission.getChannels().stream().limit(4).collect(Collectors.toList());
        List<Channel> page5List = mission.getChannels().stream().limit(5).collect(Collectors.toList());

        ChannelGroup channelGroup = new ChannelGroup("First", mission.getId(), page1List);
        ChannelGroup channelGroup1 = new ChannelGroup("Delta", mission.getId(), page2List);
        ChannelGroup channelGroup2 = new ChannelGroup("Third", mission.getId(), page3List);
        ChannelGroup channelGroup3 = new ChannelGroup("Echo", mission.getId(), page4List);
        ChannelGroup channelGroup4 = new ChannelGroup("charlie", mission.getId(), page5List);

        addChannelsGroup(channelGroup, channelGroup1, channelGroup2, channelGroup3, channelGroup4);
        return getChannelsGroup();
    }
}