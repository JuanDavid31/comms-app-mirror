package com.rallytac.engageandroid.legba.data.dto;

import androidx.annotation.Nullable;

import com.google.gson.annotations.JsonAdapter;
import com.rallytac.engageandroid.legba.mapping.ChannelElementDeserializer;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.converter.PropertyConverter;

import java.io.Serializable;
import java.util.Objects;

@Entity(nameInDb = "CHANNEL_ELEMENT")
@JsonAdapter(ChannelElementDeserializer.class)
public class ChannelElement implements Serializable {

    private static final long serialVersionUID = 7294779592633136804L;

    @Id(autoincrement = true)
    private Long id;

    protected String name;

    @Convert(converter = ChannelElementTypeConverter.class, columnType = String.class)
    protected ChannelElementType type;

    public ChannelElement() {

    }

    public ChannelElement(Long id) {
        this.id = id;
    }

    @Generated(hash = 2036434497)
    public ChannelElement(Long id, String name, ChannelElementType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChannelElementType getType() {
        return type;
    }

    public void setType(ChannelElementType type) {
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

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    //ChannelElementType
    public enum ChannelElementType {MEMBER, SUBCHANNEL}

    public static class ChannelElementTypeConverter implements PropertyConverter<ChannelElementType, String> {
        @Override
        public ChannelElementType convertToEntityProperty(String databaseValue) {
            return ChannelElementType.valueOf(databaseValue);
        }

        @Override
        public String convertToDatabaseValue(ChannelElementType entityProperty) {
            return entityProperty.name();
        }
    }
}
