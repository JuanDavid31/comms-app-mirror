package com.rallytac.engageandroid.legba.data;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.upstart13.legba.data.dto.Mission;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okio.BufferedSource;
import okio.Okio;

public class DataManager {

    private final static String MISSIONS_PATH = "mock-data/missions.json";
    private final static String LOG_TAG = "DataManager";

    public List<Mission> getMissions() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(MISSIONS_PATH);
        BufferedSource source = Okio.buffer(Okio.source(inputStream));
        try {
            String jsonMissions = source.readUtf8();
            Type listType = new TypeToken<List<Mission>>() {}.getType();

            return new GsonBuilder()
                    .create()
                    .fromJson(jsonMissions, listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}