package com.rallytac.engageandroid.legba.data.dto;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.ToMany;

import java.io.Serializable;
import java.util.List;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity(nameInDb = "SUBCHANNELS")
public class Subchannel extends ChannelElement implements Serializable {

    private static final long serialVersionUID = -2913330413151415711L;

    @Id
    private Long subchannelId;

    private String image;

    @ToMany(referencedJoinProperty = "subchannelId")
    private List<Member> members;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 924849352)
    private transient SubchannelDao myDao;

    public Subchannel() {
    }

    public Subchannel(Long subchannelId) {
        super(subchannelId);
    }

    @Generated(hash = 59576040)
    public Subchannel(Long subchannelId, String image) {
        this.subchannelId = subchannelId;
        this.image = image;
    }

    public Subchannel(Long subchannelId, String name, String image, List<Member> members) {
        super(subchannelId, name, ChannelElementType.SUBCHANNEL);
        this.image = image;
        this.members = members;
    }

    public Long getMemberId() {
        return this.subchannelId;
    }

    public void setMemberId(Long memberId) {
        this.subchannelId = memberId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 610629417)
    public List<Member> getMembers() {
        if (members == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            MemberDao targetDao = daoSession.getMemberDao();
            List<Member> membersNew = targetDao._querySubchannel_Members(subchannelId);
            synchronized (this) {
                if (members == null) {
                    members = membersNew;
                }
            }
        }
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return "SubChannel{" +
                "image='" + image + '\'' +
                ", members=" + members +
                ", id=" + subchannelId +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1358688666)
    public synchronized void resetMembers() {
        members = null;
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

    public Long getSubchannelId() {
        return this.subchannelId;
    }

    public void setSubchannelId(Long subchannelId) {
        this.subchannelId = subchannelId;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 46434065)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getSubchannelDao() : null;
    }
}