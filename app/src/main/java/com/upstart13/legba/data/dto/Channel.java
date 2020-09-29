package com.upstart13.legba.data.dto;

import com.google.gson.annotations.JsonAdapter;
import com.upstart13.legba.mapping.ChannelDeserializer;

import java.io.Serializable;
import java.util.List;

@JsonAdapter(ChannelDeserializer.class)
public class Channel implements Serializable {

    public enum ChannelType {PRIMARY, PRIORITY, RADIO}

    public int id;
    public String name;
    public ChannelType type;
    public String image;
    public List<ChannelElement> channelElements;

    public Channel(int id, String name, ChannelType type, String image, List<ChannelElement> channelElements) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.image = image;
        this.channelElements = channelElements;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", image='" + image + '\'' +
                ", subChannelsAndMembers=" + channelElements +
                '}';
    }
}
