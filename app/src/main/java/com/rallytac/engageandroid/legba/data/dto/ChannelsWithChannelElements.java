package com.rallytac.engageandroid.legba.data.dto;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Generated;

@Entity(nameInDb = "CHANNELS_WITH_CHANNELS_ELEMENTS")
public class ChannelsWithChannelElements {

    @Id(autoincrement = true)
    private Long id;

    private String channelId;

    private Integer channelElementId;

    public ChannelsWithChannelElements() {

    }

    public ChannelsWithChannelElements(Long id) {
        this.id = id;
    }

    @Generated(hash = 599625862)
    public ChannelsWithChannelElements(Long id, String channelId, Integer channelElementId) {
        this.id = id;
        this.channelId = channelId;
        this.channelElementId = channelElementId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public Integer getChannelElementId() {
        return channelElementId;
    }

    public void setChannelElementId(Integer channelElementId) {
        this.channelElementId = channelElementId;
    }

    @Override
    public String toString() {
        return "ChannelsWithChannelElements{" +
                "id=" + id +
                ", channelId='" + channelId + '\'' +
                ", channelElementId=" + channelElementId +
                '}';
    }
}
