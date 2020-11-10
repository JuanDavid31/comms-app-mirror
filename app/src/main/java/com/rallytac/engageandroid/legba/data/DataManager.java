package com.rallytac.engageandroid.legba.data;

import android.content.Context;
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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import okio.BufferedSource;
import okio.Okio;
import timber.log.Timber;

import static com.rallytac.engageandroid.legba.data.engagedto.EngageClasses.*;

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
            List<Mission> missions = new GsonBuilder()
                    .create()
                    .fromJson(jsonMissions, listType);
            loadMissionsOnEgageEngine(missions);
            missions.forEach(Mission::removeMissionControlChannelFromList);
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
        db._missions
                .stream()
                .filter(m -> m._id.equals(mission.getId()))
                .findAny()
                .ifPresent(m -> {
                    Globals.getEngageApplication().switchToMission(mission.getId());
                    Globals.getEngageApplication().getActiveConfiguration().set_missionId(mission.getId());
                    Timber.i("MissionId updated to %s ", mission);
                });
        updateDB();

        mission.addMissionControlChannelToList();

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

                    String finalTxData = Globals.getEngageApplication().buildFinalGroupJsonConfiguration(initialJsonTxData);
                    Globals.getEngageApplication().getEngine().engageCreateGroup(finalTxData);
                });

        Globals.getEngageApplication().updateActiveConfiguration();

        mission.removeMissionControlChannelFromList();
    }

    public void toggleMute(String groupId, boolean isSpeakerOn) {
        if (isSpeakerOn) {
            Globals.getEngageApplication().getEngine().engageUnmuteGroupRx(groupId);
        } else {
            Globals.getEngageApplication().getEngine().engageMuteGroupRx(groupId);
        }
    }

    public void startTx(String... groupIds) {
        Globals.getEngageApplication().startTxLegba(0, 0, groupIds);
    }

    public void endTx(String... groupIds) {
        Globals.getEngageApplication().endTxLega(groupIds);
    }
}