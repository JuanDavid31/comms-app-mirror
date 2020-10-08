package com.rallytac.engageandroid.legba.engage;

public interface RxListener {
    public void onRx(String id, String other);

    public void stopRx();

    public void onJsonRX(String id, String alias);
}
