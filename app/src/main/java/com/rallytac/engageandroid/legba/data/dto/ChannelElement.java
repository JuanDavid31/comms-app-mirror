package com.rallytac.engageandroid.legba.data.dto;

import androidx.annotation.Nullable;

import com.google.gson.annotations.JsonAdapter;
import com.rallytac.engageandroid.legba.mapping.ChannelElementDeserializer;

import java.io.Serializable;
import java.util.Objects;

@JsonAdapter(ChannelElementDeserializer.class)
public class ChannelElement implements Serializable {

    public enum ChannelElementType {MEMBER, SUBCHANNEL}

    public int id = -1;
    public String name = "";
    public ChannelElementType type;

    public ChannelElement(int id, String name, ChannelElementType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        return "ChannelElement{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return getClass().equals(Objects.requireNonNull(obj).toString());
    }
}
