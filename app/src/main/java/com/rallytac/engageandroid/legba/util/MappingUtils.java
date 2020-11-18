package com.rallytac.engageandroid.legba.util;

import android.provider.ContactsContract;

import com.rallytac.engageandroid.DatabaseGroup;
import com.rallytac.engageandroid.DatabaseMission;
import com.rallytac.engageandroid.legba.data.dto.Channel;
import com.rallytac.engageandroid.legba.data.dto.Mission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import timber.log.Timber;

import static java.util.stream.Collectors.toCollection;

public class MappingUtils {

    public static DatabaseMission mapMissionTo_Mission(Mission mission) {
        if (mission == null) {
            return null;
        }

        DatabaseMission _mission = new DatabaseMission();

        _mission._id = mission.getId();
        _mission._name = mission.getName();
        _mission._rpAddress = mission.getRpAddress();
        _mission._rpPort = mission.getRpPort();

        _mission._groups = mission.getChannels().stream()
                .map(channel -> {
                    DatabaseGroup newGroup = new DatabaseGroup();

                    newGroup._id = channel.getId();
                    newGroup._name = channel.getName();
                    newGroup._type = channel.getEngageType() == Channel.EngageType.AUDIO ? 1 : 2;
                    newGroup._txFramingMs = channel.getTxFramingMs();
                    newGroup._txCodecId = channel.getTxCodecId();
                    newGroup._maxTxSecs = channel.getMaxTxSecs();
                    newGroup._txAddress = channel.getTxAddress();
                    newGroup._txPort = channel.getTxPort();
                    newGroup._rxAddress = channel.getRxAddress();
                    newGroup._rxPort = channel.getRxPort();
                    newGroup._image = channel.getImage();

                    return newGroup;
                }).collect(toCollection(ArrayList::new));

        return _mission;
    }

    public static Mission map_missionToMission(DatabaseMission _mission) {
        Timber.i("Mission %s", _mission);

        List<Channel> newGroups = _mission._groups.stream().map(_group -> {
            return new Channel(_group._id, _mission._id,
                    _group._name, _group._image,
                    Channel.ChannelType.PRIMARY, _group._txFramingMs,
                    _group._txCodecId, _group._maxTxSecs,
                    _group._txAddress, _group._txPort,
                    _group._rxAddress, _group._rxPort,
                    Channel.EngageType.AUDIO, Collections.emptyList());
        }).collect(Collectors.toList());

        Mission newMission = new Mission(_mission._id, _mission._name, newGroups, _mission._rpAddress, _mission._rpPort);
        newMission.setRpAddress(_mission._rpAddress);
        newMission.setRpPort(_mission._rpPort);

        Timber.i("Mission %s", newMission);
        return newMission;
    }
}
