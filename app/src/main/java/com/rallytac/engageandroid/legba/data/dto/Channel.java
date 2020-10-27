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
    public boolean status;
    public List<ChannelElement> channelElements;

    public Channel(String id, String name, ChannelType type, String image,
                   boolean status, List<ChannelElement> channelElements) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.image = image;
        this.status = status;
        this.channelElements = channelElements;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", image='" + image + '\'' +
                ", status='" + status + '\'' +
                ", subChannelsAndMembers=" + channelElements +
                '}';
    }
}
