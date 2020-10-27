package com.rallytac.engageandroid.legba.data.dto;

import com.google.gson.annotations.JsonAdapter;
import com.rallytac.engageandroid.legba.mapping.ChannelDeserializer;

import java.io.Serializable;
import java.util.List;

@JsonAdapter(ChannelDeserializer.class)
public class Channel implements Serializable {

    public enum ChannelType {PRIMARY, PRIORITY, RADIO}

    public String id;
    public String name;
    public ChannelType type;
    public String image;
    public boolean isActive;
    public boolean isSpeakerOn;
    public List<ChannelElement> channelElements;

    public Channel(String id, String name, ChannelType type, String image,
                   boolean isActive, List<ChannelElement> channelElements) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.image = image;
        this.isActive = isActive;
        this.channelElements = channelElements;
        this.isSpeakerOn = false;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", image='" + image + '\'' +
                ", status='" + isActive + '\'' +
                ", subChannelsAndMembers=" + channelElements +
                '}';
    }
}
