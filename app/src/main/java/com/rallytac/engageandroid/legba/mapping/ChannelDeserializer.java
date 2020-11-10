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
        JsonElement engageTypeJsonElement = jsonObject.get("engageType");
        JsonElement txFramingMsJsonElement = jsonObject.get("txFramingMs");
        JsonElement txCodecIdJsonElement = jsonObject.get("txCodecId");
        JsonElement maxTxSecsJsonElement = jsonObject.get("maxTxSecs");
        JsonElement txAddressJsonElement = jsonObject.get("txAddress");
        JsonElement txPortJsonElement = jsonObject.get("txPort");
        JsonElement rxAddressJsonElement = jsonObject.get("rxAddress");
        JsonElement rxPortJsonElement = jsonObject.get("rxPort");

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
                txFramingMsJsonElement != null ? txFramingMsJsonElement.getAsInt() : 30,
                txCodecIdJsonElement != null ? txCodecIdJsonElement.getAsInt() : 25,
                maxTxSecsJsonElement != null ? maxTxSecsJsonElement.getAsInt() : 120,
                txAddressJsonElement != null ? txAddressJsonElement.getAsString() : "239.42.43.1",
                txPortJsonElement != null ? txPortJsonElement.getAsInt() : 49000,
                rxAddressJsonElement != null ? rxAddressJsonElement.getAsString() : "239.42.43.1",
                rxPortJsonElement != null ? rxPortJsonElement.getAsInt() : 49000,
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
