package com.upstart13.legba.data.dto;

import java.io.Serializable;
import java.util.List;

public class Channel  implements Serializable {

    public int id;
    public String name;
    public String type; //TODO: Custom converter for enum
    public String image;
    public List<ChannelElement> subChannelsAndMembers;

    @Override
    public String toString() {
        return "Channel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", image='" + image + '\'' +
                ", subChannelsAndMembers=" + subChannelsAndMembers +
                '}';
    }
}
