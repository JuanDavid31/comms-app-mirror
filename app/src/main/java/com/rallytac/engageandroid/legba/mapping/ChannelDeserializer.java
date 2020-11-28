package com.rallytac.engageandroid.legba.mapping;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.rallytac.engageandroid.legba.data.dto.Channel;

import java.lang.reflect.Type;
import java.util.List;

import static com.rallytac.engageandroid.legba.data.dto.Channel.*;
import static com.rallytac.engageandroid.legba.data.dto.Channel.EngageType.AUDIO;
import static com.rallytac.engageandroid.legba.data.dto.Channel.EngageType.PRESENCE;

public class ChannelDeserializer implements JsonDeserializer<Channel> {

    private final static String ID = "id";
    private final static String MISSION_ID = "missionId";
    private final static String NAME = "name";
    private final static String TYPE = "type";
    private final static String IMAGE = "image";
    //private final static String type = "type";
    private final static String TX_AUDIO = "txAudio";
    private final static String FRAMING_MS = "framingMs";
    private final static String ENCODER = "encoder";
    private final static String MAX_TX_SECS = "maxTxSecs";

    private final static String RX = "rx";
    private final static String TX = "tx";


    @Override
    public Channel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();

        JsonElement idJsonElement = root.get(ID);
        JsonElement missionIdJsonElement = root.get(MISSION_ID);
        JsonElement nameJsonElement = root.get(NAME);
        JsonElement typeJsonElement = root.get(TYPE);
        JsonElement imageJsonElement = root.get(IMAGE);
        JsonElement engageTypeJsonElement = root.get(TYPE);

        if (idJsonElement == null || idJsonElement.isJsonPrimitive()) {
            throw new JsonParseException("id in group property is not valid ");
        }
        String id = idJsonElement.getAsString();

        if (missionIdJsonElement == null || missionIdJsonElement.isJsonPrimitive()) {
            throw new JsonParseException("missionId in group is not a valid property");
        }
        if (nameJsonElement == null || nameJsonElement.isJsonPrimitive()) {
            throw new JsonParseException("name in group is not a valid property");
        }
        /*if (typeJsonElement == null || typeJsonElement.isJsonPrimitive()) {
            throw new JsonParseException("type in group is not a valid property");
        }*/
        if (imageJsonElement == null || imageJsonElement.isJsonPrimitive()) {
            throw new JsonParseException("image in group is not a valid property");
        }
        if (engageTypeJsonElement == null || !engageTypeJsonElement.isJsonPrimitive()) {
            throw new JsonParseException("engageType in group is not a valid property");
        }

        JsonElement txAudioJsonElement = root.get(TX_AUDIO);

        int framingMs = 60;
        int encoder = 25;
        int maxTxSecs = 30;
        if (txAudioJsonElement == null && !txAudioJsonElement.isJsonObject()){
            throw new JsonParseException("txAudio in group is not a valid property");
        } else {
            JsonObject txAudioJsonObject =  txAudioJsonElement.getAsJsonObject();

            JsonElement framingMsJsonElement = txAudioJsonObject.get(FRAMING_MS);
            if(framingMsJsonElement == null || !framingMsJsonElement.isJsonPrimitive()){
                throw new JsonParseException("framingMs in group is not valid");
            }
            JsonElement encoderJsonElement = txAudioJsonObject.get(ENCODER);
            if(encoderJsonElement == null || !encoderJsonElement.isJsonPrimitive()){
                throw new JsonParseException("encoder in group is not valid");
            }
            JsonElement maxTxSecsJsonElement = txAudioJsonObject.get(MAX_TX_SECS);
            if(maxTxSecsJsonElement == null || !maxTxSecsJsonElement.isJsonPrimitive()){
                throw new JsonParseException("maxTxSecs in group is not valid");
            }
        }


        JsonObject rxJsonObject = root.get(RX).getAsJsonObject();
        JsonObject txJsonObject = root.get(TX).getAsJsonObject();

        return new Channel( //TODO: Create a proper constructor without innecesary boilerplate
                id,
                missionIdJsonElement.getAsString(),
                nameJsonElement.getAsString(),
                imageJsonElement.getAsString(),
                getChannelType(typeJsonElement.getAsString()),
                framingMs,
                encoder,
                maxTxSecs,
                txJsonObject != null ? txJsonObject.get("address").getAsString() : "239.42.43.1",
                txJsonObject != null ? txJsonObject.get("port").getAsInt() : 49000,
                rxJsonObject != null ? rxJsonObject.get("address").getAsString() : "239.42.43.1",
                rxJsonObject != null ? rxJsonObject.get("port").getAsInt() : 49000,
                getEngageType(engageTypeJsonElement != null ? engageTypeJsonElement.getAsInt() : 1)
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
