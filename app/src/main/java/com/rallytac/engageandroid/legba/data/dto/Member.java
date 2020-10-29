package com.rallytac.engageandroid.legba.data.dto;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.converter.PropertyConverter;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

@Entity(nameInDb = "MEMBERS")
public class Member extends ChannelElement implements Serializable {

    private static final long serialVersionUID = -6152844486729153797L;

    @Id(autoincrement = true)
    private Long memberId;

    private Long subchannelId;

    //Todo: nickName is unique? @Unique
    private String nickName;

    private String number;

    @Convert(converter = RequestTypeConverter.class, columnType = String.class)
    private RequestType state;

    public Member() {
    }

    public Member(Long memberId) {
        super(memberId);
    }

    @Generated(hash = 1114883690)
    public Member(Long memberId, Long subchannelId, String nickName, String number, RequestType state) {
        this.memberId = memberId;
        this.subchannelId = subchannelId;
        this.nickName = nickName;
        this.number = number;
        this.state = state;
    }

    public Member(Long memberId, String name, Long subchannelId, String nickName, String number, RequestType state) {
        super(memberId, name, ChannelElementType.MEMBER);
        this.subchannelId = subchannelId;
        this.nickName = nickName;
        this.number = number;
        this.state = state;
    }

    public Long getMemberId() {
        return this.memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getSubchannelId() {
        return subchannelId;
    }

    public void setSubchannelId(Long subchannelId) {
        this.subchannelId = subchannelId;
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
                ", id=" + memberId +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
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