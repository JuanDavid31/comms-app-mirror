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
    private final static String FULL_DUPLEX = "fdx";
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

        if (idJsonElement == null || !idJsonElement.getAsJsonPrimitive().isString()) {
            throw new JsonParseException("id in group property is not valid ");
        }
        String id = idJsonElement.getAsString();

        if (missionIdJsonElement == null || !missionIdJsonElement.getAsJsonPrimitive().isString()) {
            throw new JsonParseException("missionId in group is not a valid property");
        }
        String missionId = missionIdJsonElement.getAsString();

        if (nameJsonElement == null || !nameJsonElement.getAsJsonPrimitive().isString()) {
            throw new JsonParseException("name in group is not a valid property");
        }
        String name = nameJsonElement.getAsString();

        if (imageJsonElement == null || !imageJsonElement.getAsJsonPrimitive().isString()) {
            throw new JsonParseException("image in group is not a valid property");
        }
        String image = imageJsonElement.getAsString();

        /*if (typeJsonElement == null || typeJsonElement.isJsonPrimitive()) {
            throw new JsonParseException("type in group is not a valid property");
        }*/


        if (engageTypeJsonElement == null || !engageTypeJsonElement.getAsJsonPrimitive().isNumber()) {
            throw new JsonParseException("engageType in group is not a valid property");
        }

        JsonElement txAudioJsonElement = root.get(TX_AUDIO);

        boolean fullDuplex;
        int framingMs = 60;
        int encoder = 25;
        int maxTxSecs = 30;
        if (txAudioJsonElement == null || !txAudioJsonElement.isJsonObject()){
            throw new JsonParseException("txAudio in group is not a valid property");
        } else {
            JsonObject txAudioJsonObject = txAudioJsonElement.getAsJsonObject();

            JsonElement fullDuplexJsonElement = txAudioJsonObject.get(FULL_DUPLEX);
            if(fullDuplexJsonElement == null || !fullDuplexJsonElement.getAsJsonPrimitive().isBoolean()){
                throw new JsonParseException("fdx in group is not valid");
            }
            fullDuplex = fullDuplexJsonElement.getAsBoolean();

            JsonElement framingMsJsonElement = txAudioJsonObject.get(FRAMING_MS);
            if(framingMsJsonElement == null || !framingMsJsonElement.getAsJsonPrimitive().isNumber()){
                throw new JsonParseException("framingMs in group is not valid");
            }
            framingMs = framingMsJsonElement.getAsInt();

            JsonElement encoderJsonElement = txAudioJsonObject.get(ENCODER);
            if(encoderJsonElement == null || !encoderJsonElement.getAsJsonPrimitive().isNumber()){
                throw new JsonParseException("encoder in group is not valid");
            }
            encoder = encoderJsonElement.getAsInt();

            JsonElement maxTxSecsJsonElement = txAudioJsonObject.get(MAX_TX_SECS);
            if(maxTxSecsJsonElement == null || !maxTxSecsJsonElement.getAsJsonPrimitive().isNumber()){
                throw new JsonParseException("maxTxSecs in group is not valid");
            }
            maxTxSecs = maxTxSecsJsonElement.getAsInt();
        }

        JsonObject rxJsonObject = root.get(RX).getAsJsonObject();
        JsonObject txJsonObject = root.get(TX).getAsJsonObject();

        return new Channel(
                id,
                missionId,
                name,
                image,
                getChannelType(typeJsonElement.getAsString()),
                fullDuplex,
                framingMs,
                encoder,
                maxTxSecs,
                txJsonObject != null ? txJsonObject.get("address").getAsString() : "239.42.43.1", //TODO: Validate
                txJsonObject != null ? txJsonObject.get("port").getAsInt() : 49000, //TODO: Validate
                rxJsonObject != null ? rxJsonObject.get("address").getAsString() : "239.42.43.1", //TODO: Validate
                rxJsonObject != null ? rxJsonObject.get("port").getAsInt() : 49000, //TODO: Validate
                getEngageType(engageTypeJsonElement.getAsInt()) //TODO: Validate
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
