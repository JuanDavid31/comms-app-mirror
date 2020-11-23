package com.rallytac.engageandroid.legba.data.dto;

import androidx.annotation.Nullable;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.converter.PropertyConverter;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

public class Member implements Serializable {

    private static final long serialVersionUID = -6152844486729153797L;

    private Long id;

    private String name;

    //Todo: nickName is unique? @Unique
    private String nickName;

    private String number;

    @Convert(converter = RequestTypeConverter.class, columnType = String.class)
    private RequestType state;

    public Member() {
    }


    public Member(Long id, String name, String nickName, String number, RequestType state) {
        this.id = id;
        this.name = name;
        this.nickName = nickName;
        this.number = number;
        this.state = state;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public RequestType getState() {
        return state;
    }

    public void setState(RequestType state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Member{" +
                "nickName='" + nickName + '\'' +
                ", state=" + state +
                ", number='" + number + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return toString().equalsIgnoreCase(obj.toString());
    }

    public enum RequestType {PENDING, ADDED, NOT_ADDED;}

    public static class RequestTypeConverter implements PropertyConverter<RequestType, String> {
        @Override
        public RequestType convertToEntityProperty(String databaseValue) {
            return RequestType.valueOf(databaseValue);
        }

        @Override
        public String convertToDatabaseValue(RequestType entityProperty) {
            return entityProperty.name();
        }
    }
}