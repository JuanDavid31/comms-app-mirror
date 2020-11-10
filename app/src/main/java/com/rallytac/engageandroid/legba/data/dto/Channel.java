package com.rallytac.engageandroid.legba.data.dto;

import com.google.gson.annotations.JsonAdapter;
import com.rallytac.engageandroid.legba.engage.GroupDiscoveryInfo;
import com.rallytac.engageandroid.legba.engage.GroupDiscoveryInfo.Identity;
import com.rallytac.engageandroid.legba.mapping.ChannelDeserializer;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinEntity;
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

    @Transient
    private int txFramingMs;

    @Transient
    private int txCodecId;

    @Transient
    private int maxTxSecs;

    @Transient
    private String txAddress;

    @Transient
    private int txPort;

    @Transient
    private String rxAddress;

    @Transient
    private int rxPort;

    @Transient
    private EngageType engageType;

    @Convert(converter = ChannelTypeConverter.class, columnType = String.class)
    private ChannelType type;

    @ToMany
    @JoinEntity(
            entity = ChannelsWithChannelElements.class,
            sourceProperty = "channelId",
            targetProperty = "channelElementId"
    )
    private List<ChannelElement> channelElements;

    @Transient
    public List<Identity> users;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 783605529)
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
                   int txFramingMs,
                   int txCodecId,
                   int maxTxSecs,
                   String txAddress,
                   int txPort,
                   String rxAddress,
                   int rxPort,
                   EngageType engageType,
                   List<ChannelElement> channelElements) {
        this.id = id;
        this.missionId = missionId;
        this.name = name;
        this.image = image;
        this.type = type;
        this.txFramingMs = txFramingMs;
        this.txCodecId = txCodecId;
        this.maxTxSecs = maxTxSecs;
        this.txAddress = txAddress;
        this.txPort = txPort;
        this.rxAddress = rxAddress;
        this.rxPort = rxPort;
        this.engageType = engageType;
        this.channelElements = channelElements;

        this.isActive = false;
        this.isSpeakerOn = true;
        this.isOnRx = false;
        this.lastRxAlias = "";
        this.lastRxDisplayName = "";
    }

    @Generated(hash = 1644940346)
    public Channel(String id, String missionId, String name, String image, boolean isSpeakerOn, String lastRxAlias, String lastRxDisplayName, String lastRxTime, ChannelType type) {
        this.id = id;
        this.missionId = missionId;
        this.name = name;
        this.image = image;
        this.isSpeakerOn = isSpeakerOn;
        this.lastRxAlias = lastRxAlias;
        this.lastRxDisplayName = lastRxDisplayName;
        this.lastRxTime = lastRxTime;
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

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1452385247)
    public List<ChannelElement> getChannelElements() {
        if (channelElements == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ChannelElementDao targetDao = daoSession.getChannelElementDao();
            List<ChannelElement> channelElementsNew = targetDao._queryChannel_ChannelElements(id);
            synchronized (this) {
                if (channelElements == null) {
                    channelElements = channelElementsNew;
                }
            }
        }
        return channelElements;
    }

    public void setChannelElements(List<ChannelElement> channelElements) {
        this.channelElements = channelElements;
        this.isSpeakerOn = true;
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
                ", channelElements=" + channelElements +
                ", type=" + type +
                '}';
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 886780443)
    public synchronized void resetChannelElements() {
        channelElements = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
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

    //ChannelElementType
    public enum ChannelType {PRIMARY, PRIORITY, RADIO}

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

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 2049488309)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getChannelDao() : null;
    }
}
