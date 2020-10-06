package com.rallytac.engageandroid.legba.data.dto;

import java.io.Serializable;
import java.util.List;

public class Mission implements Serializable {
    public int id;
    public String name;
    public List<Channel> channels;

    @Override
    public String toString() {
        return "Mission{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", channels=" + channels +
                '}';
    }
}
