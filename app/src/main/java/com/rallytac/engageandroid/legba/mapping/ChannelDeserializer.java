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
import static com.rallytac.engageandroid.legba.data.dto.Channel.EngageType.AUDIO;
import static com.rallytac.engageandroid.legba.data.dto.Channel.EngageType.PRESENCE;
import static com.rallytac.engageandroid.legba.data.engagedto.EngageClasses.TxData.PRESENCE_TYPE;

public class ChannelDeserializer implements JsonDeserializer<Channel> {

    @Override
    public Channel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        JsonElement idJsonElement = jsonObject.get("id");
        JsonElement missionIdJsonElement = jsonObject.get("missionId");
        JsonElement nameJsonElement = jsonObject.get("name");
        JsonElement typeJsonElement = jsonObject.get("type");
        JsonElement imageJsonElement = jsonObject.get("image");
        JsonElement engageTypeJsonElement = jsonObject.get("type");

        JsonElement txAudioJsonElement = jsonObject.get("txAudio");

        JsonObject txAudioJsonObject = txAudioJsonElement != null ? txAudioJsonElement.getAsJsonObject() : null;


        JsonObject rxJsonObject = jsonObject.get("rx").getAsJsonObject();
        JsonObject txJsonObject = jsonObject.get("tx").getAsJsonObject();

        JsonElement subChannelsAndMembers = jsonObject.get("subChannelsAndMembers");
        Type listType = new TypeToken<List<ChannelElement>>() {
        }.getType();
        List<ChannelElement> channelElements = new Gson().fromJson(subChannelsAndMembers, listType);

        return new Channel( //TODO: Create a proper constructor without innecesary boilerplate
                idJsonElement != null ? idJsonElement.getAsString() : "",
                missionIdJsonElement != null ? missionIdJsonElement.getAsString() : "",
                nameJsonElement != null ? nameJsonElement.getAsString() : "",
                imageJsonElement != null ? imageJsonElement.getAsString() : "",
                getChannelType(typeJsonElement != null ? typeJsonElement.getAsString() : ""),
                txAudioJsonObject != null ? txAudioJsonObject.get("framingMs").getAsInt() : 60,
                txAudioJsonObject != null ? txAudioJsonObject.get("encoder").getAsInt() : 25,
                txAudioJsonObject != null ? txAudioJsonObject.get("maxTxSecs").getAsInt() : 30,
                txJsonObject != null ? txJsonObject.get("address").getAsString() : "239.42.43.1",
                txJsonObject != null ? txJsonObject.get("port").getAsInt() : 49000,
                rxJsonObject != null ? rxJsonObject.get("address").getAsString() : "239.42.43.1",
                rxJsonObject != null ? rxJsonObject.get("port").getAsInt() : 49000,
                getEngageType(engageTypeJsonElement != null ? engageTypeJsonElement.getAsInt() : 1),
                channelElements != null ? channelElements : Collections.emptyList()
        );
    }

    private ChannelType getChannelType(String stringType) {
        final String PRIMARY_TYPE = "primary";
        final String PRIORITARY_TYPE = "priority";
        final String RADIO_TYPE = "radio";

        switch (stringType) {
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

    private EngageType getEngageType(int engageType) {
        if (engageType == 1) {
            return AUDIO;
        } else {
            return PRESENCE;
        }
    }
}
