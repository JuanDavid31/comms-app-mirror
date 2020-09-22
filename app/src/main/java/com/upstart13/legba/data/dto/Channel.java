package com.upstart13.legba.data.dto;

public class Channel {

    public String name;
    public String type; //TODO: Custom converter for enum
    public String image;

    @Override
    public String toString() {
        return "Channel{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
