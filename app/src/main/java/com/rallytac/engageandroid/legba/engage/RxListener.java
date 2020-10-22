package com.rallytac.engageandroid.legba.engage;

public interface RxListener {

    public void stopRx();

    public void onRx(String id, String alias, String displayName);
}
