package com.rallytac.engageandroid.legba.mapping;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.rallytac.engageandroid.legba.data.dto.ChannelElement;
import com.rallytac.engageandroid.legba.data.dto.Member;
import com.rallytac.engageandroid.legba.data.dto.Subchannel;

import java.lang.reflect.Type;

public class ChannelElementDeserializer<T extends ChannelElement> implements JsonDeserializer<T> {

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        if(jsonObject.get("type").getAsString().equals("member")){
            return (T) getMember(jsonObject);
        }else{
            return (T) getSubchannel(jsonObject);
        }
    }

    private Member getMember(JsonObject jsonObject) {
        Member.RequestType requestType;

        switch (jsonObject.get("state").getAsString()){
            case "added":
                requestType = Member.RequestType.ADDED;
                break;
            case "not added":
                requestType = Member.RequestType.NOT_ADDED;
                break;
            case "pending":
                requestType = Member.RequestType.PENDING;
                break;
            default:
                requestType = Member.RequestType.PENDING;
                break;
        }

        return new Member(
                jsonObject.get("id").getAsLong(),
                jsonObject.get("name").getAsString(),
                jsonObject.get("subchannelId") != null ? jsonObject.get("subchannelId").getAsLong() : null,
                jsonObject.get("nickName").getAsString(),
                jsonObject.get("number").getAsString(),
                requestType
        );
    }

    private Subchannel getSubchannel(JsonObject jsonObject) {
        return new Gson().fromJson(jsonObject.toString(), new TypeToken<Subchannel>(){}.getType());
    }
}
