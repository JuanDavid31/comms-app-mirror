package com.rallytac.engageandroid.legba.data;

import android.content.Context;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rallytac.engageandroid.Constants;
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
import java.util.List;

import okio.BufferedSource;
import okio.Okio;
import timber.log.Timber;

public class DataManager {

    private final static String MISSIONS_PATH = "mock-data/missions.json";
    private Context context;
    MissionDatabase db;

    public DataManager(Context context){
        this.context = context;
        db = MissionDatabase.load(Globals.getSharedPreferences(), Constants.MISSION_DATABASE_NAME);
    }

    public List<Mission> getMissions() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(MISSIONS_PATH);
        BufferedSource source = Okio.buffer(Okio.source(inputStream));
        try {
            String jsonMissions = source.readUtf8();
            Type listType = new TypeToken<List<Mission>>() {}.getType();
            List missions = new GsonBuilder()
                    .create()
                    .fromJson(jsonMissions, listType);
            loadMissionsOnEgageEngine(missions);
            return missions ;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private void loadMissionsOnEgageEngine(List<Mission> missions) {
        Timber.i("Missions before->");
        Log.i("algo", "jajajeje");
        //deleteEveryMissionOnEngageEngine();
        db._missions.forEach(System.out::println);

        missions.forEach(mission -> {

            DatabaseMission newMission = new DatabaseMission();
            newMission._id = Utils.generateMissionId();
            newMission._rpAddress = context.getString(R.string.default_rallypoint);
            newMission._rpPort = Utils.intOpt(context.getString(R.string.default_rallypoint_port), Constants.DEF_RP_PORT);
            newMission._mcId = Utils.generateGroupId();
            newMission._mcCryptoPassword = Utils.generateCryptoPassword();

            /*String json = "";
            String generatedMission = Globals.getEngageApplication().getEngine().engageGenerateMission("1234", mission.channels.size(), "demo.rallytac.com", mission.name);
            json = Globals.getEngageApplication()
                    .applyFlavorSpecificGeneratedMissionModifications(generatedMission);
            Timber.i("Mission Unique %s", json);
            ActiveConfiguration.installMissionJson(context, json, true);*/
        });

        Timber.i("Missions after ->");

        db._missions.forEach(System.out::println);
    }

    private void deleteEveryMissionOnEngageEngine() {
        db._missions.forEach(mission -> db.deleteMissionById(mission._id));
    }
}