package com.rallytac.engageandroid.legba.viewmodel;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.ViewModel;

import com.google.gson.GsonBuilder;
import com.rallytac.engage.engine.Engine;
import com.rallytac.engageandroid.DatabaseGroup;
import com.rallytac.engageandroid.DatabaseMission;
import com.rallytac.engageandroid.EngageApplication;
import com.rallytac.engageandroid.Utils;
import com.rallytac.engageandroid.legba.data.DataManager;
import com.rallytac.engageandroid.legba.data.dto.DaoSession;
import com.rallytac.engageandroid.legba.data.dto.Mission;
import com.rallytac.engageandroid.legba.data.dto.MissionDao;
import com.rallytac.engageandroid.legba.util.MappingUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import timber.log.Timber;

public class MissionsListViewModel extends ViewModel {

    private DaoSession daoSession;
    private MissionDao missionDao;

    public MissionsListViewModel(EngageApplication app) {
        daoSession = app.getDaoSession();
        missionDao = daoSession.getMissionDao();
    }

    public List<Mission> getMissions() {
        List<Mission> missions = missionDao.loadAll();

        //Sets OVERLORD missions as always present at first position
        String defaultMissionName = "OVERLORD";

        Mission missionFromJson = DataManager
                .getInstance()
                .getMissions()
                .get(0);

        Optional<Mission> overlord = missions
                .stream()
                .filter(mission -> mission.getName().equalsIgnoreCase(defaultMissionName))
                .findFirst();

        if (overlord.isPresent()) {
            int i = missions.indexOf(overlord.get());
            if (i != 0) {
                Collections.swap(missions, i, 0);
            }
        } else {
            missionFromJson.__setDaoSession(daoSession);
            missionFromJson.insertOrReplace();
            missions.add(0, missionFromJson);
        }

        Timber.i("Number of missions %s", missions.size());
        //-----------------------------------------

        return missions;
    }

    public void saveNewMission(Uri fileUri, Context context) {
        String jsonMission = Utils.readTextFile(context, fileUri);
        Mission importedMission = new GsonBuilder().create().fromJson(jsonMission, Mission.class);
        importedMission.getChannels().forEach(channel -> channel.setMissionId(importedMission.getId()));
        importedMission.__setDaoSession(daoSession);
        importedMission.insertOrReplace();
    }

    public void deleteMission(Mission currentMission) {
        currentMission.__setDaoSession(daoSession);
        currentMission.delete();
    }
}