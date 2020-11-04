package com.rallytac.engageandroid.legba.engage;

public interface GroupDiscoveryListener {
    void onGroupDiscover(String groupId, final GroupDiscoveryInfo groupDiscoveryInfo);

    void onGroupRediscover(String groupId, final GroupDiscoveryInfo groupDiscoveryInfo);

    void onGroupUndiscover(String groupId, final GroupDiscoveryInfo groupDiscoveryInfo);
}
