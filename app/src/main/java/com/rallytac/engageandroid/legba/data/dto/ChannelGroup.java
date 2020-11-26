package com.rallytac.engageandroid.legba.data.dto;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinEntity;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToMany;

import java.io.Serializable;
import java.util.List;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity(nameInDb = "CHANNELS_GROUPS")
public class ChannelGroup implements Serializable {

    private static final long serialVersionUID = -6517664934545170205L;

    @Id(autoincrement = true)
    private Long id;

    private String name;

    @Property(nameInDb = "mission_id")
    private String missionId;

    @ToMany
    @JoinEntity(
            entity = ChannelsGroupsWithChannels.class,
            sourceProperty = "channelGroupId",
            targetProperty = "channelId"
    )
    private List<Channel> channels;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1179602967)
    private transient ChannelGroupDao myDao;

    public ChannelGroup() {
    }

    public ChannelGroup(String name) {
        this.name = name;
    }

    public ChannelGroup(String name, String missionId, List<Channel> channels) {
        this.name = name;
        this.missionId = missionId;
        this.channels = channels;
    }

    @Generated(hash = 2052058136)
    public ChannelGroup(Long id, String name, String missionId) {
        this.id = id;
        this.name = name;
        this.missionId = missionId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMissionId() {
        return missionId;
    }

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1125410815)
    public List<Channel> getChannels() {
        if (channels == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ChannelDao targetDao = daoSession.getChannelDao();
            List<Channel> channelsNew = targetDao._queryChannelGroup_Channels(id);
            synchronized (this) {
                if (channels == null) {
                    channels = channelsNew;
                }
            }
        }
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    @Override
    public String toString() {
        return "ChannelGroup{" +
                "name='" + name + '\'' +
                ", missionId='" + missionId + '\'' +
                '}';
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1313248352)
    public synchronized void resetChannels() {
        channels = null;
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

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 81856386)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getChannelGroupDao() : null;
    }


}
