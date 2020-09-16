package com.upstart13.legba.data;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import okio.BufferedSource;
import okio.Okio;

public class DataManager {

    private final static String LOG_TAG = "DataManager";

    public String readJson() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("mock-data/missions.json");
        BufferedSource source = Okio.buffer(Okio.source(inputStream));
        try {
            String s = source.readUtf8();
            Log.i(LOG_TAG, s);
            return s;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
