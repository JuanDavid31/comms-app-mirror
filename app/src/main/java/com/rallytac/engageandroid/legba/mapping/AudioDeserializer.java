package com.rallytac.engageandroid.legba.mapping;

import android.net.Uri;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.rallytac.engage.engine.Engine;
import com.rallytac.engageandroid.legba.data.dto.Audio;
import com.rallytac.engageandroid.legba.data.dto.Channel;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

public class AudioDeserializer implements JsonDeserializer<Audio> {


    @Override
    public Audio deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject audio = json.getAsJsonObject();

        Audio.AudioType audioType = audio.get(Engine.JsonFields.TimelineEvent.direction).getAsInt() == 1 ? Audio.AudioType.Tx : Audio.AudioType.Rx;

        long startedTimestamp = audio.get(Engine.JsonFields.TimelineEvent.started).getAsLong();
        LocalTime startedTime = new Timestamp(startedTimestamp).toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

        String sender = audio.get(Engine.JsonFields.TimelineEvent.alias).getAsString();

        String uri = audio.get(Engine.JsonFields.TimelineEvent.uri).getAsString();
        Uri audioUri = Uri.parse(uri);

        long ms = audio.get("audio").getAsJsonObject().get(Engine.JsonFields.TimelineEvent.Audio.ms).getAsLong();
        int durationInSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(ms);

        return new Audio(audioType, startedTime, sender, audioUri, durationInSeconds);
    }
}
