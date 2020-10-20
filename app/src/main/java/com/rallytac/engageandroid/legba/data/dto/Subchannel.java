package com.rallytac.engageandroid.legba.data.dto;

import java.io.Serializable;
import java.util.List;

public class Subchannel extends ChannelElement implements Serializable {

    public String image;
    public List<Member> members;

    public Subchannel(int id, String name) {
        super(id, name, ChannelElementType.SUBCHANNEL);
    }

    @Override
    public String toString() {
        return "SubChannel{" +
                "image='" + image + '\'' +
                ", members=" + members +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}