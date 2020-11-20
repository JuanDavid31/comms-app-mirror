package com.rallytac.engageandroid.legba.util;

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
        _mission._useRp = mission.useRp();
        _mission._rpAddress = mission.getRpAddress();
        _mission._rpPort = mission.getRpPort();
        _mission._multicastFailoverPolicy = new Mission.MulticastTypeConverter().convertToDatabaseValue(mission.getMulticastType());

        mission.getChannels()
                .stream()
                .filter(channel -> channel.getEngageType() == Channel.EngageType.PRESENCE)
                .findFirst()
                .ifPresent(channel -> {
                    _mission._mcAddress = channel.getTxAddress();
                    _mission._mcPort = channel.getTxPort();
                });

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

        Mission.MulticastType multicastType
                = new Mission.MulticastTypeConverter().convertToEntityProperty(_mission._multicastFailoverPolicy);

        Mission newMission = new Mission(_mission._id, _mission._name,
                newGroups, _mission._useRp,
                _mission._rpAddress, _mission._rpPort,
                multicastType);

        newMission.getChannels()
                .stream()
                .filter(channel -> channel.getEngageType() == Channel.EngageType.PRESENCE)
                .findFirst()
                .ifPresent(channel -> {
                    channel.setTxAddress(_mission._mcAddress);
                    channel.setTxPort(_mission._mcPort);
                });

        Timber.i("Mission %s", newMission);
        return newMission;
    }
}
