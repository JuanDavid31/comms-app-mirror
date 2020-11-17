package com.rallytac.engageandroid.legba.data.dto;

import com.google.gson.annotations.JsonAdapter;
import com.rallytac.engageandroid.legba.mapping.ChannelDeserializer;
import com.rallytac.engageandroid.legba.mapping.MissionDeserializer;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.ToMany;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity(nameInDb = "MISSIONS")
@JsonAdapter(MissionDeserializer.class)
public class Mission implements Serializable {

    private static final long serialVersionUID = -6105178297191589989L;

    @Id
    private String id;

    private String name;

    private String rpAddress;

    private int rpPort;

    @ToMany(referencedJoinProperty = "missionId")
    private List<ChannelGroup> channelsGroups;

    @ToMany(referencedJoinProperty = "missionId")
    //@OrderBy("date ASC")
    private List<Channel> groups;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1935729854)
    private transient MissionDao myDao;

    public Mission() {
    }

    public Mission(String id) {
        this.id = id;
    }

    public Mission(String id, String name, List<Channel> groups, String rpAddress, int rpPort) {
        this.id = id;
        this.name = name;
        this.groups = groups;
        this.rpAddress = rpAddress;
        this.rpPort = rpPort;
        this.channelsGroups = new ArrayList<>();
    }

    @Generated(hash = 1014324158)
    public Mission(String id, String name, String rpAddress, int rpPort) {
        this.id = id;
        this.name = name;
        this.rpAddress = rpAddress;
        this.rpPort = rpPort;
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



    public void setGroups(List<Channel> groups) {
        this.groups = groups;
    }

    @Override
    public String toString() {
        return "Mission{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", rpAddress='" + rpAddress + '\'' +
                ", rpPort=" + rpPort +
                ", channelsGroups=" + channelsGroups +
                ", channels=" + (groups != null ? groups.size() : null) +
                '}';
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

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1974632286)
    public synchronized void resetChannelsGroups() {
        channelsGroups = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 439982547)
    public List<Channel> getGroups() {
        if (groups == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ChannelDao targetDao = daoSession.getChannelDao();
            List<Channel> groupsNew = targetDao._queryMission_Groups(id);
            synchronized (this) {
                if (groups == null) {
                    groups = groupsNew;
                }
            }
        }
        return groups;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 464128061)
    public synchronized void resetGroups() {
        groups = null;
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
    @Generated(hash = 829174646)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMissionDao() : null;
    }
}
