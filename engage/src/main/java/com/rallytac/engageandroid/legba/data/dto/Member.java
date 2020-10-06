package com.rallytac.engageandroid.legba.data.dto;

public class Member extends ChannelElement {

    public enum RequestType {PENDING, ADDED, NOT_ADDED;}

    public String nickName;
    public RequestType state;
    public String number;

    public Member(int id, String name, String nickName, RequestType state, String number) {
        super(id, name, ChannelElementType.MEMBER);
        this.nickName = nickName;
        this.state = state;
        this.number = number;
    }

    @Override
    public String toString() {
        return "Member{" +
                "nickName='" + nickName + '\'' +
                ", state=" + state +
                ", number='" + number + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}