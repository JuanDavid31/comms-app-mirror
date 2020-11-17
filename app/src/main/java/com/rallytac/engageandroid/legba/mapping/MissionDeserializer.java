package com.rallytac.engageandroid.legba.mapping;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.rallytac.engageandroid.legba.data.dto.Channel;
import com.rallytac.engageandroid.legba.data.dto.ChannelElement;
import com.rallytac.engageandroid.legba.data.dto.Mission;

import java.lang.reflect.Type;
import java.util.List;

public class MissionDeserializer implements JsonDeserializer<Mission> {

    @Override
    public Mission deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        String id = jsonObject.get("id").getAsString();
        String name = jsonObject.get("name").getAsString();
        JsonObject rp = jsonObject.get("rallypoint").getAsJsonObject();

        JsonElement groups = jsonObject.get("groups");
        Type listType = new TypeToken<List<Channel>>() { }.getType();
        List<Channel> channels = new Gson().fromJson(groups, listType);

        return new Mission(id, name, channels, rp.get("address").getAsString(), rp.get("port").getAsInt());
    }
}