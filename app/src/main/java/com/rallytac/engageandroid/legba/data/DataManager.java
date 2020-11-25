package com.rallytac.engageandroid.legba.data;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rallytac.engageandroid.Constants;
import com.rallytac.engageandroid.DatabaseGroup;
import com.rallytac.engageandroid.DatabaseMission;
import com.rallytac.engageandroid.Globals;
import com.rallytac.engageandroid.MissionDatabase;
import com.rallytac.engageandroid.Utils;
import com.rallytac.engageandroid.legba.data.dto.Channel;
import com.rallytac.engageandroid.legba.data.dto.Mission;
import com.rallytac.engageandroid.legba.data.engagedto.EngageClasses;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import okio.BufferedSource;
import okio.Okio;
import timber.log.Timber;

import static com.rallytac.engageandroid.legba.data.engagedto.EngageClasses.*;

public class DataManager {

    private final static String MISSIONS_PATH = "mock-data/missions.json";
    private final MissionDatabase db;
    private static DataManager instance;

    private Mission activeMission = null;

    private DataManager() {
        db = MissionDatabase.load(Globals.getSharedPreferences(), Constants.MISSION_DATABASE_NAME);
    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public List<Mission> getMissions() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(MISSIONS_PATH);
        BufferedSource source = Okio.buffer(Okio.source(inputStream));
        try {
            String jsonMissions = source.readUtf8();
            List<Mission> missions = missionsStringToMissionsList(jsonMissions);
            loadMissionsOnEgageEngine(missions);
            return missions;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<Mission> missionsStringToMissionsList(String jsonMissions) {
        Type listType = new TypeToken<List<Mission>>() {
        }.getType();
        return new GsonBuilder()
                .create()
                .fromJson(jsonMissions, listType);
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
            newMission._id = mission.getId();
            newMission._name = mission.getName();
            newMission._rpAddress = mission.getRpAddress(); //context.getString(R.string.default_rallypoint);
            newMission._rpPort = mission.getRpPort(); //Utils.intOpt(context.getString(R.string.default_rallypoint_port), Constants.DEF_RP_PORT);
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
                .getChannels()
                .stream()
                .map(channel -> {
                    DatabaseGroup group = new DatabaseGroup(channel.getName());
                    group._id = channel.getId();
                    group._txFramingMs = channel.getTxFramingMs(); //Constants.DEFAULT_TX_FRAMING_MS;
                    group._txCodecId = channel.getTxCodecId(); //Constants.DEFAULT_ENCODER;
                    group._maxTxSecs = channel.getMaxTxSecs(); // Constants.DEFAULT_TX_SECS;
                    group._txAddress = channel.getTxAddress(); // "239.42.43.1";
                    group._txPort = channel.getTxPort(); //49000;
                    group._rxAddress = channel.getRxAddress(); // "239.42.43.1";
                    group._rxPort = channel.getRxPort(); //49000;

                    return group;
                }).collect(Collectors.toCollection(ArrayList::new));
    }

    public void updateDB() {
        SharedPreferences sharedPreferences = Globals.getSharedPreferences();
        db.save(sharedPreferences, Constants.MISSION_DATABASE_NAME);
    }

    public void switchToMissionOnEngageEngine(Mission mission) {
        mission.getChannels()
                .forEach(channel -> {
                    AddressAndPort rx = new AddressAndPort(channel.getRxAddress(), channel.getRxPort());
                    AddressAndPort tx = new AddressAndPort(channel.getTxAddress(), channel.getTxPort());
                    TxAudio txAudio = new TxAudio(channel.getTxCodecId(), channel.getTxFramingMs(), channel.getMaxTxSecs());
                    TxData txData = new TxData(channel.getId(),
                            channel.getName(),
                            channel.getEngageType() == Channel.EngageType.AUDIO ? 1 : 2,
                            rx,
                            tx,
                            txAudio);
                    String initialJsonTxData = new Gson().toJson(txData);

                    Timber.i("txData -> %s", initialJsonTxData);

                    String finalTxData = Globals.getEngageApplication().buildFinalGroupJsonConfigurationLegba(initialJsonTxData, mission);
                    Globals.getEngageApplication().getEngine().engageCreateGroup(finalTxData);
                });
        activeMission = mission;
    }

    public void leaveMissionActiveMission(){
        if (activeMission != null){
            activeMission.getChannels()
                    .stream()
                    .map(Channel::getId)
                    .forEach(channelId -> Globals.getEngageApplication().getEngine().engageLeaveGroup(channelId));
            activeMission = null;
        }
    }

    public void toggleMute(String groupId, boolean isSpeakerOn) {
        if (isSpeakerOn) {
            Globals.getEngageApplication().getEngine().engageUnmuteGroupRx(groupId);
        } else {
            Globals.getEngageApplication().getEngine().engageMuteGroupRx(groupId);
        }
    }

    public void startTx(boolean isSos, String... groupIds) {

        Globals.getEngageApplication().startTxLegba(0, isSos ? 1 : 0, groupIds);
    }

    public void endTx(String... groupIds) {
        Globals.getEngageApplication().endTxLega(groupIds);
    }

    public void updatePresenceDescriptor() {
        EngageClasses.PresenceDescriptor presenceDescriptor =
                new EngageClasses.PresenceDescriptor("{USER-A}",
                        Globals.getSharedPreferences().getString("user_id", ""),
                        Globals.getSharedPreferences().getString("user_displayName", ""));

        String json = new Gson().toJson(presenceDescriptor);

        Timber.i("Updating presence descriptor %s", Globals.MISSION_CONTROL_ID);
        Globals.getEngageApplication().getEngine().engageUpdatePresenceDescriptor(Globals.MISSION_CONTROL_ID, json, 1);
    }

    public Channel generateMissionControlChannel(String missionId) {
        return new Channel(Globals.MISSION_CONTROL_ID, missionId,
                "MISSION CONTROL", "",
                Channel.ChannelType.PRIMARY, 30,
                25, 120,
                "239.42.43.1", 49000,
                "239.42.43.1", 49000,
                Channel.EngageType.PRESENCE);
    }
}