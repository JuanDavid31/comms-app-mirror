package com.upstart13.legba.data.dto;

import java.io.Serializable;
import java.util.List;

public class ChannelElement implements Serializable {

    public int id;
    public String name;
    public List<Member> members;
    public String nickName;
    public String type;
    public String range;
    public String number;
    public String state;

    @Override
    public String toString() {
        return "ChannelElement{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", members=" + members +
                ", nickName='" + nickName + '\'' +
                ", type='" + type + '\'' +
                ", range='" + range + '\'' +
                ", number='" + number + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
