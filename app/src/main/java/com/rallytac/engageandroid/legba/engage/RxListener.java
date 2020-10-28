package com.rallytac.engageandroid.legba.engage;

public interface RxListener {

    public void onRx(String id, String alias, String displayName);

    public void stopRx(String id, String eventExtraJson);
}
