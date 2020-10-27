package com.rallytac.engageandroid.legba.data.dto;

import java.util.List;

public class ChannelGroup {

    public String name;
    public List<Channel> channels;

    public ChannelGroup(String name, List<Channel> channels) {
        this.name = name;
        this.channels = channels;
    }

    @Override
    public String toString() {
        return "ChannelGroup{" +
                "name='" + name +
                '}';
    }
}
