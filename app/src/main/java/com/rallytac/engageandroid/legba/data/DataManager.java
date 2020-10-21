package com.rallytac.engageandroid.legba.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rallytac.engageandroid.ActiveConfiguration;
import com.rallytac.engageandroid.Constants;
import com.rallytac.engageandroid.DatabaseGroup;
import com.rallytac.engageandroid.DatabaseMission;
import com.rallytac.engageandroid.Globals;
import com.rallytac.engageandroid.MissionDatabase;
import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.Utils;
import com.rallytac.engageandroid.legba.data.dto.Mission;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import okio.BufferedSource;
import okio.Okio;
import timber.log.Timber;

public class DataManager {

    private final static String MISSIONS_PATH = "mock-data/missions.json";
    private Context context;
    MissionDatabase db;

    public DataManager(Context context) {
        this.context = context;
        db = MissionDatabase.load(Globals.getSharedPreferences(), Constants.MISSION_DATABASE_NAME);
    }

    public List<Mission> getMissions() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(MISSIONS_PATH);
        BufferedSource source = Okio.buffer(Okio.source(inputStream));
        try {
            String jsonMissions = source.readUtf8();
            Type listType = new TypeToken<List<Mission>>() {
            }.getType();
            List missions = new GsonBuilder()
                    .create()
                    .fromJson(jsonMissions, listType);
            loadMissionsOnEgageEngine(missions);
            return missions;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private void loadMissionsOnEgageEngine(List<Mission> missions) {
        Timber.i("Missions before->");
        //deleteEveryMissionOnEngageEngine();
        //db.deleteMissionById("78fdf4c8-0db0-c2a0-5ead-35b8e4acea0e");
        db._missions.forEach(System.out::println);

        missions.forEach(mission -> {
            DatabaseMission newMission = new DatabaseMission();
            newMission._id = Utils.generateMissionId();
            newMission._name = mission.name;
            newMission._rpAddress = context.getString(R.string.default_rallypoint);
            newMission._rpPort = Utils.intOpt(context.getString(R.string.default_rallypoint_port), Constants.DEF_RP_PORT);
            newMission._mcId = Utils.generateGroupId();
            newMission._mcCryptoPassword = Utils.generateCryptoPassword();
            newMission._groups = mission
                    .channels
                    .stream()
                    .map(channel -> {
                        DatabaseGroup group = new DatabaseGroup(channel.name);
                        group._id = channel.id + "";
                        group._txFramingMs = Constants.DEFAULT_TX_FRAMING_MS;
                        group._txCodecId = Constants.DEFAULT_ENCODER;
                        group._maxTxSecs = Constants.DEFAULT_TX_SECS;
                        group._txAddress = "239.42.43.1";
                        group._txPort = 49000;
                        group._txAddress = "239.42.43.1";
                        group._rxPort = 49000;

                        return group;
                    }).collect(Collectors.toCollection(ArrayList::new));

            db._missions.add(newMission);

            updateDB();
        });

        Timber.i("Missions after ->");

        db._missions.forEach(System.out::println);
    }

    private void deleteEveryMissionOnEngageEngine() {
        for (Iterator i = db._missions.iterator(); i.hasNext(); ) {
            i.next();
            i.remove();
        }
    }

    private void updateDB() {
        SharedPreferences sharedPreferences = Globals.getSharedPreferences();
        //sharedPreferences.getAll().entrySet().forEach(keySet -> System.out.println(keySet.getKey() + " - " + keySet.getValue()));
        db.save(sharedPreferences, Constants.MISSION_DATABASE_NAME);
    }
}