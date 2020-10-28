package com.rallytac.engageandroid.legba.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rallytac.engage.engine.Engine;
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
import java.util.List;
import java.util.stream.Collectors;

import okio.BufferedSource;
import okio.Okio;
import timber.log.Timber;

public class DataManager {

    private final static String MISSIONS_PATH = "mock-data/missions.json";
    private Context context;
    private MissionDatabase db;
    private static DataManager instance;

    private DataManager(Context context) {
        this.context = context;
        db = MissionDatabase.load(Globals.getSharedPreferences(), Constants.MISSION_DATABASE_NAME);
    }

    public static DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context);
        }
        return instance;
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
        db._missions.forEach(System.out::println);

        if (db._missions.size() == 2) {
            Timber.i("There are already 2 missions");
            return;
        }

        missions.forEach(mission -> {
            DatabaseMission newMission = new DatabaseMission();
            newMission._id = mission.id;
            newMission._name = mission.name;
            newMission._rpAddress = context.getString(R.string.default_rallypoint);
            newMission._rpPort = Utils.intOpt(context.getString(R.string.default_rallypoint_port), Constants.DEF_RP_PORT);
            newMission._mcId = Utils.generateGroupId();
            newMission._mcCryptoPassword = Utils.generateCryptoPassword();
            newMission._groups = getGroupsByMission(mission);

            db._missions.add(newMission);

            updateDB();
        });

        Timber.i("Missions after ->");
        db._missions.forEach(System.out::println);
    }

    private ArrayList<DatabaseGroup> getGroupsByMission(Mission mission) {
        return mission
                .channels
                .stream()
                .map(channel -> {
                    DatabaseGroup group = new DatabaseGroup(channel.name);
                    group._id = channel.id;
                    group._txFramingMs = Constants.DEFAULT_TX_FRAMING_MS;
                    group._txCodecId = Constants.DEFAULT_ENCODER;
                    group._maxTxSecs = Constants.DEFAULT_TX_SECS;
                    group._txAddress = "239.42.43.1";
                    group._txPort = 49000;
                    group._txAddress = "239.42.43.1";
                    group._rxPort = 49000;

                    return group;
                }).collect(Collectors.toCollection(ArrayList::new));
    }

    public void updateDB() {
        SharedPreferences sharedPreferences = Globals.getSharedPreferences();
        db.save(sharedPreferences, Constants.MISSION_DATABASE_NAME);
    }

    public void switchToMissionOnEngageEngine(Mission mission) {
        db._missions
                .stream()
                .filter(m -> m._id.equals(mission.id))
                .findAny()
                .ifPresent(m -> {
                    Globals.getEngageApplication().switchToMission(mission.id);
                    Globals.getEngageApplication().getActiveConfiguration().set_missionId(mission.id);
                    Timber.i("MissionId updated to %s ", mission);
                });
        updateDB();

        mission.channels
                .forEach(channel -> {
                    String txData = new Gson().toJson(new TxData(channel.id, channel.name));
                    Timber.i("txData -> %s", txData);
                    String realTxData = Globals.getEngageApplication().buildFinalGroupJsonConfiguration(txData);
                    Globals.getEngageApplication().getEngine().engageCreateGroup(realTxData);
                });

        mission.channels.forEach(channel -> Globals.getEngageApplication().getEngine().engageJoinGroup(channel.id));
        Globals.getEngageApplication().updateActiveConfiguration();
    }

    public void toggleMute(String groupId, boolean isSpeakerOn) {
        if(isSpeakerOn){
            Globals.getEngageApplication().getEngine().engageUnmuteGroupRx(groupId);
        }else{
            Globals.getEngageApplication().getEngine().engageMuteGroupRx(groupId);
        }
    }

    class TxData{
        String id;
        String name;
        int type = 1;
        RxTx rx = new RxTx("");
        RxTx tx = new RxTx("239.42.43.1");
        TxAudio txAudio = new TxAudio();

        public TxData(String id, String name) {
            this.id = id;
            this.name = name;
        }

        class RxTx {
            String address = "";
            int port = 49000;

            public RxTx(String address) {
                this.address = address;
            }
        }

        class TxAudio {//{"encoder":25,"framingMs":60,"noHdrExt":false,"fdx":false,"maxTxSecs":120}
            int encoder = 25;
            int framingMs = 60;
            boolean noHdrExt = false;
            boolean fdx = false;
            int maxTxSecs = 120;
        }
    }

    private void initEngine() {
        Engine engine = Globals.getEngageApplication().getEngine();
        //engine.engageOpenCertStore() // ?
        //engine.engageInitialize() // ?
        engine.engageStart();
    }

    public void unmuteGroup(String groupId) {
        Globals.getEngageApplication().getEngine().engageUnmuteGroupRx(groupId);
    }

    public void muteGroup(String groupId) {
        Globals.getEngageApplication().getEngine().engageMuteGroupRx(groupId);
    }

    public void startTx(String... groupIds) {
        Globals.getEngageApplication().startTxLegba(0, 0, groupIds);
    }

    public void endTx(String... groupIds) {
        Globals.getEngageApplication().endTxLega(groupIds);
    }
}