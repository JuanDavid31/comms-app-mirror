package com.rallytac.engageandroid.legba.data.dto;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity(nameInDb = "CHANNELS_GROUP_WITH_CHANNELS")
public class ChannelsGroupsWithChannels {

    @Id(autoincrement = true)
    private Long id;

    private Long channelGroupId;

    private String channelId;

    public ChannelsGroupsWithChannels() {

    }

    public ChannelsGroupsWithChannels(Long channelGroupId, String channelId) {
        this.channelGroupId = channelGroupId;
        this.channelId = channelId;
    }

    @Generated(hash = 1586546642)
    public ChannelsGroupsWithChannels(Long id, Long channelGroupId, String channelId) {
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

    public Long getChannelGroupId() {
        return channelGroupId;
    }

    public void setChannelGroupId(Long channelGroupId) {
        this.channelGroupId = channelGroupId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
}
