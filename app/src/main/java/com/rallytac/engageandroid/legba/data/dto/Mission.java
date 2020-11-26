package com.rallytac.engageandroid.legba.data.dto;

import com.google.gson.annotations.JsonAdapter;
import com.rallytac.engageandroid.legba.mapping.MissionDeserializer;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.ToMany;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.converter.PropertyConverter;

@Entity(nameInDb = "MISSIONS")
@JsonAdapter(MissionDeserializer.class)
public class Mission implements Serializable {

    private static final long serialVersionUID = -6105178297191589989L;

    public enum MulticastType {OVERRIDE_AND_PREVENT, OVERRIDE_AND_ALLOW, FOLLOW_APP_SETTING}

    @Id
    private String id;

    private String name;

    private boolean useRp;

    private String rpAddress;

    private int rpPort;

    @Convert(converter = MulticastTypeConverter.class, columnType = Integer.class)
    private MulticastType multicastType;

    @ToMany(referencedJoinProperty = "missionId")
    private List<ChannelGroup> channelsGroups;

    @ToMany(referencedJoinProperty = "missionId")
    //@OrderBy("date ASC")
    private List<Channel> channels;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1935729854)
    private transient MissionDao myDao;

    public Mission() {
    }

    public Mission(String id) {
        this.id = id;
    }

    public Mission(String id, String name, List<Channel> channels, boolean useRp, String rpAddress, int rpPort, MulticastType multicastType) {
        this.id = id;
        this.name = name;
        this.channels = channels;
        this.useRp = useRp;
        this.rpAddress = rpAddress;
        this.rpPort = rpPort;
        this.multicastType = multicastType;
    }

    @Generated(hash = 409910037)
    public Mission(String id, String name, boolean useRp, String rpAddress, int rpPort, MulticastType multicastType) {
        this.id = id;
        this.name = name;
        this.useRp = useRp;
        this.rpAddress = rpAddress;
        this.rpPort = rpPort;
        this.multicastType = multicastType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChannelsGroups(List<ChannelGroup> channelsGroups) {
        this.channelsGroups = channelsGroups;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public boolean useRp() {
        return useRp;
    }

    public void setUseRp(boolean useRp) {
        this.useRp = useRp;
    }

    public String getRpAddress() {
        return rpAddress;
    }

    public void setRpAddress(String rpAddress) {
        this.rpAddress = rpAddress;
    }

    public int getRpPort() {
        return rpPort;
    }

    public void setRpPort(int rpPort) {
        this.rpPort = rpPort;
    }

    public MulticastType getMulticastType() {
        return multicastType;
    }

    public void setMulticastType(MulticastType multicastType) {
        this.multicastType = multicastType;
    }

    @Override
    public String toString() {
        return "Mission{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", useRp=" + useRp +
                ", rpAddress='" + rpAddress + '\'' +
                ", rpPort=" + rpPort +
                ", multicastType=" + multicastType +
                ", channelsGroups=" + (channelsGroups != null ? channelsGroups.size() : null )  +
                ", channels=" + (channels != null ? channels.size() : null) +
                '}';
    }

    public static class MulticastTypeConverter implements PropertyConverter<MulticastType, Integer> {

        @Override
        public MulticastType convertToEntityProperty(Integer databaseValue) {
            switch (databaseValue) {
                case 0:
                    return MulticastType.OVERRIDE_AND_PREVENT;
                case 1:
                    return MulticastType.OVERRIDE_AND_ALLOW;
                case 2:
                    return MulticastType.FOLLOW_APP_SETTING;
                default:
                    return MulticastType.OVERRIDE_AND_PREVENT;
            }
        }

        @Override
        public Integer convertToDatabaseValue(MulticastType entityProperty) {
            switch (entityProperty) {
                case OVERRIDE_AND_PREVENT:
                    return 0;
                case OVERRIDE_AND_ALLOW:
                    return 1;
                case FOLLOW_APP_SETTING:
                    return 2;
                default:
                    return 0;
            }
        }
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1477121562)
    public List<ChannelGroup> getChannelsGroups() {
        if (channelsGroups == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ChannelGroupDao targetDao = daoSession.getChannelGroupDao();
            List<ChannelGroup> channelsGroupsNew = targetDao._queryMission_ChannelsGroups(id);
            synchronized (this) {
                if (channelsGroups == null) {
                    channelsGroups = channelsGroupsNew;
                }
            }
        }
        return channelsGroups;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 1974632286)
    public synchronized void resetChannelsGroups() {
        channelsGroups = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1354864954)
    public List<Channel> getChannels() {
        if (channels == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ChannelDao targetDao = daoSession.getChannelDao();
            List<Channel> channelsNew = targetDao._queryMission_Channels(id);
            synchronized (this) {
                if (channels == null) {
                    channels = channelsNew;
                }
            }
        }
        return channels;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 1313248352)
    public synchronized void resetChannels() {
        channels = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Keep
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        for (Channel channel : channels) {
            channel.__setDaoSession(daoSession);
            channel.delete();
        }
        daoSession.getChannelGroupDao()
                .queryBuilder()
                .where(ChannelGroupDao.Properties.MissionId.eq(getId()))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
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

    public void insertOrReplace() {
        for (Channel channel : getChannels()) {
            daoSession.getChannelDao().insertOrReplace(channel);
        }
        for (ChannelGroup channelGroup : getChannelsGroups()){
            daoSession.getChannelGroupDao().insertOrReplace(channelGroup);
        }
        myDao.insertOrReplace(this);
    }

    public boolean getUseRp() {
        return this.useRp;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 829174646)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMissionDao() : null;
    }
}
