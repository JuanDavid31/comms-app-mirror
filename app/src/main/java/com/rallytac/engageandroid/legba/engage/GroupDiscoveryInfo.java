package com.rallytac.engageandroid.legba.engage;

import java.util.List;
import java.util.Objects;

public class GroupDiscoveryInfo{
    public List<GroupAlias> groupAliases;
    public Identity identity;

    public class GroupAlias{
        String alias;
        public String groupId;

        @Override
        public String toString() {
            return "GroupAlias{" +
                    "alias='" + alias + '\'' +
                    ", groupId='" + groupId + '\'' +
                    '}';
        }
    }

    public class Identity{
        public String displayName = "";
        String userId = "";

        @Override
        public String toString() {
            return "Identity{" +
                    "displayName='" + displayName + '\'' +
                    ", userId='" + userId + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Identity identity = (Identity) o;
            return Objects.equals(displayName, identity.displayName) &&
                    Objects.equals(userId, identity.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(displayName, userId);
        }
    }
}
