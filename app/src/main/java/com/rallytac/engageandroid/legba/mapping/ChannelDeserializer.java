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

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import static com.rallytac.engageandroid.legba.data.dto.Channel.*;

public class ChannelDeserializer implements JsonDeserializer<Channel> {

    @Override
    public Channel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        JsonElement idJsonElement = jsonObject.get("id");
        JsonElement nameJsonElement = jsonObject.get("name");
        JsonElement typeJsonElement = jsonObject.get("type");
        JsonElement imageJsonElement = jsonObject.get("image");
        JsonElement statusJsonElement = jsonObject.get("status");

        JsonElement subChannelsAndMembers = jsonObject.get("subChannelsAndMembers");
        Type listType = new TypeToken<List<ChannelElement>>() {}.getType();
        List<ChannelElement> channelElements = new Gson().fromJson(subChannelsAndMembers, listType);

        return new Channel(
                idJsonElement != null ? idJsonElement.getAsInt() : -1,
                nameJsonElement != null ? nameJsonElement.getAsString() : "",
                getChannelType(typeJsonElement != null ? typeJsonElement.getAsString() : ""),
                imageJsonElement != null ? imageJsonElement.getAsString() : "",
                statusJsonElement != null ? statusJsonElement.getAsBoolean() : false,
                channelElements != null ? channelElements : Collections.emptyList()
        );
    }

    private ChannelType getChannelType(String stringType){
        final String PRIMARY_TYPE = "primary";
        final String PRIORITARY_TYPE = "priority";
        final String RADIO_TYPE = "radio";

        switch (stringType){
            case PRIMARY_TYPE:
                return ChannelType.PRIMARY;
            case PRIORITARY_TYPE:
                return ChannelType.PRIORITY;
            case RADIO_TYPE:
                return ChannelType.RADIO;
            default:
                return ChannelType.RADIO;
        }
    }


}
