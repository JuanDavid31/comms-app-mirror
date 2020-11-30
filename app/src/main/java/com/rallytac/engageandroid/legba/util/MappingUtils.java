package com.rallytac.engageandroid.legba.util;

import android.provider.ContactsContract;

import com.rallytac.engage.engine.Engine;
import com.rallytac.engageandroid.DatabaseGroup;
import com.rallytac.engageandroid.DatabaseMission;
import com.rallytac.engageandroid.Utils;
import com.rallytac.engageandroid.legba.data.dto.Channel;
import com.rallytac.engageandroid.legba.data.dto.DaoSession;
import com.rallytac.engageandroid.legba.data.dto.Mission;

import org.json.JSONArray;
import org.json.JSONObject;

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
                    newGroup._fdx = channel.isFullDuplex();
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

    public static Mission map_missionToMission(DatabaseMission _mission, DaoSession daoSession) {
        Timber.i("Mission %s", _mission);

        List<Channel> newGroups = _mission._groups.stream().map(_group -> {
            return new Channel(_group._id, _mission._id,
                    _group._name, _group._image,
                    Channel.ChannelType.PRIMARY, _group._fdx,
                    _group._txFramingMs,
                    _group._txCodecId, _group._maxTxSecs,
                    _group._txAddress, _group._txPort,
                    _group._rxAddress, _group._rxPort,
                    Channel.EngageType.AUDIO);
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

        newMission.__setDaoSession(daoSession);
        return newMission;
    }

    public static String makeTemplate(Mission mission) {

        DatabaseMission _mission = MappingUtils.mapMissionTo_Mission(mission);

        JSONObject jsonMission = new JSONObject();

        try {
            jsonMission.put(Engine.JsonFields.Mission.id, _mission._id);

            if (!Utils.isEmptyString(_mission._name)) {
                jsonMission.put(Engine.JsonFields.Mission.name, _mission._name);
            }

            if (!Utils.isEmptyString(_mission._description)) {
                jsonMission.put(Engine.JsonFields.Mission.description, _mission._description);
            }

            /*if (!Utils.isEmptyString(_missionModPin)) {
                jsonMission.put(Engine.JsonFields.Mission.modPin, _missionModPin);
            }*/

            jsonMission.put("multicastFailoverPolicy", 0);

            if (!Utils.isEmptyString(_mission._rpAddress) && _mission._rpPort > 0) {
                JSONObject rallypoint = new JSONObject();
                rallypoint.put("use", false);
                rallypoint.put(Engine.JsonFields.Rallypoint.Host.address, _mission._rpAddress);
                rallypoint.put(Engine.JsonFields.Rallypoint.Host.port, _mission._rpPort);
                jsonMission.put(Engine.JsonFields.Rallypoint.objectName, rallypoint);
            }

            if (_mission._groups != null && _mission._groups.size() > 0) {
                JSONArray groups = new JSONArray();

                for (DatabaseGroup _group : _mission._groups) {
                    JSONObject group = new JSONObject();
                    group.put("id", _group._id);
                    group.put("name", _group._name);
                    group.put("blockAdvertising", true);
                    group.put("cryptoPassword", _group._cryptoPassword);

                    JSONObject rx = new JSONObject();
                    rx.put("address", _group._rxAddress);
                    rx.put("port", _group._rxPort);
                    group.put("rx", rx);

                    JSONObject tx = new JSONObject();
                    tx.put("address", _group._txAddress);
                    tx.put("port", _group._txPort);
                    group.put("tx", tx);

                    group.put("type", _group._type);
                    if (_group._type == 1) { //Audio type
                        JSONObject timeline = new JSONObject();
                        timeline.put("enabled", true);
                        timeline.put("maxAudioTimeMs", 30000);
                        group.put("timeline", timeline);

                        JSONObject txAudio = new JSONObject();
                        txAudio.put("encoder", _group._txCodecId);
                        txAudio.put("fdx", _group._fdx);
                        txAudio.put("framingMs", _group._txFramingMs);
                        txAudio.put("maxTxSecs", _group._maxTxSecs);
                        group.put("txAudio", txAudio);
                    }

                    groups.put(group);
                }

                jsonMission.put(Engine.JsonFields.Group.arrayName, groups);
            }
        } catch (Exception e) {
            jsonMission = null;
            e.printStackTrace();
        }

        return jsonMission.toString();
    }
}
