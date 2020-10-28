//
//  Copyright (c) 2019 Rally Tactical Systems, Inc.
//  All rights reserved.
//

package com.rallytac.engageandroid;

import android.content.Context;
import android.content.SharedPreferences;

import com.rallytac.engageandroid.legba.engage.RxListener;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class Globals
{
    private static Context _ctx = null;
    private static EngageApplication _app = null;
    private static SharedPreferences _sp = null;
    private static SharedPreferences.Editor _spEd = null;
    private static AudioPlayerManager _apm = null;
    public static List<RxListener> rxListeners = new ArrayList();

    public static void setContext(Context ctx)
    {
        _ctx = ctx;
    }

    public static Context getContext()
    {
        return _ctx;
    }

    public static void setEngageApplication(EngageApplication app)
    {
        _app = app;
    }

    public static EngageApplication getEngageApplication()
    {
        return _app;
    }

    public static void setSharedPreferences(SharedPreferences sp)
    {
        _sp = sp;
        _spEd = sp.edit();
    }

    public static SharedPreferences getSharedPreferences()
    {
        return _sp;
    }

    public static SharedPreferences.Editor getSharedPreferencesEditor()
    {
        return _spEd;
    }

    public static void setAudioPlayerManager(AudioPlayerManager apm)
    {
        _apm = apm;
    }

    public static AudioPlayerManager getAudioPlayerManager()
    {
        return _apm;
    }

    public static void notifyListenersStart(final String id, final String alias, final String displayname) {
        Timber.i("notifyListenersStart id -> %s alias -> %s displayName -> %s", id, alias, displayname);
        rxListeners.forEach(rxListener -> rxListener.onRx(id, alias, displayname));
    }

    public static void notifyListenersStop(String id, String eventExtraJson) {
        Timber.i("notifyListenerStop id -> %s eventExtraJson -> %s", id, eventExtraJson);
        rxListeners.forEach(rxListener -> rxListener.stopRx(id, eventExtraJson));
    }
}
