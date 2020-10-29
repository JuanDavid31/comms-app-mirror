package com.rallytac.engageandroid.legba.data.dto;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity(nameInDb = "CHANNELS_GROUP_WITH_CHANNELS")
public class ChannelsGroupsWithChannels {

    @Id(autoincrement = true)
    private Long id;

    private String channelGroupId;

    private String channelId;

    public ChannelsGroupsWithChannels() {

    }

    public ChannelsGroupsWithChannels(String channelGroupId, String channelId) {
        this.channelGroupId = channelGroupId;
        this.channelId = channelId;
    }

    @Generated(hash = 1812320717)
    public ChannelsGroupsWithChannels(Long id, String channelGroupId, String channelId) {
        this.id = id;
        this.channelGroupId = channelGroupId;
        this.channelId = channelId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChannelGroupId() {
        return channelGroupId;
    }

    public void setChannelGroupId(String channelGroupId) {
        this.channelGroupId = channelGroupId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
}
