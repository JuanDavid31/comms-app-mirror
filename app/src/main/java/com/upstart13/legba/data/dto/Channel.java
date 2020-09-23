package com.upstart13.legba.data.dto;

import java.io.Serializable;
import java.util.List;

public class Channel  implements Serializable {

    public String name;
    public String type; //TODO: Custom converter for enum
    public String image;
    public List<ChannelElement> subChannelsAndMembers;

    @Override
    public String toString() {
        return "Channel{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", image='" + image + '\'' +
                ", subChannels=" + subChannelsAndMembers +
                '}';
    }
}
