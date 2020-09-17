package com.upstart13.legba.data.dto;

import java.util.List;

public class Mission {
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
