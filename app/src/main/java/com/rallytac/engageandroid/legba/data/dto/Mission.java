package com.rallytac.engageandroid.legba.data.dto;

import java.io.Serializable;
import java.util.List;

public class Mission implements Serializable {
    public String id;
    public String name;
    public List<Channel> channels;

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    @Override
    public String toString() {
        return "Mission{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", channels=" + channels +
                '}';
    }
}
