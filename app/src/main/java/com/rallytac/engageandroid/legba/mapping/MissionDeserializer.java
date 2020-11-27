package com.rallytac.engageandroid.legba.mapping;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.rallytac.engageandroid.legba.data.dto.Channel;
import com.rallytac.engageandroid.legba.data.dto.Mission;

import java.lang.reflect.Type;
import java.util.List;

public class MissionDeserializer implements JsonDeserializer<Mission> {

    private final static String MISSION_ID = "id";
    private final static String MISSION_NAME = "name";
    private final static String RALLYPOINT = "rallypoint";
    private final static String RP_USE = "use";
    private final static String RP_ADDRESS = "address";
    private final static String RP_PORT = "port";
    private final static String MULTICAST_POLICY = "multicastFailoverPolicy";

    private final static String GROUPS = "groups";

    @Override
    public Mission deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        String id = jsonObject.get(MISSION_ID).getAsString();
        if (id == null) {
            throw new JsonParseException("No id in mission");
        }

        String name = jsonObject.get(MISSION_NAME).getAsString();
        if (name == null) {
            throw new JsonParseException("No name in mission");
        }

        boolean rpUse = false;
        String rpAddress = "";
        int rpPort = 0;

        JsonElement rpElement = jsonObject.get(RALLYPOINT);
        if (rpElement != null) {
            if (!rpElement.isJsonObject()) {
                throw new JsonParseException("Rallypoint property is invalid");
            }

            JsonObject rp = rpElement.getAsJsonObject();

            JsonElement rpUseElement = rp.get(RP_USE);
            if (!rpUseElement.isJsonPrimitive()) {
                throw new JsonParseException("Use property is not valid");
            }
            rpUse = rpUseElement.getAsBoolean();

            JsonElement rpAddressElement = rp.get(RP_ADDRESS);
            if (rpAddressElement.isJsonPrimitive()) {
                throw new JsonParseException("Address property is not valid");
            }
            rpAddress = rpAddressElement.getAsString();

            JsonElement rpPortElement = rp.get(RP_PORT);
            if (rpPortElement.isJsonPrimitive()) {
                throw new JsonParseException("Port property is not valid");
            }
            rpPort = rpPortElement.getAsInt();
        }

        int multicastFailoverPolicy;

        JsonElement multicastPolicyElement = jsonObject.get(MULTICAST_POLICY);
        if (multicastPolicyElement != null && multicastPolicyElement.isJsonPrimitive()) {
            multicastFailoverPolicy = multicastPolicyElement.getAsInt();
        } else {
            throw new JsonParseException("multicastFailoverPolicy property is not valid");
        }

        Mission.MulticastType multicastType
                = new Mission.MulticastTypeConverter().convertToEntityProperty(multicastFailoverPolicy);

        JsonElement groups = jsonObject.get(GROUPS);
        Type listType = new TypeToken<List<Channel>>() {
        }.getType();

        List<Channel> channels = new Gson().fromJson(groups, listType);

        if (channels.isEmpty()){
            throw new JsonParseException("There are no groups");
        }

        return new Mission(id, name, channels, rpUse, rpAddress, rpPort, multicastType);
    }
}