package com.rallytac.engageandroid.legba.data.dto;

import com.google.gson.annotations.JsonAdapter;
import com.rallytac.engageandroid.legba.engage.GroupDiscoveryInfo;
import com.rallytac.engageandroid.legba.engage.GroupDiscoveryInfo.Identity;
import com.rallytac.engageandroid.legba.mapping.ChannelDeserializer;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinEntity;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.converter.PropertyConverter;

import java.io.Serializable;
import java.util.List;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity(nameInDb = "CHANNEL")
@JsonAdapter(ChannelDeserializer.class)
public class Channel implements Serializable {

    public enum EngageType {AUDIO, PRESENCE}

    private static final long serialVersionUID = 923291192016354450L;

    @Id
    private String id;

    @Property(nameInDb = "mission_id")
    private String missionId;

    private String name;

    private String image;

    @Transient
    private boolean isActive;

    @Property(nameInDb = "is_speaker_on")
    private boolean isSpeakerOn;

    @Transient
    private boolean isOnRx;

    @Property(nameInDb = "last_rx_alias")
    private String lastRxAlias;

    private String lastRxDisplayName;

    private String lastRxTime;

    private boolean fullDuplex;

    private int txFramingMs;

    private int txCodecId;

    private int maxTxSecs;

    private String txAddress;

    private int txPort;

    private String rxAddress;

    private int rxPort;

    @Convert(converter = EngageTypeConverter.class, columnType = Integer.class)
    private EngageType engageType;

    @Convert(converter = ChannelTypeConverter.class, columnType = String.class)
    private ChannelType type;

    @Transient
    public List<Identity> users;

    private transient DaoSession daoSession;

    private transient ChannelDao myDao;

    public Channel() {
    }

    public Channel(String id) {
        this.id = id;
    }

    public Channel(String id,
                   String missionId,
                   String name,
                   String image,
                   ChannelType type,
                   boolean fullDuplex,
                   int txFramingMs,
                   int txCodecId,
                   int maxTxSecs,
                   String txAddress,
                   int txPort,
                   String rxAddress,
                   int rxPort,
                   EngageType engageType) {
        this.id = id;
        this.missionId = missionId;
        this.name = name;
        this.image = image;
        this.type = type;
        this.fullDuplex = fullDuplex;
        this.txFramingMs = txFramingMs;
        this.txCodecId = txCodecId;
        this.maxTxSecs = maxTxSecs;
        this.txAddress = txAddress;
        this.txPort = txPort;
        this.rxAddress = rxAddress;
        this.rxPort = rxPort;
        this.engageType = engageType;


        this.isActive = false;
        this.isSpeakerOn = true;
        this.isOnRx = false;
        this.lastRxAlias = "";
        this.lastRxDisplayName = "";
    }

    @Generated(hash = 424181647)
    public Channel(String id, String missionId, String name, String image, boolean isSpeakerOn, String lastRxAlias, String lastRxDisplayName, String lastRxTime, boolean fullDuplex,
            int txFramingMs, int txCodecId, int maxTxSecs, String txAddress, int txPort, String rxAddress, int rxPort, EngageType engageType, ChannelType type) {
        this.id = id;
        this.missionId = missionId;
        this.name = name;
        this.image = image;
        this.isSpeakerOn = isSpeakerOn;
        this.lastRxAlias = lastRxAlias;
        this.lastRxDisplayName = lastRxDisplayName;
        this.lastRxTime = lastRxTime;
        this.fullDuplex = fullDuplex;
        this.txFramingMs = txFramingMs;
        this.txCodecId = txCodecId;
        this.maxTxSecs = maxTxSecs;
        this.txAddress = txAddress;
        this.txPort = txPort;
        this.rxAddress = rxAddress;
        this.rxPort = rxPort;
        this.engageType = engageType;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMissionId() {
        return missionId;
    }

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isSpeakerOn() {
        return isSpeakerOn;
    }

    public void setSpeakerOn(boolean speakerOn) {
        isSpeakerOn = speakerOn;
    }

    public boolean isOnRx() {
        return isOnRx;
    }

    public void setOnRx(boolean onRx) {
        isOnRx = onRx;
    }

    public enum ChannelType {PRIMARY, PRIORITY, RADIO}

    public ChannelType getType() {
        return type;
    }

    public void setType(ChannelType type) {
        this.type = type;
    }

    public String getLastRxDisplayName() {
        return lastRxDisplayName;
    }

    public void setLastRxDisplayName(String lastRxDisplayName) {
        this.lastRxDisplayName = lastRxDisplayName;
    }

    public String getLastRxTime() {
        return lastRxTime;
    }

    public void setLastRxTime(String lastRxTime) {
        this.lastRxTime = lastRxTime;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "id='" + id + '\'' +
                ", missionId='" + missionId + '\'' +
                ", name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", isActive=" + isActive +
                ", isSpeakerOn=" + isSpeakerOn +
                ", isOnRx=" + isOnRx +
                ", users=" + users +
                ", type=" + type +
                '}';
    }

    @Keep
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        daoSession.getChannelsGroupsWithChannelsDao()
                .queryBuilder()
                .where(ChannelsGroupsWithChannelsDao.Properties.ChannelId.eq(getId()))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
        myDao.delete(this);
    }

    public boolean getIsActive() {
        return this.isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean getIsSpeakerOn() {
        return this.isSpeakerOn;
    }

    public void setIsSpeakerOn(boolean isSpeakerOn) {
        this.isSpeakerOn = isSpeakerOn;
    }

    public String getLastRxAlias() {
        return this.lastRxAlias;
    }

    public void setLastRxAlias(String lastRxAlias) {
        this.lastRxAlias = lastRxAlias;
    }

    public static class ChannelTypeConverter implements PropertyConverter<ChannelType, String> {

        @Override
        public ChannelType convertToEntityProperty(String databaseValue) {
            return ChannelType.valueOf(databaseValue);
        }

        @Override
        public String convertToDatabaseValue(ChannelType entityProperty) {
            return entityProperty.name();
        }
    }

    public boolean isFullDuplex() {
        return fullDuplex;
    }

    public void setFullDuplex(boolean fullDuplex) {
        this.fullDuplex = fullDuplex;
    }

    public int getTxFramingMs() {
        return txFramingMs;
    }

    public void setTxFramingMs(int txFramingMs) {
        this.txFramingMs = txFramingMs;
    }

    public int getTxCodecId() {
        return txCodecId;
    }

    public void setTxCodecId(int txCodecId) {
        this.txCodecId = txCodecId;
    }

    public int getMaxTxSecs() {
        return maxTxSecs;
    }

    public void setMaxTxSecs(int maxTxSecs) {
        this.maxTxSecs = maxTxSecs;
    }

    public String getTxAddress() {
        return txAddress;
    }

    public void setTxAddress(String txAddress) {
        this.txAddress = txAddress;
    }

    public int getTxPort() {
        return txPort;
    }

    public void setTxPort(int txPort) {
        this.txPort = txPort;
    }

    public String getRxAddress() {
        return rxAddress;
    }

    public void setRxAddress(String rxAddress) {
        this.rxAddress = rxAddress;
    }

    public int getRxPort() {
        return rxPort;
    }

    public void setRxPort(int rxPort) {
        this.rxPort = rxPort;
    }

    public EngageType getEngageType() {
        return engageType;
    }

    public void setEngageType(EngageType engageType) {
        this.engageType = engageType;
    }

    public static class EngageTypeConverter implements PropertyConverter<EngageType, Integer> {

        @Override
        public EngageType convertToEntityProperty(Integer type) {
            if (type == 1) {
                return EngageType.AUDIO;
            } else {
                return EngageType.PRESENCE;
            }
        }

        @Override
        public Integer convertToDatabaseValue(EngageType type) {
            if (type == EngageType.AUDIO) {
                return 1;
            } else {
                return 2;
            }
        }
    }

    public void insertOrReplace(){
        myDao.insertOrReplace(this);
    }

    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    @Keep
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getChannelDao() : null;
    }

    public boolean getFullDuplex() {
        return this.fullDuplex;
    }
}
