//
//  Copyright (c) 2019 Rally Tactical Systems, Inc.
//  All rights reserved.
//

package com.rallytac.engageandroid;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.rallytac.engage.engine.Engine;
import com.rallytac.engageandroid.Biometrics.DataSeries;
import com.rallytac.engageandroid.Biometrics.RandomHumanBiometricGenerator;
import com.rallytac.engageandroid.legba.data.DataManager;
import com.rallytac.engageandroid.legba.data.dto.DaoMaster;
import com.rallytac.engageandroid.legba.data.dto.DaoSession;
import com.rallytac.engageandroid.legba.data.dto.Mission;
import com.rallytac.engageandroid.legba.data.engagedto.EngageClasses;
import com.rallytac.engageandroid.legba.engage.GroupDiscoveryInfo;

import org.greenrobot.greendao.database.Database;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

public class EngageApplication
        extends
        Application

        implements
        Application.ActivityLifecycleCallbacks,
        ServiceConnection,
        Engine.IEngineListener,
        Engine.IRallypointListener,
        Engine.IGroupListener,
        Engine.ILicenseListener,
        LocationManager.ILocationUpdateNotifications,
        IPushToTalkRequestHandler,
        BluetoothManager.IBtNotification,
        LicenseActivationTask.ITaskCompletionNotification {
    private static String TAG = EngageApplication.class.getSimpleName();

    private DaoSession daoSession;

    public interface IGroupTextMessageListener {
        void onGroupTextMessageRx(PresenceDescriptor sourcePd, String message);
    }

    public interface IPresenceChangeListener {
        void onPresenceAdded(PresenceDescriptor pd);

        void onPresenceChange(PresenceDescriptor pd);

        void onPresenceRemoved(PresenceDescriptor pd);
    }

    public interface IUiUpdateListener {
        void onAnyTxPending();

        void onAnyTxActive();

        void onAnyTxEnding();

        void onAllTxEnded();

        void onGroupUiRefreshNeeded(GroupDescriptor gd);

        void onGroupTxUsurped(GroupDescriptor gd, String eventExtra);

        void onGroupMaxTxTimeExceeded(GroupDescriptor gd);

        void onGroupTxFailed(GroupDescriptor gd, String eventExtra);
    }

    public interface IAssetChangeListener {
        void onAssetDiscovered(String id, String json);

        void onAssetRediscovered(String id, String json);

        void onAssetUndiscovered(String id, String json);
    }

    public interface IConfigurationChangeListener {
        void onMissionChanged();

        void onCriticalConfigurationChange();
    }

    public interface ILicenseChangeListener {
        void onLicenseChanged();

        void onLicenseExpired();

        void onLicenseExpiring(double secondsLeft);
    }

    public interface IGroupTimelineListener {
        void onGroupTimelineEventStarted(GroupDescriptor gd, String eventJson);

        void onGroupTimelineEventUpdated(GroupDescriptor gd, String eventJson);

        void onGroupTimelineEventEnded(GroupDescriptor gd, String eventJson);

        void onGroupTimelineReport(GroupDescriptor gd, String reportJson);

        void onGroupTimelineReportFailed(GroupDescriptor gd);

        void onGroupTimelineGroomed(GroupDescriptor gd, String eventListJson);

        void onGroupHealthReport(GroupDescriptor gd, String reportJson);

        void onGroupHealthReportFailed(GroupDescriptor gd);

        void onGroupStatsReport(GroupDescriptor gd, String reportJson);

        void onGroupStatsReportFailed(GroupDescriptor gd);
    }

    public interface IGroupTimelineListenerLegba {
        void onGroupTimelineEventStarted(String groupId, String eventJson);

        /*void onGroupTimelineEventUpdated(GroupDescriptor gd, String eventJson);

        void onGroupTimelineEventEnded(GroupDescriptor gd, String eventJson);

        void onGroupTimelineReport(GroupDescriptor gd, String reportJson);

        void onGroupTimelineReportFailed(GroupDescriptor gd);

        void onGroupTimelineGroomed(GroupDescriptor gd, String eventListJson);

        void onGroupHealthReport(GroupDescriptor gd, String reportJson);

        void onGroupHealthReportFailed(GroupDescriptor gd);

        void onGroupStatsReport(GroupDescriptor gd, String reportJson);

        void onGroupStatsReportFailed(GroupDescriptor gd);*/
    }

    private EngageService _svc = null;
    private boolean _engineRunning = false;
    private ActiveConfiguration _activeConfiguration = null;
    private boolean _missionChangedStatus = false;
    private LocationManager _locationManager = null;

    private HashSet<IPresenceChangeListener> _presenceChangeListeners = new HashSet<>();
    private HashSet<IUiUpdateListener> _uiUpdateListeners = new HashSet<>();
    private HashSet<IAssetChangeListener> _assetChangeListeners = new HashSet<>();
    private HashSet<IConfigurationChangeListener> _configurationChangeListeners = new HashSet<>();
    private HashSet<ILicenseChangeListener> _licenseChangeListeners = new HashSet<>();
    private HashSet<IGroupTimelineListener> _groupTimelineListeners = new HashSet<>();
    private HashSet<IGroupTextMessageListener> _groupTextMessageListeners = new HashSet<>();


    private long _lastAudioActivity = 0;
    private long _lastTxActivity = 0;
    private boolean _delayTxUnmuteToCaterForSoundPropogation = false;
    private Timer _groupHealthCheckTimer = null;
    private long _lastNetworkErrorNotificationPlayed = 0;
    private HashMap<String, GroupDescriptor> _dynamicGroups = new HashMap<>();
    private HardwareButtonManager _hardwareButtonManager = null;
    private boolean _licenseExpired = false;
    private double _licenseSecondsLeft = 0;
    private Timer _licenseActivationTimer = null;
    private boolean _licenseActivationPaused = false;

    private Timer _humanBiometricsReportingTimer = null;
    private int _hbmTicksSoFar = 0;
    private int _hbmTicksBeforeReport = 5;

    private DataSeries _hbmHeartRate = null;
    private DataSeries _hbmSkinTemp = null;
    private DataSeries _hbmCoreTemp = null;
    private DataSeries _hbmHydration = null;
    private DataSeries _hbmBloodOxygenation = null;
    private DataSeries _hbmFatigueLevel = null;
    private DataSeries _hbmTaskEffectiveness = null;

    private RandomHumanBiometricGenerator _rhbmgHeart = null;
    private RandomHumanBiometricGenerator _rhbmgSkinTemp = null;
    private RandomHumanBiometricGenerator _rhbmgCoreTemp = null;
    private RandomHumanBiometricGenerator _rhbmgHydration = null;
    private RandomHumanBiometricGenerator _rhbmgOxygenation = null;
    private RandomHumanBiometricGenerator _rhbmgFatigueLevel = null;
    private RandomHumanBiometricGenerator _rhbmgTaskEffectiveness = null;

    private JSONObject _cachedPdLocation = null;
    private JSONObject _cachedPdConnectivityInfo = null;
    private JSONObject _cachedPdPowerInfo = null;

    private MyDeviceMonitor _deviceMonitor = null;
    private boolean _enableDevicePowerMonitor = false;
    private boolean _enableDeviceConnectivityMonitor = false;

    private MyApplicationIntentReceiver _appIntentReceiver = null;

    private FirebaseAnalytics _firebaseAnalytics = null;

    private boolean _hasEngineBeenInitialized = false;

    private boolean _terminateOnEngineStopped = false;
    private Activity _terminatingActivity = null;

    public class GroupConnectionTrackerInfo {
        public GroupConnectionTrackerInfo(boolean mc, boolean mcfo, boolean rp) {
            hasMulticastConnection = mc;
            operatingInMulticastFailover = mcfo;
            hasRpConnection = rp;
        }

        public boolean hasMulticastConnection;
        public boolean operatingInMulticastFailover;
        public boolean hasRpConnection;
    }

    private HashMap<String, GroupConnectionTrackerInfo> _groupConnections = new HashMap<>();

    private void eraseGroupConnectionState(String id) {
        _groupConnections.remove(id);
    }

    private void setGroupConnectionState(String id, boolean mc, boolean mcfo, boolean rp) {
        setGroupConnectionState(id, new GroupConnectionTrackerInfo(mc, mcfo, rp));
    }

    private void setGroupConnectionState(String id, GroupConnectionTrackerInfo gts) {
        _groupConnections.put(id, gts);
    }

    public GroupConnectionTrackerInfo getGroupConnectionState(String id) {
        GroupConnectionTrackerInfo rc = _groupConnections.get(id);
        if (rc == null) {
            rc = new GroupConnectionTrackerInfo(false, false, false);
        }

        return rc;
    }

    public boolean isGroupConnectedInSomeWay(String id) {
        GroupConnectionTrackerInfo chk = getGroupConnectionState(id);
        return (chk.hasMulticastConnection || chk.hasRpConnection);
    }

    private HashMap<String, ArrayList<TextMessage>> _textMessageDatabase = new HashMap<>();

    private void cleanupTextMessageDatabase() {
        synchronized (_textMessageDatabase) {
            // TODO: cleanup the text messaging database
        }
    }

    public ArrayList<TextMessage> getTextMessagesForGroup(String id) {
        ArrayList<TextMessage> rc = null;

        synchronized (_textMessageDatabase) {
            ArrayList<TextMessage> theList = _textMessageDatabase.get(id);
            if (theList != null) {
                rc = new ArrayList<>();
                for (TextMessage tm : theList) {
                    rc.add(tm);
                }
            }
        }

        return rc;
    }

    public void addTextMessage(TextMessage tm) {
        synchronized (_textMessageDatabase) {
            ArrayList<TextMessage> theList = _textMessageDatabase.get(tm._groupId);
            if (theList == null) {
                theList = new ArrayList<>();
                _textMessageDatabase.put(tm._groupId, theList);
            }

            theList.add(tm);

            cleanupTextMessageDatabase();
        }
    }

    private class MyApplicationIntentReceiver extends BroadcastReceiver {
        public void start() {
            IntentFilter filter = new IntentFilter();

            filter.addAction(BuildConfig.APPLICATION_ID + "." + getString(R.string.app_intent_ptt_on));
            filter.addAction(BuildConfig.APPLICATION_ID + "." + getString(R.string.app_intent_ptt_off));
            filter.addAction(BuildConfig.APPLICATION_ID + "." + getString(R.string.app_intent_next_group));
            filter.addAction(BuildConfig.APPLICATION_ID + "." + getString(R.string.app_intent_prev_group));
            filter.addAction(BuildConfig.APPLICATION_ID + "." + getString(R.string.app_intent_mute_group));
            filter.addAction(BuildConfig.APPLICATION_ID + "." + getString(R.string.app_intent_unmute_group));

            registerReceiver(this, filter);
        }

        public void stop() {
            unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "{DBG}: MyApplicationIntentReceiver: " + intent.toString());

            String action = intent.getAction();
            if (action == null) {
                return;
            }

            int pos = action.lastIndexOf(".");
            if (pos <= 0) {
                return;
            }

            action = action.substring(pos + 1);
            Log.i(TAG, "{DBG}: MyApplicationIntentReceiver: action=" + action);

            if (action.compareTo(getString(R.string.app_intent_ptt_on)) == 0) {
                startTx(0, 0);
            } else if (action.compareTo(getString(R.string.app_intent_ptt_off)) == 0) {
                endTx();
            }
        }
    }

    private class MyDeviceMonitor extends BroadcastReceiver {
        private final int NUM_SIGNAL_LEVELS = 5;

        public MyDeviceMonitor() {
        }

        public void start() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
            intentFilter.addAction("android.intent.action.BATTERY_LEVEL_CHANGED");
            intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
            intentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
            intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
            intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);

            registerReceiver(this, intentFilter);
        }

        public void stop() {
            unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "{DBG}: " + intent.toString());


            String action = intent.getAction();
            if (action == null) {
                return;
            }

            Log.i(TAG, "{DBG}: action=" + action);

            if (action.compareTo(WifiManager.RSSI_CHANGED_ACTION) == 0) {
                try {
                    Bundle bundle = intent.getExtras();
                    if (bundle != null) {
                        int newRssiDbm = bundle.getInt(WifiManager.EXTRA_NEW_RSSI);
                        int level = WifiManager.calculateSignalLevel(newRssiDbm, NUM_SIGNAL_LEVELS);

                        onConnectivityChange(true, ConnectivityType.wirelessWifi, newRssiDbm, level);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (action.compareTo(ConnectivityManager.CONNECTIVITY_ACTION) == 0) {
                try {
                    NetworkInfo info1 = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                    NetworkInfo info2 = intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);

                    Log.i(TAG, "{DBG}: info1=" + info1);

                    if (info1 != null) {
                        if (info1.isConnected()) {
                            if (info1.getType() == ConnectivityManager.TYPE_MOBILE) {
                                // TODO: RSSI for cellular
                                onConnectivityChange(true, ConnectivityType.wirelessCellular, 0, NUM_SIGNAL_LEVELS);
                            } else if (info1.getType() == ConnectivityManager.TYPE_WIFI) {
                                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                                int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), NUM_SIGNAL_LEVELS);

                                onConnectivityChange(true, ConnectivityType.wirelessWifi, wifiInfo.getRssi(), level);
                            } else if (info1.getType() == ConnectivityManager.TYPE_ETHERNET) {
                                onConnectivityChange(true, ConnectivityType.wired, 0, NUM_SIGNAL_LEVELS);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (action.compareTo(Intent.ACTION_BATTERY_CHANGED) == 0) {
                try {
                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                    int levelPercent = (int) (((float) level / scale) * 100);

                    PowerSourceType pst;
                    int pluggedIn = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                    if (pluggedIn == 0) {
                        pst = PowerSourceType.battery;
                    } else {
                        pst = PowerSourceType.wired;
                    }

                    PowerSourceState pss;
                    int chargingExtra = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
                    switch (chargingExtra) {
                        case BatteryManager.BATTERY_STATUS_UNKNOWN:
                            pss = PowerSourceState.unknown;
                            break;

                        case BatteryManager.BATTERY_STATUS_CHARGING:
                            pss = PowerSourceState.charging;
                            break;

                        case BatteryManager.BATTERY_STATUS_DISCHARGING:
                            pss = PowerSourceState.discharging;
                            break;

                        case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                            pss = PowerSourceState.notCharging;
                            break;

                        case BatteryManager.BATTERY_STATUS_FULL:
                            pss = PowerSourceState.full;
                            break;

                        default:
                            pss = PowerSourceState.unknown;
                            break;
                    }

                    onPowerChange(pst, pss, levelPercent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.w(TAG, "{DBG}: unhandled action '" + action + "'");
            }
        }
    }

    private void startFirebaseAnalytics() {
        if (Utils.boolOpt(getString(R.string.opt_firebase_analytics_enabled), false)) {
            try {
                _firebaseAnalytics = FirebaseAnalytics.getInstance(this);
            } catch (Exception e) {
                _firebaseAnalytics = null;
                e.printStackTrace();
            }
        }
    }

    private void stopFirebaseAnalytics() {
        _firebaseAnalytics = null;
    }

    public void logEvent(String eventName) {
        logEvent(eventName, null);
    }

    public void logEvent(String eventName, String key, int value) {
        Bundle b = new Bundle();
        b.putInt(key, value);
        logEvent(eventName, b);
    }

    public void logEvent(String eventName, String key, long value) {
        Bundle b = new Bundle();
        b.putLong(key, value);
        logEvent(eventName, b);
    }

    public void logEvent(String eventName, Bundle b) {
        try {
            if (_firebaseAnalytics != null) {
                _firebaseAnalytics.logEvent(eventName, b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupDirectories() {
        try {
            File certStoreDir = new File(getCertStoreCacheDir());
            if (!certStoreDir.exists()) {
                certStoreDir.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setupFilesystemLogging() {
        try {
            File appDirectory = new File(Environment.getExternalStorageDirectory() + "/" + BuildConfig.APPLICATION_ID);
            File logDirectory = new File(appDirectory + "/log");
            File logFile = new File(logDirectory, "logcat-" + System.currentTimeMillis() + ".txt");

            if (!appDirectory.exists()) {
                appDirectory.mkdir();
            }

            if (!logDirectory.exists()) {
                logDirectory.mkdir();
            }

            try {

                Process process = Runtime.getRuntime().exec("logcat -c");
                process = Runtime.getRuntime().exec("logcat -f " + logFile);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        Timber.plant(new Timber.DebugTree());


        // regular SQLite database
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "legba-test");
        Database db = helper.getWritableDb();

        // encrypted SQLCipher database
        // note: you need to add SQLCipher to your dependencies, check the build.gradle file
        // DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "notes-db-encrypted");
        // Database db = helper.getEncryptedWritableDb("encryption-key");
        daoSession = new DaoMaster(db).newSession();

        // Its important to set this as soon as possible!
        Engine.setApplicationContext(this.getApplicationContext());

        // Note: This is for developer testing only!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                //Os.setenv("ENGAGE_OVERRIDE_DEVICE_ID", "$RTS$DELETEME01", true);
                //Os.setenv("ENGAGE_LICENSE_CHECK_INTERVAL_MS", "5000", true);

            } catch (Exception e) {
                Log.e(TAG, "cannot set 'ENGAGE_OVERRIDE_DEVICE_ID' environment variable");
            }
        }

        Globals.setEngageApplication(this);
        Globals.setContext(getApplicationContext());
        Globals.setSharedPreferences(PreferenceManager.getDefaultSharedPreferences(this));
        Globals.setAudioPlayerManager(new AudioPlayerManager(this));

        // Globals.getEngageApplication().onGroupRxStarted();

        setupDirectories();
        //setupFilesystemLogging();

        startFirebaseAnalytics();

        runPreflightCheck();

        registerActivityLifecycleCallbacks(this);

        startService(new Intent(this, EngageService.class));

        int bindingFlags = (Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
        Intent intent = new Intent(this, EngageService.class);
        bindService(intent, this, bindingFlags);

        startAppIntentReceiver();
    }

    @Override
    public void onTerminate() {
        Log.d(TAG, "onTerminate");
        stop();
        stopFirebaseAnalytics();

        super.onTerminate();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        Log.d(TAG, "onActivityCreated: " + activity.toString());
        if (!_hasEngineBeenInitialized) {
            if (activity.getClass().getSimpleName().compareTo(LauncherActivity.class.getSimpleName()) != 0) {
                activity.finish();

                Intent intent = new Intent(this, LauncherActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.d(TAG, "onActivityStarted: " + activity.toString());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.d(TAG, "onActivityResumed: " + activity.toString());
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.d(TAG, "onActivityPaused: " + activity.toString());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.d(TAG, "onActivityStopped: " + activity.toString());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        Log.d(TAG, "onActivitySaveInstanceState: " + activity.toString());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.d(TAG, "onActivityDestroyed: " + activity.toString());
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        Log.d(TAG, "onServiceConnected: " + name.toString() + ", " + binder.toString());
        _svc = ((EngageService.EngageServiceBinder) binder).getService();

        getEngine().addEngineListener(this);
        getEngine().addRallypointListener(this);
        getEngine().addGroupListener(this);
        getEngine().addLicenseListener(this);

        startDeviceMonitor();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected: " + name.toString());
        cleanupServiceConnection();
        _svc = null;
    }

    @Override
    public void onBindingDied(ComponentName name) {
        Log.d(TAG, "onBindingDied: " + name.toString());
        cleanupServiceConnection();
        _svc = null;
    }

    @Override
    public void onNullBinding(ComponentName name) {
        Log.d(TAG, "onNullBinding: " + name.toString());
        cleanupServiceConnection();
        _svc = null;
    }

    private void updateCachedPdLocation(Location location) {
        try {
            if (location != null) {
                JSONObject obj = new JSONObject();

                obj.put(Engine.JsonFields.Location.longitude, location.getLongitude());
                obj.put(Engine.JsonFields.Location.latitude, location.getLatitude());
                if (location.hasAltitude()) {
                    obj.put(Engine.JsonFields.Location.altitude, location.getAltitude());
                }
                if (location.hasBearing()) {
                    obj.put(Engine.JsonFields.Location.direction, location.getBearing());
                }
                if (location.hasSpeed()) {
                    obj.put(Engine.JsonFields.Location.speed, location.getSpeed());
                }

                _cachedPdLocation = obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCachedPdConnectivityInfo(ConnectivityType type, int rssi, int qualityRating) {
        if (_enableDeviceConnectivityMonitor) {
            try {
                JSONObject obj = new JSONObject();
                obj.put(Engine.JsonFields.Connectivity.type, type.ordinal());
                obj.put(Engine.JsonFields.Connectivity.strength, rssi);
                obj.put(Engine.JsonFields.Connectivity.rating, qualityRating);
                _cachedPdConnectivityInfo = obj;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            _cachedPdConnectivityInfo = null;
        }
    }

    private void updateCachedPdPowerInfo(PowerSourceType source, PowerSourceState state, int level) {
        if (_enableDevicePowerMonitor) {
            try {
                JSONObject obj = new JSONObject();
                obj.put(Engine.JsonFields.Power.source, source.ordinal());
                obj.put(Engine.JsonFields.Power.state, state.ordinal());
                obj.put(Engine.JsonFields.Power.level, level);
                _cachedPdPowerInfo = obj;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            _cachedPdPowerInfo = null;
        }
    }

    public enum ConnectivityType {unknown, wired, wirelessWifi, wirelessCellular}

    public enum PowerSourceType {unknown, battery, wired}

    public enum PowerSourceState {unknown, charging, discharging, notCharging, full}

    public void onConnectivityChange(boolean connected, ConnectivityType type, int rssi, int qualityRating) {
        updateCachedPdConnectivityInfo(type, rssi, qualityRating);
        sendUpdatedPd(buildPd());
    }

    public void onPowerChange(PowerSourceType source, PowerSourceState state, int level) {
        updateCachedPdPowerInfo(source, state, level);
        sendUpdatedPd(buildPd());
    }

    private JSONObject buildPd() {
        JSONObject pd = null;

        try {
            if (getActiveConfiguration() != null) {
                pd = new JSONObject();

                pd.put(Engine.JsonFields.Identity.objectName, getActiveConfiguration().makeIdentityObject());
                if (_cachedPdLocation != null) {
                    pd.put(Engine.JsonFields.Location.objectName, _cachedPdLocation);
                }

                if (_cachedPdPowerInfo != null) {
                    pd.put(Engine.JsonFields.Power.objectName, _cachedPdPowerInfo);
                }

                if (_cachedPdConnectivityInfo != null) {
                    pd.put(Engine.JsonFields.Connectivity.objectName, _cachedPdConnectivityInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            pd = null;
        }

        return pd;
    }

    public Set<String> getCertificateStoresPasswords() {
        Set<String> rc = new HashSet<>();

        // Add the empty password
        rc.add("");

        // Load passwords from resources
        String[] resPasswords = getResources().getStringArray(R.array.certstore_passwords);
        if (resPasswords != null && resPasswords.length > 0) {
            for (String s : resPasswords) {
                rc.add(s);
            }
        }

        // Load passwords from preferences
        Set<String> prefsPasswords = Globals.getSharedPreferences().getStringSet(PreferenceKeys.USER_CERT_STORE_PASSWORD_SET, null);
        if (prefsPasswords != null && prefsPasswords.size() > 0) {
            for (String s : prefsPasswords) {
                rc.add(s);
            }
        }

        return rc;
    }

    public JSONObject getCertificateStoreDescriptorForFile(String filePath) {
        JSONObject rc = null;
        String jsonText;

        try {
            Set<String> passwords = getCertificateStoresPasswords();
            for (String pwd : passwords) {
                jsonText = Globals.getEngageApplication().getEngine().engageQueryCertStoreContents(filePath, pwd);
                if (!Utils.isEmptyString(jsonText)) {
                    JSONObject tmp = new JSONObject(jsonText);
                    if (tmp != null) {
                        int version = tmp.optInt(Engine.JsonFields.CertStoreDescriptor.version, 0);
                        if (version > 0) {
                            rc = tmp;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            rc = null;
            e.printStackTrace();
        }

        return rc;
    }

    public void restartDeviceMonitoring() {
        startDeviceMonitor();
        stopDeviceMonitor();
    }

    private void startDeviceMonitor() {
        if (_deviceMonitor == null) {
            _enableDevicePowerMonitor = Globals.getSharedPreferences().getBoolean(PreferenceKeys.USER_EXPERIMENT_ENABLE_DEVICE_REPORT_POWER, false);
            _enableDeviceConnectivityMonitor = Globals.getSharedPreferences().getBoolean(PreferenceKeys.USER_EXPERIMENT_ENABLE_DEVICE_REPORT_CONNECTIVITY, false);

            if (_enableDevicePowerMonitor || _enableDeviceConnectivityMonitor) {
                _deviceMonitor = new MyDeviceMonitor();
                _deviceMonitor.start();
            }
        }
    }

    private void stopDeviceMonitor() {
        if (_deviceMonitor != null) {
            _deviceMonitor.stop();
            _deviceMonitor = null;
        }
    }

    private void startAppIntentReceiver() {
        if (_appIntentReceiver == null) {
            _appIntentReceiver = new MyApplicationIntentReceiver();
            _appIntentReceiver.start();
        }
    }

    private void stopAppIntentReceiver() {
        if (_appIntentReceiver != null) {
            _appIntentReceiver.stop();
            _appIntentReceiver = null;
        }
    }

    private void cleanupServiceConnection() {
        Log.d(TAG, "cleanupServiceConnection");

        stopDeviceMonitor();

        if (getEngine() != null) {
            getEngine().removeEngineListener(this);
            getEngine().removeRallypointListener(this);
            getEngine().removeGroupListener(this);
        }
    }

    public void stop() {

        stopAppIntentReceiver();
        stopDeviceMonitor();

        if (getEngine() != null) {
            getEngine().engageStop();
        } else {
            clearOutServiceOperation();

            goIdle();

            try {
                Thread.sleep(500);
            } catch (Exception e) {
                Log.d(TAG, "stop: exception");
            }
        }
    }

    private void clearOutServiceOperation() {
        unbindService(EngageApplication.this);
        stopService(new Intent(EngageApplication.this, EngageService.class));
        unregisterActivityLifecycleCallbacks(EngageApplication.this);
    }

    public void terminateApplicationAndReturnToAndroid(Activity callingActivity) {
        _terminateOnEngineStopped = true;
        _terminatingActivity = callingActivity;
        stop();
    }

    public void addPresenceChangeListener(IPresenceChangeListener listener) {
        synchronized (_presenceChangeListeners) {
            _presenceChangeListeners.add(listener);
        }
    }

    public void removePresenceChangeListener(IPresenceChangeListener listener) {
        synchronized (_presenceChangeListeners) {
            _presenceChangeListeners.remove(listener);
        }
    }

    public void addUiUpdateListener(IUiUpdateListener listener) {
        synchronized (_uiUpdateListeners) {
            _uiUpdateListeners.add(listener);
        }
    }

    public void removeUiUpdateListener(IUiUpdateListener listener) {
        synchronized (_uiUpdateListeners) {
            _uiUpdateListeners.remove(listener);
        }
    }

    public void addAssetChangeListener(IAssetChangeListener listener) {
        synchronized (_assetChangeListeners) {
            _assetChangeListeners.add(listener);
        }
    }

    public void removeAssetChangeListener(IAssetChangeListener listener) {
        synchronized (_assetChangeListeners) {
            _assetChangeListeners.remove(listener);
        }
    }

    public void addConfigurationChangeListener(IConfigurationChangeListener listener) {
        synchronized (_configurationChangeListeners) {
            _configurationChangeListeners.add(listener);
        }
    }

    public void removeConfigurationChangeListener(IConfigurationChangeListener listener) {
        synchronized (_configurationChangeListeners) {
            _configurationChangeListeners.remove(listener);
        }
    }

    public void addLicenseChangeListener(ILicenseChangeListener listener) {
        synchronized (_licenseChangeListeners) {
            _licenseChangeListeners.add(listener);
        }
    }

    public void removeLicenseChangeListener(ILicenseChangeListener listener) {
        synchronized (_licenseChangeListeners) {
            _licenseChangeListeners.remove(listener);
        }
    }

    public void addGroupTimelineListener(IGroupTimelineListener listener) {
        synchronized (_groupTimelineListeners) {
            _groupTimelineListeners.add(listener);
        }
    }

    public void removeGroupTimelineListener(IGroupTimelineListener listener) {
        synchronized (_groupTimelineListeners) {
            _groupTimelineListeners.remove(listener);
        }
    }

    public void addGroupTextMessageListener(IGroupTextMessageListener listener) {
        synchronized (_groupTextMessageListeners) {
            _groupTextMessageListeners.add(listener);
        }
    }

    public void removeGroupTextMessageListener(IGroupTextMessageListener listener) {
        synchronized (_groupTextMessageListeners) {
            _groupTextMessageListeners.remove(listener);
        }
    }

    public void startHardwareButtonManager() {
        Log.d(TAG, "startHardwareButtonManager");
        _hardwareButtonManager = new HardwareButtonManager(this, this, this);
        _hardwareButtonManager.start();
    }

    public void stopHardwareButtonManager() {
        if (_hardwareButtonManager != null) {
            _hardwareButtonManager.stop();
            _hardwareButtonManager = null;
        }
    }

    public void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates");
        stopLocationUpdates();

        ActiveConfiguration.LocationConfiguration lc = getActiveConfiguration().getLocationConfiguration();
        if (lc != null && lc.enabled) {
            _locationManager = new LocationManager(this,
                    this,
                    lc.accuracy,
                    lc.intervalMs,
                    lc.minIntervalMs,
                    lc.minDisplacement);

            _locationManager.start();
        }
    }

    public void stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates");
        if (_locationManager != null) {
            _locationManager.stop();
            _locationManager = null;
        }
    }

    public void setMissionChangedStatus(boolean s) {
        Log.d(TAG, "setMissionChangedStatus: " + s);
        _missionChangedStatus = s;
    }

    public boolean getMissionChangedStatus() {
        return _missionChangedStatus;
    }

    private void sendUpdatedPd(JSONObject pd) {
        if (pd == null) {
            Log.w(TAG, "sendUpdatedPd with null PD");
            return;
        }

        try {
            if (getActiveConfiguration() != null) {
                Log.d(TAG, "sendUpdatedPd pd=" + pd.toString());

                if (getActiveConfiguration().getMissionGroups() != null) {
                    boolean anyPresenceGroups = false;
                    String pdString = pd.toString();

                    for (GroupDescriptor gd : getActiveConfiguration().getMissionGroups()) {
                        if (gd.type == GroupDescriptor.Type.gtPresence) {
                            anyPresenceGroups = true;
                            getEngine().engageUpdatePresenceDescriptor(gd.id, pdString, 1);
                        }
                    }

                    if (!anyPresenceGroups) {
                        Log.w(TAG, "sendUpdatedPd but no presence groups");
                    } else {
                        Log.i(TAG, "sendUpdatedPd sent updated PD: " + pdString);
                    }
                } else {
                    Log.w(TAG, "sendUpdatedPd but no mission groups");
                }
            } else {
                Log.w(TAG, "sendUpdatedPd but no active configuration");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onLocationUpdated(Location loc) {
        Log.d(TAG, "onLocationUpdated: " + loc.toString());
        updateCachedPdLocation(loc);
        sendUpdatedPd(buildPd());
    }

    public VolumeLevels loadVolumeLevels(String groupId) {
        VolumeLevels vl = new VolumeLevels();

        vl.left = Globals.getSharedPreferences().getInt(PreferenceKeys.VOLUME_LEFT_FOR_GROUP_BASE_NAME + groupId, 100);
        if (vl.left < 0) {
            vl.left = 0;
        }

        vl.right = Globals.getSharedPreferences().getInt(PreferenceKeys.VOLUME_RIGHT_FOR_GROUP_BASE_NAME + groupId, 100);
        if (vl.right < 0) {
            vl.right = 0;
        }

        return vl;
    }

    public void saveVolumeLevels(String groupId, VolumeLevels vl) {
        Globals.getSharedPreferencesEditor().putInt(PreferenceKeys.VOLUME_LEFT_FOR_GROUP_BASE_NAME + groupId, vl.left);
        Globals.getSharedPreferencesEditor().putInt(PreferenceKeys.VOLUME_RIGHT_FOR_GROUP_BASE_NAME + groupId, vl.right);
        Globals.getSharedPreferencesEditor().apply();
    }

    private GroupDescriptor getGroup(String id) {
        if (getActiveConfiguration().getMissionGroups() != null) {
            for (GroupDescriptor gd : getActiveConfiguration().getMissionGroups()) {
                if (gd.id.compareTo(id) == 0) {
                    return gd;
                }
            }
        }

        return null;
    }

    private String buildAdvancedTxJson(int flags, int priority, int subchannelTag, boolean includeNodeId, String alias) {
        String rc;

        try {
            JSONObject obj = new JSONObject();

            obj.put(Engine.JsonFields.AdvancedTxParams.flags, flags);
            obj.put(Engine.JsonFields.AdvancedTxParams.priority, priority);
            obj.put(Engine.JsonFields.AdvancedTxParams.subchannelTag, subchannelTag);
            obj.put(Engine.JsonFields.AdvancedTxParams.includeNodeId, includeNodeId);
            obj.put(Engine.JsonFields.AdvancedTxParams.muted, false);

            if (!Utils.isEmptyString(alias)) {
                obj.put("alias", alias);
            }

            rc = obj.toString();

            //Log.e(TAG, rc);
        } catch (Exception e) {
            e.printStackTrace();
            rc = null;
        }

        return rc;
    }

    public String buildAdvancedTxJsonPublic(int flags, int priority, int subchannelTag, boolean includeNodeId, String alias) {
        String rc;

        try {
            JSONObject obj = new JSONObject();

            obj.put(Engine.JsonFields.AdvancedTxParams.flags, flags);
            obj.put(Engine.JsonFields.AdvancedTxParams.priority, priority);
            obj.put(Engine.JsonFields.AdvancedTxParams.subchannelTag, subchannelTag);
            obj.put(Engine.JsonFields.AdvancedTxParams.includeNodeId, includeNodeId);
            obj.put(Engine.JsonFields.AdvancedTxParams.muted, true);

            if (!Utils.isEmptyString(alias)) {
                obj.put("alias", alias);
            }

            rc = obj.toString();

            //Log.e(TAG, rc);
        } catch (Exception e) {
            e.printStackTrace();
            rc = null;
        }

        return rc;
    }

    public String buildFinalGroupJsonConfiguration(String groupJson) {
        String rc;

        try {
            JSONObject group = new JSONObject(groupJson);

            if (!Utils.isEmptyString(_activeConfiguration.getNetworkInterfaceName())) {
                group.put("interfaceName", _activeConfiguration.getNetworkInterfaceName());
            }

            if (!Utils.isEmptyString(_activeConfiguration.getUserAlias())) {
                group.put("alias", _activeConfiguration.getUserAlias());
            }

            if (group.optInt(Engine.JsonFields.Group.type, 0) == 1) {
                JSONObject audio = new JSONObject();

                //audio.put("inputId", 1);
                //audio.put("outputId", 4);
                audio.put("outputGain", (_activeConfiguration.getSpeakerOutputBoostFactor() * 100));

                group.put("audio", audio);
            }

            if (_activeConfiguration.getUseRp()) {
                JSONObject rallypoint = new JSONObject();

                JSONObject host = new JSONObject();
                host.put("address", _activeConfiguration.getRpAddress());
                host.put("port", _activeConfiguration.getRpPort());

                rallypoint.put("host", host);
                rallypoint.put("certificate", "@certstore://" + Globals.getContext().getString(R.string.certstore_default_certificate_id));
                rallypoint.put("certificateKey", "@certstore://" + Globals.getContext().getString(R.string.certstore_default_certificate_id));

                JSONArray rallypoints = new JSONArray();
                rallypoints.put(rallypoint);

                group.put("rallypoints", rallypoints);

                // Multicast failover only applies when rallypoints are present
                group.put("enableMulticastFailover", _activeConfiguration.getMulticastFailoverConfiguration().enabled);
                group.put("multicastFailoverSecs", _activeConfiguration.getMulticastFailoverConfiguration().thresholdSecs);
            }

            {
                JSONObject timeline = new JSONObject();

                timeline.put("enabled", true);
                group.put("timeline", timeline);
            }

            rc = group.toString();
        } catch (Exception e) {
            e.printStackTrace();
            rc = null;
        }

        return rc;
    }

    public String buildFinalGroupJsonConfigurationLegba(String groupJson, Mission mission) { //TODO: Fix
        String rc;

        try {
            JSONObject group = new JSONObject(groupJson);

            if (!Utils.isEmptyString(_activeConfiguration.getNetworkInterfaceName())) {
                group.put("interfaceName", _activeConfiguration.getNetworkInterfaceName());
            }

            if (!Utils.isEmptyString(_activeConfiguration.getUserAlias())) {
                group.put("alias", _activeConfiguration.getUserAlias());
            }

            if (group.optInt(Engine.JsonFields.Group.type, 0) == 1) {
                JSONObject audio = new JSONObject();

                //audio.put("inputId", 1);
                //audio.put("outputId", 4);
                audio.put("outputGain", (_activeConfiguration.getSpeakerOutputBoostFactor() * 100));

                group.put("audio", audio);
            }

            if (mission.useRp()) {
                JSONObject rallypoint = new JSONObject();

                JSONObject host = new JSONObject();
                host.put("address", mission.getRpAddress());
                host.put("port", mission.getRpPort());
                rallypoint.put("host", host);

                rallypoint.put("certificate", "@certstore://" + Globals.getContext().getString(R.string.certstore_default_certificate_id));
                rallypoint.put("certificateKey", "@certstore://" + Globals.getContext().getString(R.string.certstore_default_certificate_id));

                JSONArray rallypoints = new JSONArray();
                rallypoints.put(rallypoint);

                group.put("rallypoints", rallypoints);

                // Multicast failover only applies when rallypoints are present
                group.put("enableMulticastFailover", _activeConfiguration.getMulticastFailoverConfiguration().enabled);
                group.put("multicastFailoverSecs", _activeConfiguration.getMulticastFailoverConfiguration().thresholdSecs);
            }

            {
                JSONObject timeline = new JSONObject();

                timeline.put("enabled", true);
                group.put("timeline", timeline);
            }

            rc = group.toString();
        } catch (Exception e) {
            e.printStackTrace();
            rc = null;
        }

        return rc;
    }

    public void createAllGroupObjects() {
        Log.d(TAG, "createAllGroupObjects");
        try {
            for (GroupDescriptor gd : _activeConfiguration.getMissionGroups()) {
                Log.d(TAG, "creating " + gd.id + " of mission " + _activeConfiguration.getMissionName());
                Timber.i(String.format("creating %s of mission %s", gd.id, _activeConfiguration.getMissionName()));
                getEngine().engageCreateGroup(buildFinalGroupJsonConfiguration(gd.jsonConfiguration));
                if (gd.type == GroupDescriptor.Type.gtAudio) {
                    VolumeLevels vl = loadVolumeLevels(gd.id);
                    getEngine().engageSetGroupRxVolume(gd.id, vl.left, vl.right);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void joinSelectedGroups() {
        Log.d(TAG, "joinSelectedGroups");

        // TODO: leaving everything before joining is terrible - needs to be optimized!
        leaveAllGroups();

        try {
            for (GroupDescriptor gd : _activeConfiguration.getMissionGroups()) {
                boolean joinIt = false;

                if (gd.type == GroupDescriptor.Type.gtPresence) {
                    // All presence groups get joined
                    joinIt = true;

                } else if (gd.type == GroupDescriptor.Type.gtRaw) {
                    // All raw groups get joined
                    joinIt = true;
                } /*else if (gd.type == GroupDescriptor.Type.gtAudio) {
                    // Maybe just the one that's the single view
                    if (_activeConfiguration.getUiMode() == Constants.UiMode.vSingle) {
                        if (gd.selectedForSingleView) {
                            joinIt = true;
                        }
                    }
                    // ... or the multi-view group(s)?
                    else if (_activeConfiguration.getUiMode() == Constants.UiMode.vMulti) {
                        if (gd.selectedForMultiView) {
                            joinIt = true;
                        }
                    }
                }*/

                joinIt = true;

                if (joinIt) {
                    if (gd.joined) {
                        Log.w(TAG, "joining a group which is already joined");
                    }

                    Timber.i("Joining %s", gd.id);
                    getEngine().engageJoinGroup(gd.id);
                }
            }

            stopGroupHealthCheckTimer();
            startGroupHealthCheckerTimer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void leaveAllGroups() {
        Log.d(TAG, "leaveAllGroups");
        try {
            stopGroupHealthCheckTimer();
            for (GroupDescriptor gd : _activeConfiguration.getMissionGroups()) {
                getEngine().engageLeaveGroup(gd.id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startGroupHealthCheckerTimer() {
        Log.d(TAG, "startGroupHealthCheckerTimer");
        if (_groupHealthCheckTimer == null) {
            _groupHealthCheckTimer = new Timer();
            _groupHealthCheckTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    checkOnGroupHealth();
                }
            }, Constants.GROUP_HEALTH_CHECK_TIMER_INITIAL_DELAY_MS, Constants.GROUP_HEALTH_CHECK_TIMER_INTERVAL_MS);
        }
    }

    private void stopGroupHealthCheckTimer() {
        Log.d(TAG, "stopGroupHealthCheckTimer");
        if (_groupHealthCheckTimer != null) {
            _groupHealthCheckTimer.cancel();
            _groupHealthCheckTimer = null;
        }
    }

    private void checkOnGroupHealth() {
        if (_activeConfiguration.getNotifyOnNetworkError()) {
            for (GroupDescriptor gd : _activeConfiguration.getMissionGroups()) {
                if (gd.created && gd.joined && !gd.isConnectedInSomeForm()) {
                    long now = Utils.nowMs();
                    if (now - _lastNetworkErrorNotificationPlayed >= Constants.GROUP_HEALTH_CHECK_NETWORK_ERROR_NOTIFICATION_MIN_INTERVAL_MS) {
                        playNetworkDownNotification();
                        _lastNetworkErrorNotificationPlayed = now;
                    }

                    break;
                }
            }
        }
    }

    public void vibrate() {
        if (_activeConfiguration.getEnableVibrations()) {
            Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            if (vibe != null && vibe.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= 26) {
                    vibe.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibe.vibrate(100);
                }
            }
        }
    }

    public void playAssetDiscoveredNotification() {
        Log.d(TAG, "playAssetDiscoveredOnNotification");

        vibrate();

        float volume = _activeConfiguration.getPttToneNotificationLevel();
        if (volume == 0.0) {
            return;
        }

        try {
            Globals.getAudioPlayerManager().playNotification(R.raw.asset_discovered, volume, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playAssetUndiscoveredNotification() {
        Log.d(TAG, "playAssetUndiscoveredNotification");

        vibrate();

        float volume = _activeConfiguration.getPttToneNotificationLevel();
        if (volume == 0.0) {
            return;
        }

        try {
            Globals.getAudioPlayerManager().playNotification(R.raw.asset_undiscovered, volume, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playNetworkDownNotification() {
        Log.d(TAG, "playNetworkDownNotification");

        vibrate();

        float volume = _activeConfiguration.getErrorToneNotificationLevel();
        if (volume == 0.0) {
            return;
        }

        try {
            Globals.getAudioPlayerManager().playNotification(R.raw.network_down, volume, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playGeneralErrorNotification() {
        Log.d(TAG, "playGeneralErrorNotification");

        vibrate();

        float volume = _activeConfiguration.getErrorToneNotificationLevel();
        if (volume == 0.0) {
            return;
        }

        try {
            Globals.getAudioPlayerManager().playNotification(R.raw.general_error, volume, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean playTxOnNotification(Runnable onPlayComplete) {
        Log.d(TAG, "playTxOnNotification");

        vibrate();

        boolean rc = false;
        float volume = _activeConfiguration.getPttToneNotificationLevel();
        if (volume == 0.0) {
            return rc;
        }

        try {
            Globals.getAudioPlayerManager().playNotification(R.raw.tx_on, volume, onPlayComplete);
            rc = true;
        } catch (Exception e) {
            e.printStackTrace();
            rc = false;
        }

        return rc;
    }

    // TODO:
    public void playTxOffNotification() {
        Log.d(TAG, "playTxOffNotification");
        /*
        float volume = _activeConfiguration.getPttToneNotificationLevel();
        if(volume == 0.0)
        {
            return;
        }

        try
        {
            Globals.getAudioPlayerManager().playNotification(R.raw.tx_on, volume);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        */
    }

    public void restartEngine() {
        Log.d(TAG, "restartEngine");
        stopEngine();
        startEngine();
    }

    public String getCertStoreCacheDir() {
        return Globals.getContext().getFilesDir().getAbsolutePath() + "/" + Globals.getContext().getString(R.string.certstore_cache_dir);
    }

    private String getCustomCertStoreFn() {
        String fn = Globals.getSharedPreferences().getString(PreferenceKeys.USER_CERT_STORE_FILE_NAME, "");
        if (!Utils.isEmptyString(fn)) {
            if (!Utils.doesFileExist(fn)) {
                fn = null;
            }
        }

        /*
        try
        {
            String tmp;

            // See if our preferences tell us what file to use
            tmp = Globals.getSharedPreferences().getString(PreferenceKeys.USER_CERT_STORE_FILE_NAME, "");
            if(Utils.doesFileExist(tmp))
            {
               fn = tmp;
            }
            else
            {
                ArrayList<String> dirs = new ArrayList<>();

                dirs.add(Globals.getContext().getFilesDir().getAbsolutePath());
                dirs.add(Environment.getExternalStorageDirectory().getAbsolutePath());
                dirs.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());

                for (String dirName : dirs)
                {
                    tmp = dirName + "/" + Globals.getContext().getString(R.string.certstore_default_fn);
                    if (Utils.doesFileExist(tmp))
                    {
                        fn = tmp;
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fn = null;
        }
        */

        return fn;

    }

    private byte[] getCustomCertStoreContent() {
        byte[] rc = null;

        try {
            String fn = getCustomCertStoreFn();
            if (!Utils.isEmptyString(fn)) {
                RandomAccessFile raf = new RandomAccessFile(fn, "r");
                rc = new byte[(int) raf.length()];
                raf.readFully(rc);
                raf.close();

                Log.i(TAG, "Auto-importing custom certificate store '" + fn + "'");
            }
        } catch (Exception e) {
            e.printStackTrace();
            rc = null;
        }

        return rc;
    }

    private boolean openCertificateStore() {
        boolean rc = false;

        try {
            byte[] certStoreContent;

            certStoreContent = getCustomCertStoreContent();

            if (certStoreContent == null) {
                certStoreContent = Utils.getBinaryResource(Globals.getContext(), R.raw.android_engage_default_certstore);
                if (certStoreContent == null) {
                    throw new Exception("cannot load binary resource");
                }
            }

            String dir = Globals.getContext().getFilesDir().toString();
            String fn = dir + "/" + Globals.getContext().getString(R.string.certstore_active_fn);

            File fd = new File(fn);
            fd.deleteOnExit();

            FileOutputStream fos = new FileOutputStream(fd);
            fos.write(certStoreContent);
            fos.close();

            Set<String> passwords = getCertificateStoresPasswords();
            for (String pwd : passwords) {
                if (getEngine().engageOpenCertStore(fn, pwd) == 0) {
                    rc = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            rc = false;
        }

        return rc;
    }

    public String applyFlavorSpecificGeneratedMissionModifications(String json) {
        String generatedMissionDescription = getString(R.string.generated_mission_description);
        if (Utils.isEmptyString(generatedMissionDescription)) {
            return json;
        }

        String rc;

        try {
            JSONObject jo = new JSONObject(json);

            jo.put(Engine.JsonFields.Mission.description, generatedMissionDescription);

            rc = jo.toString();
        } catch (Exception e) {
            e.printStackTrace();
            rc = json;
        }

        return rc;
    }

    public String getEnginePolicy() {
        String rc = null;

        try {
            String tmp = Globals.getSharedPreferences().getString(PreferenceKeys.ENGINE_POLICY_JSON, "");
            if (!Utils.isEmptyString(tmp)) {
                JSONObject jo = new JSONObject(tmp);
                rc = jo.toString();
            }
        } catch (Exception e) {
            rc = null;
        }

        if (Utils.isEmptyString(rc)) {
            rc = Utils.getStringResource(this, R.raw.default_engine_policy_template);
        }

        return rc;
    }

    private void createSampleConfiguration() {
        String enginePolicyJson = ActiveConfiguration.makeBaselineEnginePolicyObject(getEnginePolicy()).toString();
        String identityJson = "{}";
        String tempDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();

        getEngine().engageInitialize(enginePolicyJson,
                identityJson,
                tempDirectory);

        String passphrase = BuildConfig.APPLICATION_ID + getString(R.string.manufacturer_id);
        String mn = String.format(getString(R.string.sample_mission_gen_mission_name_fmt), getString(R.string.app_name));
        String missionJson = getEngine().engageGenerateMission(passphrase, Integer.parseInt(getString(R.string.sample_mission_gen_group_count)), "", mn);

        try {
            JSONObject jo = new JSONObject(missionJson);

            // RP
            String rp = getString(R.string.default_rallypoint);
            if (!Utils.isEmptyString(rp)) {
                JSONObject rallypoint = new JSONObject();
                rallypoint.put("use", Utils.boolOpt(getString(R.string.sample_mission_gen_use_default_rallypoint), false));
                rallypoint.put(Engine.JsonFields.Rallypoint.Host.address, rp);
                rallypoint.put(Engine.JsonFields.Rallypoint.Host.port, Utils.intOpt(getString(R.string.default_rallypoint_port), Constants.DEF_RP_PORT));
                jo.put(Engine.JsonFields.Rallypoint.objectName, rallypoint);
            }

            // Group names
            String groupNameFmt = getString(R.string.sample_mission_gen_audio_group_name_fmt);
            if (!Utils.isEmptyString(groupNameFmt)) {
                JSONArray ar = jo.getJSONArray(Engine.JsonFields.Group.arrayName);
                int idx;

                idx = 1;
                for (int x = 0; x < ar.length(); x++) {
                    JSONObject g = ar.getJSONObject(x);
                    int type = g.getInt(Engine.JsonFields.Group.type);
                    if (type == GroupDescriptor.Type.gtAudio.ordinal()) {
                        String gn = String.format(groupNameFmt, idx);
                        g.put(Engine.JsonFields.Group.name, gn);
                        idx++;
                    }
                }
            }

            missionJson = applyFlavorSpecificGeneratedMissionModifications(jo.toString());
        } catch (Exception e) {
            e.printStackTrace();
            missionJson = "";
        }

        Globals.getSharedPreferencesEditor().putString(PreferenceKeys.ACTIVE_MISSION_CONFIGURATION_JSON, missionJson);
        Globals.getSharedPreferencesEditor().apply();

        if (!Utils.isEmptyString(missionJson)) {
            ActiveConfiguration.installMissionJson(null, missionJson, true);
        }

        getEngine().deinitialize();
    }

    public void startEngine() {
        Log.d(TAG, "startEngine");

        try {
            if (!openCertificateStore()) {
                throw new Exception("Throw cannot open certificate store");
            }

            updateActiveConfiguration();

            if (_activeConfiguration == null) {
                createSampleConfiguration();
                updateActiveConfiguration();
            }

            setMissionChangedStatus(false);
            JSONObject policyBaseline = ActiveConfiguration.makeBaselineEnginePolicyObject(getEnginePolicy());

            String enginePolicyJson = getActiveConfiguration().makeEnginePolicyObjectFromBaseline(policyBaseline).toString();
            String identityJson = getActiveConfiguration().makeIdentityObject().toString();
            String tempDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();

            getEngine().engageInitialize(enginePolicyJson,
                    identityJson,
                    tempDirectory);

            getEngine().engageStart();

            _hasEngineBeenInitialized = true;
        } catch (Exception e) {
            e.printStackTrace();
            stop();
        }
    }

    public void stopEngine() {
        try {
            leaveAllGroups();
            stopLocationUpdates();
            _engineRunning = false;
            getEngine().engageStop();
            getEngine().engageShutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ActiveConfiguration updateActiveConfiguration() {
        Log.d(TAG, "updateActiveConfiguration");
        _activeConfiguration = Utils.loadConfiguration(_activeConfiguration, _dynamicGroups);

        if (_activeConfiguration != null) {
            if (!_activeConfiguration.getDiscoverSsdpAssets()
                    && !_activeConfiguration.getDiscoverCistechGv1Assets()
                    && !_activeConfiguration.getDiscoverTrelliswareAssets()) {
                if (!_dynamicGroups.isEmpty()) {
                    _dynamicGroups.clear();
                    _activeConfiguration = Utils.loadConfiguration(_activeConfiguration, null);
                }
            }

            ArrayList<String> toBeTrashed = new ArrayList<>();
            ArrayList<GroupDescriptor> groups = _activeConfiguration.getMissionGroups();
            for (String id : _groupConnections.keySet()) {
                boolean found;

                found = false;
                for (GroupDescriptor gd : groups) {
                    if (gd.id.compareTo(id) == 0) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    toBeTrashed.add(id);
                }
            }

            for (String id : toBeTrashed) {
                eraseGroupConnectionState(id);
            }

            restartStartHumanBiometricsReporting();
            restartDeviceMonitoring();
        }

        return _activeConfiguration;
    }

    public ActiveConfiguration getActiveConfiguration() {
        return _activeConfiguration;
    }

    private void runPreflightCheck() {
        Log.d(TAG, "runPreflightCheck");

        {
            String mnf = Build.MANUFACTURER;
            String brand = Build.BRAND;
            String model = Build.MODEL;

            Log.i(TAG, "mnf=" + mnf + ", brand=" + brand + ", model=" + model);
        }

        // See if we're running for the first time
        boolean wasRunPreviously = (Globals.getSharedPreferences().getBoolean(PreferenceKeys.APP_FIRST_TIME_RUN, false) == true);

        String val;

        // Do we want to enforce PTT latching
        if (!wasRunPreviously) {
            Globals.getSharedPreferencesEditor().putBoolean(PreferenceKeys.USER_UI_PTT_LATCHING, Utils.boolOpt(getString(R.string.opt_ptt_latching), false));
            Globals.getSharedPreferencesEditor().apply();

            // Install a burnt-in license if we have one and we don't already have one scanned in
            val = Globals.getSharedPreferences().getString(PreferenceKeys.USER_LICENSING_KEY, null);
            if (Utils.isEmptyString(val)) {
                val = getString(R.string.license_key);
                if (!Utils.isEmptyString(val)) {
                    Globals.getSharedPreferencesEditor().putString(PreferenceKeys.USER_LICENSING_KEY, val);

                    val = getString(R.string.activation_code);
                    if (Utils.isEmptyString(val)) {
                        val = "";
                    }

                    Globals.getSharedPreferencesEditor().putString(PreferenceKeys.USER_LICENSING_ACTIVATION_CODE, val);
                    Globals.getSharedPreferencesEditor().apply();
                }
            }
        }

        // Make sure we have a mission database - even if it's empty
        MissionDatabase database = MissionDatabase.load(Globals.getSharedPreferences(), Constants.MISSION_DATABASE_NAME);
        if (database == null) {
            database = new MissionDatabase();
            database.save(Globals.getSharedPreferences(), Constants.MISSION_DATABASE_NAME);
        }

        // We'll need a network interface for binding
        val = Globals.getSharedPreferences().getString(PreferenceKeys.NETWORK_BINDING_NIC_NAME, null);
        if (Utils.isEmptyString(val)) {
            NetworkInterface ni = Utils.getFirstViableMulticastNetworkInterface();
            if (ni != null) {
                val = ni.getName();
                Globals.getSharedPreferencesEditor().putString(PreferenceKeys.NETWORK_BINDING_NIC_NAME, val);
                Globals.getSharedPreferencesEditor().apply();
            }
        }

        // See if we have a node id.  If not, make one
        val = Globals.getSharedPreferences().getString(PreferenceKeys.USER_NODE_ID, null);
        if (Utils.isEmptyString(val)) {
            val = Utils.generateUserNodeId();
            Globals.getSharedPreferencesEditor().putString(PreferenceKeys.USER_NODE_ID, val);
            Globals.getSharedPreferencesEditor().apply();
        }

        // See if we have an active configuration.  If we don't we'll make one
        /*
        val = Globals.getSharedPreferences().getString(PreferenceKeys.ACTIVE_MISSION_CONFIGURATION_JSON, null);
        if(Utils.isEmptyString(val))
        {
            // Make the sample mission
            Utils.generateSampleMission(this);

            // Load it
            ActiveConfiguration ac = Utils.loadConfiguration(null);

            // Get it from our database
            DatabaseMission mission = database.getMissionById(ac.getMissionId());
            if(mission == null)
            {
                // It doesn't exist - add it
                if( database.addOrUpdateMissionFromActiveConfiguration(ac) )
                {
                    database.save(Globals.getSharedPreferences(), Constants.MISSION_DATABASE_NAME);
                }
            }
        }
        */

        updateActiveConfiguration();

        // We have run for the first time (actually every time) but that's OK
        Globals.getSharedPreferencesEditor().putBoolean(PreferenceKeys.APP_FIRST_TIME_RUN, true);
        Globals.getSharedPreferencesEditor().apply();
    }


    public Engine getEngine() {
        return (_svc != null ? _svc.getEngine() : null);
    }

    public boolean isServiceOnline() {
        return (_svc != null);
    }

    private void notifyGroupUiListeners(GroupDescriptor gd) {
        synchronized (_uiUpdateListeners) {
            for (IUiUpdateListener listener : _uiUpdateListeners) {
                listener.onGroupUiRefreshNeeded(gd);
            }
        }
    }

    public final void runOnUiThread(Runnable action) {
        if (Thread.currentThread() != getMainLooper().getThread()) {
            new Handler(Looper.getMainLooper()).post(action);
        } else {
            action.run();
        }
    }

    private HashSet<GroupDescriptor> _groupsSelectedForTx = new HashSet<>();

    public void startTx(final int priority, final int flags) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (_groupsSelectedForTx) {
                        if (!_groupsSelectedForTx.isEmpty()) {
                            Log.e(TAG, "attempt to begin tx while there is already a pending/active tx");
                            playGeneralErrorNotification();
                            return;
                        }

                        ArrayList<GroupDescriptor> selected = getActiveConfiguration().getSelectedGroups();
                        if (selected.isEmpty()) {
                            playGeneralErrorNotification();
                            return;
                        }

                        for (GroupDescriptor gd : selected) {
                            _groupsSelectedForTx.add(gd);
                        }

                        boolean anyGroupToTxOn = false;
                        for (GroupDescriptor g : _groupsSelectedForTx) {
                            if (getActiveConfiguration().getUiMode() == Constants.UiMode.vSingle ||
                                    (getActiveConfiguration().getUiMode() == Constants.UiMode.vMulti) && (!g.txMuted)) {
                                anyGroupToTxOn = true;
                                g.txPending = true;
                                break;
                            }
                        }

                        if (!anyGroupToTxOn) {
                            _groupsSelectedForTx.clear();
                            playGeneralErrorNotification();
                            return;
                        }

                        // Start TX - in TX muted mode!!
                        synchronized (_groupsSelectedForTx) {
                            if (!_groupsSelectedForTx.isEmpty()) {
                                if (_groupsSelectedForTx.size() == 1) {
                                    logEvent(Analytics.GROUP_TX_REQUESTED_SINGLE);
                                } else {
                                    logEvent(Analytics.GROUP_TX_REQUESTED_MULTIPLE);
                                }

                                for (GroupDescriptor g : _groupsSelectedForTx) {
                                    if (getActiveConfiguration().getUiMode() == Constants.UiMode.vSingle ||
                                            (getActiveConfiguration().getUiMode() == Constants.UiMode.vMulti) && (!g.txMuted)) {
                                        MissionDatabase db = MissionDatabase.load(Globals.getSharedPreferences(), Constants.MISSION_DATABASE_NAME);

                                        Log.i("Grupos ", "Lo9s grupos");
                                        db._missions.forEach(mission -> mission._groups.forEach(group -> {
                                            Log.i(group._id, g.id);
                                            if (group._id.equals(g.id)) {
                                                Log.i("ENCONTRE EL GRUPO ! %s", g.id);
                                            }
                                        }));

                                        getEngine().engageBeginGroupTxAdvanced(g.id, buildAdvancedTxJson(flags, priority, 0, true, _activeConfiguration.getUserAlias()));
                                    }
                                }
                            } else {
                                Log.w(TAG, "tx task runnable found no groups to tx on");
                            }
                        }

                        // TODO: we're already in a sync, now going into another.  is this dangerous
                        // considering what we're calling (maybe not...?)
                        synchronized (_uiUpdateListeners) {
                            for (IUiUpdateListener listener : _uiUpdateListeners) {
                                listener.onAnyTxPending();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void startTxLegba(final int priority, final int flags, String... groupIds) {

        ActiveConfiguration activeConfiguration = getActiveConfiguration();

        runOnUiThread(() -> {
            synchronized (groupIds) {
                for (String id : groupIds) {
                    String currentMissionId = activeConfiguration.getMissionId();

                    Timber.i("CurrentMissionId -> %s ", currentMissionId);
                    Timber.i("CurrentMissionName ->%s ", activeConfiguration.getMissionName());
                    Timber.i("CurrentMissionGroupId -> %s", id);

                    getEngine().engageBeginGroupTxAdvanced(id, buildAdvancedTxJson(flags, priority, 0, true, _activeConfiguration.getUserAlias()));
                }
            }
        });
    }

    public void endTxLega(String... groupIds) {
        runOnUiThread(() -> {
            for (String id : groupIds) {
                getEngine().engageEndGroupTx(id);
            }
        });
    }

    public void endTx() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    // We'll just end transmit on everything
                    for (GroupDescriptor gd : _activeConfiguration.getMissionGroups()) {
                        if (gd.type == GroupDescriptor.Type.gtAudio) {
                            getEngine().engageEndGroupTx(gd.id);
                        }
                    }

                    synchronized (_groupsSelectedForTx) {
                        _groupsSelectedForTx.clear();
                    }

                    synchronized (_uiUpdateListeners) {
                        for (IUiUpdateListener listener : _uiUpdateListeners) {
                            listener.onAnyTxEnding();
                        }
                    }

                    checkIfAnyTxStillActiveAndNotify();

                    /*
                    synchronized (_groupsSelectedForTx)
                    {
                        if (!_groupsSelectedForTx.isEmpty())
                        {
                            for (GroupDescriptor g : _groupsSelectedForTx)
                            {
                                getEngine().engageEndGroupTx(g.id);
                            }

                            _groupsSelectedForTx.clear();

                            // TODO: only play tx off notification if something was already in a TX state of some sort
                            playTxOffNotification();
                        }
                        else
                        {
                            Log.w(TAG, "#SB# - endTx but no groups selected for tx");

                        }

                        synchronized (_uiUpdateListeners)
                        {
                            for (IUiUpdateListener listener : _uiUpdateListeners)
                            {
                                listener.onAnyTxEnding();
                            }
                        }

                        checkIfAnyTxStillActiveAndNotify();
                    }
                    */
                } catch (Exception e) {
                    e.printStackTrace();
                }
    
            /*
            if(_txPending)
            {
                _txPending = false;
                Log.i(TAG, "cancelling previous tx pending");
                cancelPreviousTxPending();
            }
            */
            }
        });
    }

    private void goIdle() {
        stopHardwareButtonManager();
        stopGroupHealthCheckTimer();
        stopLocationUpdates();
    }

    public boolean isEngineRunning() {
        return _engineRunning;
    }

    private void checkIfAnyTxStillActiveAndNotify() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (_groupsSelectedForTx) {
                    boolean anyStillActive = (!_groupsSelectedForTx.isEmpty());

                    // Safety check
                    for (GroupDescriptor testGroup : _activeConfiguration.getMissionGroups()) {
                        if (!_groupsSelectedForTx.contains(testGroup)) {
                            /*
                            if (testGroup.tx || testGroup.txPending)
                            {
                                Log.wtf(TAG, "data model says group is tx or txPending but the group is not in the tx set!!");
                            }
                            */

                            testGroup.tx = false;
                            testGroup.txPending = false;
                        }
                    }

                    if (!anyStillActive) {
                        synchronized (_uiUpdateListeners) {
                            for (IUiUpdateListener listener : _uiUpdateListeners) {
                                listener.onAllTxEnded();
                            }
                        }
                    }
                }
            }
        });
    }

    public void initiateMissionDownload(final Activity activity) {
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View promptView = layoutInflater.inflate(R.layout.download_mission_url_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setView(promptView);

        final EditText etPassword = promptView.findViewById(R.id.etPassword);
        final EditText etUrl = promptView.findViewById(R.id.etUrl);

        //etUrl.setText("https://s3.us-east-2.amazonaws.com/rts-missions/{a50f4cfb-f200-4cf0-8f88-0401585ba034}.json");

        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(R.string.mission_download_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        downloadAndSwitchToMission(etUrl.getText().toString(), etPassword.getText().toString());
                    }
                })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void downloadAndSwitchToMission(final String url, final String password) {
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                try {
                    int responseCode = msg.arg1;
                    String resultMsg = msg.getData().getString(DownloadMissionTask.BUNDLE_RESULT_MSG);
                    String resultData = msg.getData().getString(DownloadMissionTask.BUNDLE_RESULT_DATA);

                    if (responseCode >= 200 && responseCode <= 299) {
                        processDownloadedMissionAndSwitchIfOk(resultData, password);
                    } else {
                        Toast.makeText(EngageApplication.this, "Mission download failed - " + resultMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(EngageApplication.this, "Mission download failed with exception " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        };

        DownloadMissionTask dmt = new DownloadMissionTask(handler);
        dmt.execute(url);
    }

    public void processDownloadedMissionAndSwitchIfOk(final String missionData,
                                                      final String password) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    ActiveConfiguration ac = new ActiveConfiguration();
                    if (!ac.parseTemplate(missionData)) {
                        throw new Exception("Invalid mission data");
                    }

                    saveAndActivateConfiguration(ac);

                    Toast.makeText(EngageApplication.this, ac.getMissionName() + " processed", Toast.LENGTH_LONG).show();

                    synchronized (_configurationChangeListeners) {
                        for (IConfigurationChangeListener listener : _configurationChangeListeners) {
                            listener.onMissionChanged();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(EngageApplication.this, "Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public int getQrCodeScannerRequestCode() {
        return IntentIntegrator.REQUEST_CODE;
    }

    public void initiateMissionQrCodeScan(final Activity activity) {
        // Clear any left-over password
        Globals.getSharedPreferencesEditor().putString(PreferenceKeys.QR_CODE_SCAN_PASSWORD, null);
        Globals.getSharedPreferencesEditor().apply();

        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View promptView = layoutInflater.inflate(R.layout.qr_code_mission_scan_password_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setView(promptView);

        final EditText editText = promptView.findViewById(R.id.etPassword);

        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(R.string.qr_code_scan_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Save the password so the scanned intent result can get it later
                        Globals.getSharedPreferencesEditor().putString(PreferenceKeys.QR_CODE_SCAN_PASSWORD, editText.getText().toString());
                        Globals.getSharedPreferencesEditor().apply();

                        invokeQrCodeScanner(activity);
                    }
                })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public void initiateSimpleQrCodeScan(Activity activity) {
        invokeQrCodeScanner(activity);
    }

    private void invokeQrCodeScanner(Activity activity) {
        IntentIntegrator ii = new IntentIntegrator(activity);

        ii.setCaptureActivity(OrientationIndependentQrCodeScanActivity.class);
        ii.setPrompt(getString(R.string.qr_scan_prompt));
        ii.setBeepEnabled(true);
        ii.setOrientationLocked(false);
        ii.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        ii.setBarcodeImageEnabled(true);
        ii.setTimeout(10000);
        ii.initiateScan();
    }

    private void saveAndActivateConfiguration(ActiveConfiguration ac) {
        try {
            // Template
            Globals.getSharedPreferencesEditor().putString(PreferenceKeys.ACTIVE_MISSION_CONFIGURATION_JSON, ac.getInputJson());
            Globals.getSharedPreferencesEditor().apply();

            // See if any groups have rallypoints.  If any do, then we'll use the first one we find as the RP for the whole mission
            //JSONObject jMission = new JSONObject(ac.getInputJson());
            //JSONObject jGroups = jMission.get("groups")

            // Add this guy to our mission database
            MissionDatabase database = MissionDatabase.load(Globals.getSharedPreferences(), Constants.MISSION_DATABASE_NAME);
            if (database != null) {
                if (database.addOrUpdateMissionFromActiveConfiguration(ac)) {
                    database.save(Globals.getSharedPreferences(), Constants.MISSION_DATABASE_NAME);
                } else {
                    // TODO: how do we let the user know that we could not save into our database ??
                }
            }

            // Our mission has changed
            setMissionChangedStatus(true);
        } catch (Exception e) {
            e.printStackTrace();
            Utils.showLongPopupMsg(EngageApplication.this, e.getMessage());
        }
    }

    public ActiveConfiguration processScannedQrCode(String scannedString, String pwd) throws
            Exception {
        ActiveConfiguration ac = ActiveConfiguration.parseEncryptedQrCodeString(scannedString, pwd);

        saveAndActivateConfiguration(ac);

        return ac;
    }

    public ActiveConfiguration processScannedQrCodeResultIntent(int requestCode,
                                                                int resultCode, Intent intent) throws Exception {
        // Grab any password that may have been stored for our purposes
        String pwd = Globals.getSharedPreferences().getString(PreferenceKeys.QR_CODE_SCAN_PASSWORD, "");

        // And clear it
        Globals.getSharedPreferencesEditor().putString(PreferenceKeys.QR_CODE_SCAN_PASSWORD, null);
        Globals.getSharedPreferencesEditor().apply();

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result == null) {
            return null;
        }

        // Get the string we scanned
        String scannedString = result.getContents();

        if (Utils.isEmptyString(scannedString)) {
            throw new SimpleMessageException(getString(R.string.qr_scan_cancelled));
        }

        return processScannedQrCode(scannedString, pwd);
    }

    public boolean switchToMission(String id) {
        boolean rc;

        try {
            MissionDatabase database = MissionDatabase.load(Globals.getSharedPreferences(), Constants.MISSION_DATABASE_NAME);
            DatabaseMission mission = database.getMissionById(id);
            if (mission == null) {
                throw new Exception("WTF, no mission by this ID");
            }

            ActiveConfiguration ac = ActiveConfiguration.loadFromDatabaseMission(mission);

            if (ac == null) {
                throw new Exception("boom!");
            }

            String serializedAc = ac.makeTemplate().toString();
            SharedPreferences.Editor ed = Globals.getSharedPreferencesEditor();
            ed.putString(PreferenceKeys.ACTIVE_MISSION_CONFIGURATION_JSON, serializedAc);
            ed.apply();

            rc = true;
        } catch (Exception e) {
            rc = false;
        }

        return rc;
    }

    // Handlers, listeners, and overrides ========================================
    @Override
    public void onBluetoothDeviceConnected() {
        Log.d(TAG, "onBluetoothDeviceConnected");
    }

    @Override
    public void onBluetoothDeviceDisconnected() {
        Log.d(TAG, "onBluetoothDeviceDisconnected");
    }

    @Override
    public void requestPttOn(int priority, int flags) {
        startTx(priority, flags);
    }

    @Override
    public void requestPttOff() {
        endTx();
    }

    @Override
    public void onEngineStarted(String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logEvent(Analytics.ENGINE_STARTED);

                Log.d(TAG, "onEngineStarted");
                _engineRunning = true;
//                createAllGroupObjects();
//                joinSelectedGroups();
//                startLocationUpdates(); // Bussiness logic
//                startHardwareButtonManager(); // Bussiness logic
            }
        });
    }

    @Override
    public void onEngineStartFailed(String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logEvent(Analytics.ENGINE_START_FAILED);

                Log.e(TAG, "onEngineStartFailed");
                _engineRunning = false;
                stop();
            }
        });
    }

    @Override
    public void onEngineStopped(String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logEvent(Analytics.ENGINE_STOPPED);

                Log.d(TAG, "onEngineStopped");
                _engineRunning = false;
                goIdle();

                if (_terminateOnEngineStopped) {
                    _terminateOnEngineStopped = false;
                    getEngine().engageShutdown();

                    clearOutServiceOperation();

                    _terminatingActivity.moveTaskToBack(true);
                    _terminatingActivity.finishAndRemoveTask();

                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }
            }
        });
    }

    @Override
    public void onGroupCreated(final String id, final String eventExtraJson) {
        runOnUiThread(() -> {
            Timber.i("onGroupCreated %s", id);
            Globals.getEngageApplication().getEngine().engageJoinGroup(id);
        });
    }

    @Override
    public void onGroupCreateFailed(final String id, final String eventExtraJson) {
        runOnUiThread(() -> {
            Timber.i("onGroupCreatedFailed %s", id);
        });
    }

    @Override
    public void onGroupDeleted(final String id, final String eventExtraJson) {
        runOnUiThread(() -> {
            Timber.i(TAG, "onGroupDeleted %s", id);
        });
    }

    @Override
    public void onGroupConnected(final String id, final String eventExtraJson) {
        runOnUiThread(() -> {
            GroupDescriptor gd = getGroup(id);
            if (gd == null)
            {
                Log.e(TAG, "onGroupConnected: cannot find group id='" + id + "'");
                return;
            }

            Log.d(TAG, "onGroupConnected: id='" + id + "', n='" + gd.name + "', x=" + eventExtraJson);

            try
            {
                if (!Utils.isEmptyString(eventExtraJson))
                {
                    JSONObject eej = new JSONObject(eventExtraJson);
                    JSONObject gcd = eej.optJSONObject(Engine.JsonFields.GroupConnectionDetail.objectName);
                    if (gcd != null)
                    {
                        GroupConnectionTrackerInfo gts = getGroupConnectionState(id);

                        Engine.ConnectionType ct = Engine.ConnectionType.fromInt(gcd.optInt(Engine.JsonFields.GroupConnectionDetail.connectionType));
                        if (ct == Engine.ConnectionType.ipMulticast)
                        {
                            if (gcd.optBoolean(Engine.JsonFields.GroupConnectionDetail.asFailover, false))
                            {
                                logEvent(Analytics.GROUP_CONNECTED_MC_FAILOVER);
                            }
                            else
                            {
                                logEvent(Analytics.GROUP_CONNECTED_MC);
                            }

                            gts.hasMulticastConnection = true;
                        }
                        else if (ct == Engine.ConnectionType.rallypoint)
                        {
                            logEvent(Analytics.GROUP_CONNECTED_RP);
                            gts.hasRpConnection = true;
                        }

                        gts.operatingInMulticastFailover = gcd.optBoolean(Engine.JsonFields.GroupConnectionDetail.asFailover, false);

                        setGroupConnectionState(id, gts);
                    }
                    else
                    {
                        logEvent(Analytics.GROUP_CONNECTED_OTHER);
                    }
                }
                else
                {
                    logEvent(Analytics.GROUP_CONNECTED_OTHER);
                }
            }
            catch (Exception e)
            {
                logEvent(Analytics.GROUP_CONNECTED_OTHER);

                // If we have no specializer, assume the following (the Engine should always tell us though)
                setGroupConnectionState(id, true, false, true);
            }

            // If we get connected to a presence group ...
            if (gd.type == GroupDescriptor.Type.gtPresence)
            {
                // TODO: If we have multiple presence groups, this will generate extra traffic

                // Build whatever PD we currently have and send it
                sendUpdatedPd(buildPd());
            }

            notifyGroupUiListeners(gd);
        });
    }

    @Override
    public void onGroupConnectFailed(final String id, final String eventExtraJson) {
        runOnUiThread(() -> {
            GroupDescriptor gd = getGroup(id);

            Log.d(TAG, "onGroupConnectFailed: id='" + id + "', , x=" + eventExtraJson);

            try {
                if (!Utils.isEmptyString(eventExtraJson)) {
                    JSONObject eej = new JSONObject(eventExtraJson);
                    JSONObject gcd = eej.optJSONObject(Engine.JsonFields.GroupConnectionDetail.objectName);
                    if (gcd != null) {
                        GroupConnectionTrackerInfo gts = getGroupConnectionState(id);

                        Engine.ConnectionType ct = Engine.ConnectionType.fromInt(gcd.optInt(Engine.JsonFields.GroupConnectionDetail.connectionType));
                        if (ct == Engine.ConnectionType.ipMulticast) {
                            if (gcd.optBoolean(Engine.JsonFields.GroupConnectionDetail.asFailover, false)) {
                                logEvent(Analytics.GROUP_CONNECT_FAILED_MC_FAILOVER);
                            } else {
                                logEvent(Analytics.GROUP_CONNECT_FAILED_MC);
                            }

                            gts.hasMulticastConnection = false;
                        } else if (ct == Engine.ConnectionType.rallypoint) {
                            logEvent(Analytics.GROUP_CONNECT_FAILED_RP);
                            gts.hasRpConnection = false;
                        }

                        gts.operatingInMulticastFailover = false;

                        setGroupConnectionState(id, gts);
                    } else {
                        logEvent(Analytics.GROUP_CONNECT_FAILED_OTHER);
                    }
                } else {
                    logEvent(Analytics.GROUP_CONNECT_FAILED_OTHER);
                }
            } catch (Exception e) {
                logEvent(Analytics.GROUP_CONNECT_FAILED_OTHER);

                // If we have no specializer, assume the following (the Engine should always tell us though)
                eraseGroupConnectionState(id);
            }

            notifyGroupUiListeners(gd);
        });
    }

    @Override
    public void onGroupDisconnected(final String id, final String eventExtraJson) {
        runOnUiThread(() -> {
            GroupDescriptor gd = getGroup(id);
            if (gd == null) {
                Log.e(TAG, "onGroupDisconnected: cannot find group id='" + id + "'");
                return;
            }

            Log.d(TAG, "onGroupDisconnected: id='" + id + "', n='" + gd.name + "', x=" + eventExtraJson);

            try {
                if (!Utils.isEmptyString(eventExtraJson)) {
                    JSONObject eej = new JSONObject(eventExtraJson);
                    JSONObject gcd = eej.optJSONObject(Engine.JsonFields.GroupConnectionDetail.objectName);
                    if (gcd != null) {
                        GroupConnectionTrackerInfo gts = getGroupConnectionState(id);

                        Engine.ConnectionType ct = Engine.ConnectionType.fromInt(gcd.optInt(Engine.JsonFields.GroupConnectionDetail.connectionType));
                        if (ct == Engine.ConnectionType.ipMulticast) {
                            if (gcd.optBoolean(Engine.JsonFields.GroupConnectionDetail.asFailover, false)) {
                                logEvent(Analytics.GROUP_DISCONNECTED_MC_FAILOVER);
                            } else {
                                logEvent(Analytics.GROUP_DISCONNECTED_MC);
                            }

                            gts.hasMulticastConnection = false;
                        } else if (ct == Engine.ConnectionType.rallypoint) {
                            logEvent(Analytics.GROUP_DISCONNECTED_RP);
                            gts.hasRpConnection = false;
                        }

                        gts.operatingInMulticastFailover = false;

                        setGroupConnectionState(id, gts);
                    } else {
                        logEvent(Analytics.GROUP_DISCONNECTED_OTHER);
                    }
                } else {
                    logEvent(Analytics.GROUP_DISCONNECTED_OTHER);
                }
            } catch (Exception e) {
                logEvent(Analytics.GROUP_DISCONNECTED_OTHER);

                // If we have no specializer, assume the following (the Engine should always tell us though)
                setGroupConnectionState(id, false, false, false);
            }

            notifyGroupUiListeners(gd);
        });
    }

    @Override
    public void onGroupJoined(final String id, final String eventExtraJson) {
        runOnUiThread(() -> {
            logEvent(Analytics.GROUP_JOINED);

            /*GroupDescriptor gd = getGroup(id);
            if (gd == null) {
                Log.e(TAG, "onGroupJoined: cannot find group id='" + id + "'");
                return;
            }*/

            Timber.d("onGroupJoined: id='%s'", id);

            /*gd.joined = true;
            gd.joinError = false;

            notifyGroupUiListeners(gd);*/
        });
    }

    @Override
    public void onGroupJoinFailed(final String id, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logEvent(Analytics.GROUP_JOIN_FAILED);

                GroupDescriptor gd = getGroup(id);
                if (gd == null) {
                    Log.e(TAG, "onGroupJoinFailed: cannot find group id='" + id + "'");
                    return;
                }

                Log.e(TAG, "onGroupJoinFailed: id='" + id + "', n='" + gd.name + "'");

                gd.resetState();
                gd.joinError = true;

                notifyGroupUiListeners(gd);
            }
        });
    }

    @Override
    public void onGroupLeft(final String id, final String eventExtraJson) {
        runOnUiThread(() -> {
            Timber.i("onGroupLeft %s", id);
        });
    }

    @Override
    public void onGroupMemberCountChanged(final String id, final long newCount,
                                          final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //logEvent(Analytics.GROUP_MEMBER_COUNT_CHANGED);

                GroupDescriptor gd = getGroup(id);
                if (gd == null) {
                    Log.e(TAG, "onGroupMemberCountChanged: cannot find group id='" + id + "'");
                    return;
                }

                Log.d(TAG, "onGroupMemberCountChanged: id='" + id + "', n=" + gd.name + ", c=" + newCount);

                notifyGroupUiListeners(gd);
            }
        });
    }

    @Override
    public void onGroupRxStarted(final String id, final String eventExtraJson) {
        runOnUiThread(() -> {
            logEvent(Analytics.GROUP_RX_STARTED);

            Timber.i("onGroupRxStarted %s", id);



            long now = Utils.nowMs();
            if ((now - _lastAudioActivity) > (Constants.RX_IDLE_SECS_BEFORE_NOTIFICATION * 1000)) {
                if (_activeConfiguration.getNotifyOnNewAudio()) {
                    float volume = _activeConfiguration.getNotificationToneNotificationLevel();
                    if (volume != 0.0) {
                        try {
                            Globals.getAudioPlayerManager().playNotification(R.raw.incoming_rx, volume, null);
                        } catch (Exception e) {
                        }
                    }
                }
            }
            _lastAudioActivity = now;

            //notifyGroupUiListeners(gd);
        });
    }

    @Override
    public void onGroupRxEnded(final String id, final String eventExtraJson) {


        try {
            Globals.notifyListenersStop(id, eventExtraJson);
        } catch (Exception e) {
            e.printStackTrace();
        }


        runOnUiThread(() -> {
            logEvent(Analytics.GROUP_RX_ENDED);

            GroupDescriptor gd = getGroup(id);
            if (gd == null) {
                Log.e(TAG, "onGroupRxEnded: cannot find group id='" + id + "'");
                return;
            }

            Log.d(TAG, "onGroupRxEnded: id='" + id + "', n='" + gd.name + "'");

            gd.rx = false;
            _lastAudioActivity = Utils.nowMs();

            notifyGroupUiListeners(gd);
        });
    }

    @Override
    public void onGroupRxSpeakersChanged(final String id, final String groupTalkerJson,
                                         final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //logEvent(Analytics.GROUP_RX_SPEAKER_COUNT_CHANGED);

                Timber.i("onGroupRxSpeakersChanged: id='%s'", id);

                ArrayList<TalkerDescriptor> talkers = null;

                if (!Utils.isEmptyString(groupTalkerJson)) {
                    try {
                        JSONObject root = new JSONObject(groupTalkerJson);
                        JSONArray list = root.getJSONArray(Engine.JsonFields.GroupTalkers.list);
                        if (list != null && list.length() > 0) {
                            for (int x = 0; x < list.length(); x++) {
                                JSONObject obj = list.getJSONObject(x);
                                TalkerDescriptor td = new TalkerDescriptor();
                                td.alias = obj.optString(Engine.JsonFields.TalkerInformation.alias);
                                td.nodeId = obj.optString(Engine.JsonFields.TalkerInformation.nodeId);
                                if (talkers == null) {
                                    talkers = new ArrayList<>();
                                }

                                PresenceDescriptor pd = _activeConfiguration.getPresenceDescriptor(td.nodeId);
                                String displayName = td.alias;
                                if (pd != null) {
                                    displayName = pd.displayName;
                                }
                                boolean isSos = obj.optInt(Engine.JsonFields.TalkerInformation.rxFlags) == 1;
                                Globals.notifyListenersStart(id, td.alias, displayName, isSos);

                                talkers.add(td);
                            }
                        }
                    } catch (Exception e) {
                        if (talkers != null) {
                            talkers.clear();
                            talkers = null;
                        }

                        e.printStackTrace();
                    }
                }

                //gd.updateTalkers(talkers);
                _lastAudioActivity = Utils.nowMs();

                //notifyGroupUiListeners(gd);
            }
        });
    }

    @Override
    public void onGroupRxMuted(final String id, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logEvent(Analytics.GROUP_RX_MUTED);

                GroupDescriptor gd = getGroup(id);
                if (gd == null) {
                    Log.e(TAG, "onGroupRxMuted: cannot find group id='" + id + "'");
                    return;
                }

                Log.d(TAG, "onGroupRxMuted: id='" + id + "', n='" + gd.name + "'");

                gd.rxMuted = true;

                notifyGroupUiListeners(gd);
            }
        });
    }

    @Override
    public void onGroupRxUnmuted(final String id, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logEvent(Analytics.GROUP_RX_UNMUTED);

                GroupDescriptor gd = getGroup(id);
                if (gd == null) {
                    Log.e(TAG, "onGroupRxUnmuted: cannot find group id='" + id + "'");
                    return;
                }

                Log.d(TAG, "onGroupRxUnmuted: id='" + id + "', n='" + gd.name + "'");

                gd.rxMuted = false;

                notifyGroupUiListeners(gd);
            }
        });
    }

    @Override
    public void onGroupTxStarted(final String id, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logEvent(Analytics.GROUP_TX_STARTED);

                final GroupDescriptor gd = getGroup(id);
                if (gd == null) {
                    Log.e(TAG, "onGroupTxStarted: cannot find group id='" + id + "'");
                    return;
                }

                Log.d(TAG, "onGroupTxStarted: id='" + id + "', n='" + gd.name + "', x=" + eventExtraJson);

                // Run this task either right away or after we've played our grant tone
                Runnable txTask = new Runnable() {
                    @Override
                    public void run() {
                        gd.tx = true;
                        gd.txPending = false;
                        gd.txError = false;
                        gd.txUsurped = false;
                        gd.lastTxStartTime = Utils.nowMs();

                        // Our TX is always starting in mute, so unmute it here if we're not (still) playing a sound
                        if (_delayTxUnmuteToCaterForSoundPropogation) {
                            Timer tmr = new Timer();
                            tmr.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    getEngine().engageUnmuteGroupTx(id);
                                }
                            }, Constants.TX_UNMUTE_DELAY_MS_AFTER_GRANT_TONE);
                        } else {
                            getEngine().engageUnmuteGroupTx(id);
                        }

                        _lastAudioActivity = Utils.nowMs();

                        notifyGroupUiListeners(gd);

                        synchronized (_uiUpdateListeners) {
                            for (IUiUpdateListener listener : _uiUpdateListeners) {
                                listener.onAnyTxActive();
                            }
                        }
                    }
                };

                long now = Utils.nowMs();

                if (_activeConfiguration.getNotifyPttEveryTime() ||
                        ((now - _lastTxActivity) > (Constants.TX_IDLE_SECS_BEFORE_NOTIFICATION * 1000))) {
                    _lastTxActivity = now;
                    _delayTxUnmuteToCaterForSoundPropogation = true;
                    if (!playTxOnNotification(txTask)) {
                        txTask.run();
                    }
                } else {
                    _lastTxActivity = now;
                    _delayTxUnmuteToCaterForSoundPropogation = true;
                    vibrate();
                    txTask.run();
                }
            }
        });
    }

    @Override
    public void onGroupTxEnded(final String id, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logEvent(Analytics.GROUP_TX_ENDED);

                GroupDescriptor gd = getGroup(id);
                if (gd == null) {
                    Log.e(TAG, "onGroupTxEnded: cannot find group id='" + id + "'");
                    return;
                }

                Log.d(TAG, "onGroupTxEnded: id='" + id + "', n='" + gd.name + "', x=" + eventExtraJson);

                if (gd.lastTxStartTime > 0) {
                    long txDuration = (Utils.nowMs() - gd.lastTxStartTime);
                    logEvent(Analytics.GROUP_TX_DURATION, "ms", txDuration);
                }

                gd.tx = false;
                gd.txPending = false;
                gd.txError = false;
                gd.lastTxStartTime = 0;

                _lastAudioActivity = Utils.nowMs();

                synchronized (_groupsSelectedForTx) {
                    _groupsSelectedForTx.remove(gd);
                    notifyGroupUiListeners(gd);
                    checkIfAnyTxStillActiveAndNotify();
                }
            }
        });
    }

    @Override
    public void onGroupTxFailed(final String id, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logEvent(Analytics.GROUP_TX_FAILED);

                GroupDescriptor gd = getGroup(id);
                if (gd == null) {
                    Log.e(TAG, "onGroupTxFailed: cannot find group id='" + id + "'");
                    return;
                }

                Log.d(TAG, "onGroupTxFailed: id='" + id + "', n='" + gd.name + "', x=" + eventExtraJson);

                gd.tx = false;
                gd.txPending = false;
                gd.txError = true;
                gd.txUsurped = false;

                _lastAudioActivity = Utils.nowMs();

                synchronized (_groupsSelectedForTx) {
                    _groupsSelectedForTx.remove(gd);
                    playGeneralErrorNotification();
                    notifyGroupUiListeners(gd);
                    checkIfAnyTxStillActiveAndNotify();
                }

                synchronized (_uiUpdateListeners) {
                    for (IUiUpdateListener listener : _uiUpdateListeners) {
                        listener.onGroupTxFailed(gd, eventExtraJson);
                    }
                }
            }
        });
    }

    @Override
    public void onGroupTxUsurpedByPriority(final String id, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logEvent(Analytics.GROUP_TX_USURPED);

                GroupDescriptor gd = getGroup(id);
                if (gd == null) {
                    Log.e(TAG, "onGroupTxUsurpedByPriority: cannot find group id='" + id + "'");
                    return;
                }

                Log.d(TAG, "onGroupTxUsurpedByPriority: id='" + id + "', n='" + gd.name + "', x=" + eventExtraJson);

                gd.tx = false;
                gd.txPending = false;
                gd.txError = false;
                gd.txUsurped = true;

                _lastAudioActivity = Utils.nowMs();

                synchronized (_groupsSelectedForTx) {
                    _groupsSelectedForTx.remove(gd);
                    playGeneralErrorNotification();
                    notifyGroupUiListeners(gd);
                    checkIfAnyTxStillActiveAndNotify();
                }

                synchronized (_uiUpdateListeners) {
                    for (IUiUpdateListener listener : _uiUpdateListeners) {
                        listener.onGroupTxUsurped(gd, eventExtraJson);
                    }
                }
            }
        });
    }

    @Override
    public void onGroupMaxTxTimeExceeded(final String id, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logEvent(Analytics.GROUP_TX_MAX_EXCEEDED);

                GroupDescriptor gd = getGroup(id);
                if (gd == null) {
                    Log.e(TAG, "onGroupMaxTxTimeExceeded: cannot find group id='" + id + "'");
                    return;
                }

                Log.d(TAG, "onGroupMaxTxTimeExceeded: id='" + id + "', n='" + gd.name + "'");

                gd.tx = false;
                gd.txPending = false;
                gd.txError = false;
                gd.txUsurped = true;

                _lastAudioActivity = Utils.nowMs();

                synchronized (_groupsSelectedForTx) {
                    _groupsSelectedForTx.remove(gd);
                    playGeneralErrorNotification();
                    notifyGroupUiListeners(gd);
                    checkIfAnyTxStillActiveAndNotify();
                }

                synchronized (_uiUpdateListeners) {
                    for (IUiUpdateListener listener : _uiUpdateListeners) {
                        listener.onGroupMaxTxTimeExceeded(gd);
                    }
                }
            }
        });
    }

    @Override
    public void onGroupTxMuted(final String id, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //logEvent(Analytics.GROUP_TX_MUTED);

                GroupDescriptor gd = getGroup(id);
                if (gd == null) {
                    Log.e(TAG, "onGroupTxMuted: cannot find group id='" + id + "'");
                    return;
                }

                Log.d(TAG, "onGroupTxMuted: id='" + id + "', n='" + gd.name + "'");

                // TX muted means something else here
                //gd.txMuted = true;

                notifyGroupUiListeners(gd);
            }
        });
    }

    @Override
    public void onGroupTxUnmuted(final String id, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //logEvent(Analytics.GROUP_TX_UNMUTED);

                GroupDescriptor gd = getGroup(id);
                if (gd == null) {
                    Log.e(TAG, "onGroupTxUnmuted: cannot find group id='" + id + "'");
                    return;
                }

                Log.d(TAG, "onGroupTxUnmuted: id='" + id + "', n='" + gd.name + "'");

                // TX muted means something else here
                //gd.txMuted = false;

                notifyGroupUiListeners(gd);
            }
        });
    }

    @Override
    public void onGroupNodeDiscovered(final String id, final String nodeJson,
                                      final String eventExtraJson) {

        runOnUiThread(() -> {

            Timber.i("onGroupNodeDiscovered %s nodeJson %s eventExtraJson %s", id, nodeJson, eventExtraJson);

            //{"announceOnReceive":false,"comment":"","connectivity":null,"custom":"","disposition":0,
            // "groupAliases":
            // [
            // {"alias":"MB","groupId":"{G1}"},
            // {"alias":"MB","groupId":"{G2}"},
            // {"alias":"MB","groupId":"{G3}"},
            // {"alias":"MB","groupId":"{G4}"},
            // {"alias":"MB","groupId":"{G5}"}
            // ],
            // "identity":{"avatar":"","displayName":"Mb","nodeId":"{e24b92dc-0531-483a-b1a4-1ee3ec3da364}","userId":"mb"},
            // "location":{"latitude":-999.999,"longitude":-999.999},"nextUpdate":1604502374,"power":{"level":0,"source":0,"state":0},"ts":1604502308}

            GroupDiscoveryInfo groupDiscoveryInfo = new Gson().fromJson(nodeJson, GroupDiscoveryInfo.class);
            Globals.groupDiscoveryListener.onGroupDiscover(id, groupDiscoveryInfo);

            //logEvent(Analytics.GROUP_NODE_DISCOVERED);

/*            GroupDescriptor gd = getGroup(id);
            if (gd == null) {
                Log.e(TAG, "onGroupNodeDiscovered: cannot find group id='" + id + "'");
                return;
            }

            Log.d(TAG, "onGroupNodeDiscovered: id='" + id + "', n='" + gd.name + "'");

            PresenceDescriptor pd = getActiveConfiguration().processNodeDiscovered(nodeJson);
            if (pd != null) {
                if (!pd.self && _activeConfiguration.getNotifyOnNodeJoin()) {
                    float volume = _activeConfiguration.getNotificationToneNotificationLevel();
                    if (volume != 0.0) {
                        try {
                            Globals.getAudioPlayerManager().playNotification(R.raw.node_join, volume, null);
                        } catch (Exception e) {
                        }
                    }
                }

                synchronized (_presenceChangeListeners) {
                    for (IPresenceChangeListener listener : _presenceChangeListeners) {
                        listener.onPresenceAdded(pd);
                    }
                }

                notifyGroupUiListeners(gd);
            }*/
        });
    }

    @Override
    public void onGroupNodeRediscovered(final String id, final String nodeJson, final String eventExtraJson) {
        runOnUiThread(() -> {

            Timber.i("onGroupNodeREDISCOVERED %s nodeJson %s eventExtraJson %s", id, nodeJson, eventExtraJson);


            //{"announceOnReceive":false,"comment":"","connectivity":null,"custom":"","disposition":0,
            // "groupAliases":
            // [
            // {"alias":"MB","groupId":"{G1}"},
            // {"alias":"MB","groupId":"{G2}"},
            // {"alias":"MB","groupId":"{G3}"},
            // {"alias":"MB","groupId":"{G4}"},
            // {"alias":"MB","groupId":"{G5}"}
            // ],
            // "identity":{"avatar":"","displayName":"Mb","nodeId":"{e24b92dc-0531-483a-b1a4-1ee3ec3da364}","userId":"mb"},
            // "location":{"latitude":-999.999,"longitude":-999.999},"nextUpdate":1604502374,"power":{"level":0,"source":0,"state":0},"ts":1604502308}

            GroupDiscoveryInfo groupDiscoveryInfo = new Gson().fromJson(nodeJson, GroupDiscoveryInfo.class);
            Globals.groupDiscoveryListener.onGroupRediscover(id, groupDiscoveryInfo);
        });
    }

    @Override
    public void onGroupNodeUndiscovered(final String id, final String nodeJson,
                                        final String eventExtraJson) {
        runOnUiThread(() -> {

            Timber.i("onGroupNodeUndiscovered %s nodeJson %s eventExtraJson %s", id, nodeJson, eventExtraJson);


            //{"announceOnReceive":false,"comment":"","connectivity":null,"custom":"","disposition":0,
            // "groupAliases":
            // [
            // {"alias":"MB","groupId":"{G1}"},
            // {"alias":"MB","groupId":"{G2}"},
            // {"alias":"MB","groupId":"{G3}"},
            // {"alias":"MB","groupId":"{G4}"},
            // {"alias":"MB","groupId":"{G5}"}
            // ],
            // "identity":{"avatar":"","displayName":"Mb","nodeId":"{e24b92dc-0531-483a-b1a4-1ee3ec3da364}","userId":"mb"},
            // "location":{"latitude":-999.999,"longitude":-999.999},"nextUpdate":1604502374,"power":{"level":0,"source":0,"state":0},"ts":1604502308}

            GroupDiscoveryInfo groupDiscoveryInfo = new Gson().fromJson(nodeJson, GroupDiscoveryInfo.class);
            Globals.groupDiscoveryListener.onGroupUndiscover(id, groupDiscoveryInfo);





            /*//logEvent(Analytics.GROUP_NODE_UNDISCOVERED);

            GroupDescriptor gd = getGroup(id);
            if (gd == null) {
                Log.e(TAG, "onGroupNodeUndiscovered: cannot find group id='" + id + "'");
                return;
            }

            Log.d(TAG, "onGroupNodeUndiscovered: id='" + id + "', n='" + gd.name + "'");

            PresenceDescriptor pd = getActiveConfiguration().processNodeUndiscovered(nodeJson);
            if (pd != null) {
                if (!pd.self && _activeConfiguration.getNotifyOnNodeLeave()) {
                    float volume = _activeConfiguration.getNotificationToneNotificationLevel();
                    if (volume != 0.0) {
                        try {
                            Globals.getAudioPlayerManager().playNotification(R.raw.node_leave, volume, null);
                        } catch (Exception e) {
                        }
                    }
                }

                synchronized (_presenceChangeListeners) {
                    for (IPresenceChangeListener listener : _presenceChangeListeners) {
                        listener.onPresenceRemoved(pd);
                    }
                }

                notifyGroupUiListeners(gd);
            }*/
        });
    }

    public boolean getLicenseExpired() {
        return _licenseExpired;
    }

    public double getLicenseSecondsLeft() {
        return _licenseSecondsLeft;
    }

    @Override
    public void onLicenseChanged(final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logEvent(Analytics.LICENSE_CHANGED);

                Log.d(TAG, "onLicenseChanged");
                _licenseExpired = false;
                _licenseSecondsLeft = 0;
                cancelObtainingActivationCode();

                synchronized (_licenseChangeListeners) {
                    for (ILicenseChangeListener listener : _licenseChangeListeners) {
                        listener.onLicenseChanged();
                    }
                }
            }
        });
    }

    @Override
    public void onLicenseExpired(final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!_licenseExpired) {
                    logEvent(Analytics.LICENSE_EXPIRED);
                }

                Log.d(TAG, "onLicenseExpired");
                _licenseExpired = true;
                _licenseSecondsLeft = 0;
                scheduleObtainingActivationCode();

                synchronized (_licenseChangeListeners) {
                    for (ILicenseChangeListener listener : _licenseChangeListeners) {
                        listener.onLicenseExpired();
                    }
                }
            }
        });
    }

    @Override
    public void onLicenseExpiring(final double secondsLeft, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //logEvent(Analytics.LICENSE_EXPIRING);

                Log.d(TAG, "onLicenseExpiring - " + secondsLeft + " seconds remaining");
                _licenseExpired = false;
                _licenseSecondsLeft = secondsLeft;
                scheduleObtainingActivationCode();

                synchronized (_licenseChangeListeners) {
                    for (ILicenseChangeListener listener : _licenseChangeListeners) {
                        listener.onLicenseExpiring(secondsLeft);
                    }
                }
            }
        });
    }

    private String __devOnly__groupId = "SIM0001";

    public void __devOnly__RunTest() {
        if (!_dynamicGroups.containsKey(__devOnly__groupId)) {
            __devOnly__simulateGroupAssetDiscovered();
        } else {
            __devOnly__simulateGroupAssetUndiscovered();
        }
    }

    public void __devOnly__simulateGroupAssetDiscovered() {
        try {
            JSONObject group = new JSONObject();

            group.put(Engine.JsonFields.Group.id, __devOnly__groupId);
            group.put(Engine.JsonFields.Group.name, "Simulated Group 1");
            group.put(Engine.JsonFields.Group.type, GroupDescriptor.Type.gtAudio.ordinal());

            // RX
            {
                JSONObject rx = new JSONObject();
                rx.put(Engine.JsonFields.Rx.address, "234.5.6.7");
                rx.put(Engine.JsonFields.Rx.port, 29000);
                group.put(Engine.JsonFields.Rx.objectName, rx);
            }

            // TX
            {
                JSONObject tx = new JSONObject();
                tx.put(Engine.JsonFields.Tx.address, "234.5.6.7");
                tx.put(Engine.JsonFields.Tx.port, 29000);
                group.put(Engine.JsonFields.Tx.objectName, tx);
            }

            // TxAudio
            {
                JSONObject txAudio = new JSONObject();
                txAudio.put(Engine.JsonFields.TxAudio.encoder, 1);
                txAudio.put(Engine.JsonFields.TxAudio.framingMs, 20);
                txAudio.put(Engine.JsonFields.TxAudio.noHdrExt, true);
                group.put(Engine.JsonFields.TxAudio.objectName, txAudio);
            }

            String json = group.toString();

            onGroupAssetDiscovered(__devOnly__groupId, json, "");
            getEngine().engageCreateGroup(buildFinalGroupJsonConfiguration(json));
        } catch (Exception e) {
            Toast.makeText(this, "EXCEPTION: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void __devOnly__simulateGroupAssetUndiscovered() {
        try {
            JSONObject group = new JSONObject();

            group.put(Engine.JsonFields.Group.id, __devOnly__groupId);
            group.put(Engine.JsonFields.Group.name, "Simulated Group 1");
            group.put(Engine.JsonFields.Group.type, GroupDescriptor.Type.gtAudio.ordinal());

            // RX
            {
                JSONObject rx = new JSONObject();
                rx.put(Engine.JsonFields.Rx.address, "234.5.6.7");
                rx.put(Engine.JsonFields.Rx.port, 29000);
                group.put(Engine.JsonFields.Rx.objectName, rx);
            }

            // TX
            {
                JSONObject tx = new JSONObject();
                tx.put(Engine.JsonFields.Tx.address, "234.5.6.7");
                tx.put(Engine.JsonFields.Tx.port, 29000);
                group.put(Engine.JsonFields.Tx.objectName, tx);
            }

            // TxAudio
            {
                JSONObject txAudio = new JSONObject();
                txAudio.put(Engine.JsonFields.TxAudio.encoder, 1);
                txAudio.put(Engine.JsonFields.TxAudio.framingMs, 20);
                txAudio.put(Engine.JsonFields.TxAudio.noHdrExt, true);
                group.put(Engine.JsonFields.TxAudio.objectName, txAudio);
            }

            String json = group.toString();

            onGroupAssetUndiscovered(__devOnly__groupId, json, "");
            getEngine().engageDeleteGroup(__devOnly__groupId);
        } catch (Exception e) {
            Toast.makeText(this, "EXCEPTION: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void restartStartHumanBiometricsReporting() {
        stopHumanBiometricsReporting();
        startHumanBiometricsReporting();
    }


    public void startHumanBiometricsReporting() {
        if (_humanBiometricsReportingTimer == null) {
            if (Globals.getSharedPreferences().getBoolean(PreferenceKeys.USER_EXPERIMENT_ENABLE_HBM, false)) {
                _hbmTicksBeforeReport = Integer.parseInt(Globals.getSharedPreferences().getString(PreferenceKeys.USER_EXPERIMENT_HBM_INTERVAL_SECS, "0"));
                if (_hbmTicksBeforeReport >= 1) {
                    _hbmTicksSoFar = 0;

                    _hbmHeartRate = new DataSeries(Engine.HumanBiometricsElement.heartRate.toInt());
                    _hbmSkinTemp = new DataSeries(Engine.HumanBiometricsElement.skinTemp.toInt());
                    _hbmCoreTemp = new DataSeries(Engine.HumanBiometricsElement.coreTemp.toInt());
                    _hbmHydration = new DataSeries(Engine.HumanBiometricsElement.hydration.toInt());
                    _hbmBloodOxygenation = new DataSeries(Engine.HumanBiometricsElement.bloodOxygenation.toInt());
                    _hbmFatigueLevel = new DataSeries(Engine.HumanBiometricsElement.fatigueLevel.toInt());
                    _hbmTaskEffectiveness = new DataSeries(Engine.HumanBiometricsElement.taskEffectiveness.toInt());

                    _rhbmgHeart = new RandomHumanBiometricGenerator(50, 175, 15, 75);
                    _rhbmgSkinTemp = new RandomHumanBiometricGenerator(30, 38, 2, 33);
                    _rhbmgCoreTemp = new RandomHumanBiometricGenerator(35, 37, 1, 36);
                    _rhbmgHydration = new RandomHumanBiometricGenerator(60, 100, 3, 90);
                    _rhbmgOxygenation = new RandomHumanBiometricGenerator(87, 100, 5, 94);
                    _rhbmgFatigueLevel = new RandomHumanBiometricGenerator(0, 10, 3, 3);
                    _rhbmgTaskEffectiveness = new RandomHumanBiometricGenerator(0, 10, 3, 3);

                    _humanBiometricsReportingTimer = new Timer();
                    _humanBiometricsReportingTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            onHumanBiometricsTimerTick();
                        }
                    }, 0, 1000);
                }
            }
        }
    }

    public void stopHumanBiometricsReporting() {
        if (_humanBiometricsReportingTimer != null) {
            _humanBiometricsReportingTimer.cancel();
            _humanBiometricsReportingTimer = null;
        }
    }

    private void onHumanBiometricsTimerTick() {
        if (_hbmTicksSoFar == 0) {
            _hbmHeartRate.restart();
            _hbmSkinTemp.restart();
            _hbmCoreTemp.restart();
            _hbmHydration.restart();
            _hbmBloodOxygenation.restart();
            _hbmFatigueLevel.restart();
            _hbmTaskEffectiveness.restart();
        }

        _hbmHeartRate.addElement((byte) 1, (byte) _rhbmgHeart.nextInt());
        _hbmSkinTemp.addElement((byte) 1, (byte) _rhbmgSkinTemp.nextInt());
        _hbmCoreTemp.addElement((byte) 1, (byte) _rhbmgCoreTemp.nextInt());
        _hbmHydration.addElement((byte) 1, (byte) _rhbmgHydration.nextInt());
        _hbmBloodOxygenation.addElement((byte) 1, (byte) _rhbmgOxygenation.nextInt());
        _hbmFatigueLevel.addElement((byte) 1, (byte) _rhbmgFatigueLevel.nextInt());
        _hbmTaskEffectiveness.addElement((byte) 1, (byte) _rhbmgTaskEffectiveness.nextInt());

        _hbmTicksSoFar++;

        if (_hbmTicksSoFar == _hbmTicksBeforeReport) {
            try {
                ByteArrayOutputStream bas = new ByteArrayOutputStream();

                if (Globals.getSharedPreferences().getBoolean(PreferenceKeys.USER_EXPERIMENT_HBM_ENABLE_HEART_RATE, false)) {
                    bas.write(_hbmHeartRate.toByteArray());
                }

                if (Globals.getSharedPreferences().getBoolean(PreferenceKeys.USER_EXPERIMENT_HBM_ENABLE_SKIN_TEMP, false)) {
                    bas.write(_hbmSkinTemp.toByteArray());
                }

                if (Globals.getSharedPreferences().getBoolean(PreferenceKeys.USER_EXPERIMENT_HBM_ENABLE_CORE_TEMP, false)) {
                    bas.write(_hbmCoreTemp.toByteArray());
                }

                if (Globals.getSharedPreferences().getBoolean(PreferenceKeys.USER_EXPERIMENT_HBM_ENABLE_BLOOD_HYDRO, false)) {
                    bas.write(_hbmHydration.toByteArray());
                }

                if (Globals.getSharedPreferences().getBoolean(PreferenceKeys.USER_EXPERIMENT_HBM_ENABLE_BLOOD_OXY, false)) {
                    bas.write(_hbmBloodOxygenation.toByteArray());
                }

                if (Globals.getSharedPreferences().getBoolean(PreferenceKeys.USER_EXPERIMENT_HBM_ENABLE_FATIGUE_LEVEL, false)) {
                    bas.write(_hbmFatigueLevel.toByteArray());
                }

                if (Globals.getSharedPreferences().getBoolean(PreferenceKeys.USER_EXPERIMENT_HBM_ENABLE_TASK_EFFECTIVENESS_LEVEL, false)) {
                    bas.write(_hbmTaskEffectiveness.toByteArray());
                }

                byte[] blob = bas.toByteArray();

                if (blob.length > 0) {
                    Log.i(TAG, "Reporting human biometrics data - blob size is " + blob.length + " bytes");

                    // Our JSON parameters indicate that the payload is binary human biometric data in Engage format
                    JSONObject bi = new JSONObject();
                    bi.put(Engine.JsonFields.BlobInfo.payloadType, Engine.BlobType.engageHumanBiometrics.toInt());
                    String jsonParams = bi.toString();

                    ActiveConfiguration ac = getActiveConfiguration();
                    for (GroupDescriptor gd : ac.getMissionGroups()) {
                        if (gd.type == GroupDescriptor.Type.gtPresence) {
                            getEngine().engageSendGroupBlob(gd.id, blob, blob.length, jsonParams);
                        }
                    }
                } else {
                    Log.w(TAG, "Cannot report human biometrics data - no presence group found");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            _hbmTicksSoFar = 0;
        }
    }

    @Override
    public void onGroupAssetDiscovered(final String id, final String nodeJson,
                                       final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logEvent(Analytics.GROUP_ASSET_DISCOVERED);

                Log.d(TAG, "onGroupAssetDiscovered: id='" + id + "', json='" + nodeJson + "'");

                synchronized (_assetChangeListeners) {
                    for (IAssetChangeListener listener : _assetChangeListeners) {
                        listener.onAssetDiscovered(id, nodeJson);
                    }
                }

                boolean notify = false;
                synchronized (_dynamicGroups) {
                    GroupDescriptor gd = _dynamicGroups.get(id);
                    if (gd == null) {
                        gd = new GroupDescriptor();
                        if (gd.loadFromJson(nodeJson)) {
                            gd.setDynamic(true);
                            gd.selectedForMultiView = true;
                            _dynamicGroups.put(id, gd);
                            notify = true;
                        } else {
                            Log.e(TAG, "onGroupAssetDiscovered: failed to load group descriptor from json");
                        }
                    }
                }

                if (notify) {
                    playAssetDiscoveredNotification();

                    synchronized (_configurationChangeListeners) {
                        for (IConfigurationChangeListener listener : _configurationChangeListeners) {
                            listener.onCriticalConfigurationChange();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onGroupAssetRediscovered(final String id, final String nodeJson,
                                         final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //logEvent(Analytics.GROUP_ASSET_REDISCOVERED);

                synchronized (_assetChangeListeners) {
                    for (IAssetChangeListener listener : _assetChangeListeners) {
                        listener.onAssetRediscovered(id, nodeJson);
                    }
                }

                boolean notify = false;
                synchronized (_dynamicGroups) {
                    GroupDescriptor gd = _dynamicGroups.get(id);
                    if (gd == null) {
                        gd = new GroupDescriptor();
                        if (gd.loadFromJson(nodeJson)) {
                            gd.setDynamic(true);
                            gd.selectedForMultiView = true;
                            _dynamicGroups.put(id, gd);
                            notify = true;
                        } else {
                            Log.e(TAG, "onGroupAssetRediscovered: failed to load group descriptor from json");
                        }
                    }
                }

                if (notify) {
                    playAssetDiscoveredNotification();

                    synchronized (_configurationChangeListeners) {
                        for (IConfigurationChangeListener listener : _configurationChangeListeners) {
                            listener.onCriticalConfigurationChange();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onGroupAssetUndiscovered(final String id, final String nodeJson,
                                         final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logEvent(Analytics.GROUP_ASSET_UNDISCOVERED);

                synchronized (_assetChangeListeners) {
                    for (IAssetChangeListener listener : _assetChangeListeners) {
                        listener.onAssetUndiscovered(id, nodeJson);
                    }
                }

                boolean notify = false;
                synchronized (_dynamicGroups) {
                    if (_dynamicGroups.containsKey(id)) {
                        _dynamicGroups.remove(id);
                        notify = true;
                    }
                }

                if (notify) {
                    playAssetUndiscoveredNotification();
                    synchronized (_configurationChangeListeners) {
                        for (IConfigurationChangeListener listener : _configurationChangeListeners) {
                            listener.onCriticalConfigurationChange();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onGroupBlobSent(final String id, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onGroupBlobSent");
            }
        });
    }

    @Override
    public void onGroupBlobSendFailed(final String id, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "onGroupBlobSendFailed");
            }
        });
    }

    @Override
    public void onGroupBlobReceived(final String id, final String blobInfoJson,
                                    final byte[] blob, final long blobSize, final String eventExtraJson) {

        Log.w("message", "receive");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onGroupBlobReceived: blobInfoJson=" + blobInfoJson);

                try {
                    JSONObject blobInfo = new JSONObject(blobInfoJson);

                    int payloadType = blobInfo.getInt(Engine.JsonFields.BlobInfo.payloadType);
                    String source = blobInfo.getString(Engine.JsonFields.BlobInfo.source);
                    String target = blobInfo.getString(Engine.JsonFields.BlobInfo.target);

                    PresenceDescriptor pd = _activeConfiguration.getPresenceDescriptor(source);

                    // Make a super basic PD if we couldn't find one for some reason
                    if (pd == null) {
                        pd = new PresenceDescriptor();
                        pd.self = false;
                        pd.nodeId = source;
                    }

                    // Human biometrics ... ?
                    if (Engine.BlobType.fromInt(payloadType) == Engine.BlobType.engageHumanBiometrics) {
                        int blobOffset = 0;
                        int bytesLeft = (int) blobSize;
                        boolean anythingUpdated = false;

                        while (bytesLeft > 0) {
                            DataSeries ds = new DataSeries();
                            int bytesProcessed = ds.parseByteArray(blob, blobOffset, bytesLeft);
                            if (bytesProcessed <= 0) {
                                throw new Exception("Error processing HBM");
                            }

                            bytesLeft -= bytesProcessed;
                            blobOffset += bytesProcessed;

                            if (pd.updateBioMetrics(ds)) {
                                anythingUpdated = true;
                            }
                        }

                        if (anythingUpdated) {
                            synchronized (_presenceChangeListeners) {
                                for (IPresenceChangeListener listener : _presenceChangeListeners) {
                                    listener.onPresenceChange(pd);
                                }
                            }
                        }
                    } else if (Engine.BlobType.fromInt(payloadType) == Engine.BlobType.appTextUtf8) {
                        // TODO: only pass on if for this node or for everyone!

                        String message = new String(blob, Constants.CHARSET);

                        synchronized (_groupTextMessageListeners) {
                            for (IGroupTextMessageListener listener : _groupTextMessageListeners) {
                                listener.onGroupTextMessageRx(pd, message);
                            }
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onGroupRtpSent(final String id, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onGroupRtpSent");
            }
        });
    }

    @Override
    public void onGroupRtpSendFailed(final String id, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "onGroupRtpSendFailed");
            }
        });
    }

    @Override
    public void onGroupRtpReceived(final String id, final String rtpHeaderJson,
                                   final byte[] payload, final long payloadSize, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onGroupRtpReceived: rtpHeaderJson=" + rtpHeaderJson);
            }
        });
    }

    public void onGroupRawSent(final String id, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onGroupRawSent");
            }
        });
    }

    @Override
    public void onGroupRawSendFailed(final String id, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "onGroupRawSendFailed");
            }
        });
    }

    @Override
    public void onGroupRawReceived(final String id, final byte[] raw, final long rawsize,
                                   final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onGroupRawReceived");
            }
        });
    }

    @Override
    public void onGroupTimelineEventStarted(final String id, final String eventJson,
                                            final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onGroupTimelineEventStarted: " + id);

                final GroupDescriptor gd = getGroup(id);
                if (gd == null) {
                    Log.e(TAG, "onGroupTimelineEventStarted: cannot find group id='" + id + "'");
                    return;
                }

                synchronized (_groupTimelineListeners) {
                    for (IGroupTimelineListener listener : _groupTimelineListeners) {
                        listener.onGroupTimelineEventStarted(gd, eventJson);
                    }
                }
            }
        });
    }

    @Override
    public void onGroupTimelineEventUpdated(final String id, final String eventJson,
                                            final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onGroupTimelineEventUpdated: " + id);

                final GroupDescriptor gd = getGroup(id);
                if (gd == null) {
                    Log.e(TAG, "onGroupTimelineEventUpdated: cannot find group id='" + id + "'");
                    return;
                }

                synchronized (_groupTimelineListeners) {
                    for (IGroupTimelineListener listener : _groupTimelineListeners) {
                        listener.onGroupTimelineEventUpdated(gd, eventJson);
                    }
                }
            }
        });
    }

    @Override
    public void onGroupTimelineEventEnded(final String id, final String eventJson,
                                          final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onGroupTimelineEventEnded: " + id);

                final GroupDescriptor gd = getGroup(id);
                if (gd == null) {
                    Log.e(TAG, "onGroupTimelineEventEnded: cannot find group id='" + id + "'");
                    return;
                }

                synchronized (_groupTimelineListeners) {
                    for (IGroupTimelineListener listener : _groupTimelineListeners) {
                        listener.onGroupTimelineEventEnded(gd, eventJson);
                    }
                }
            }
        });
    }

    @Override
    public void onGroupTimelineReport(final String id, final String reportJson,
                                      final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "onGroupTimelineReport: " + id);

                synchronized (Globals.onGroupTimelineReportListener) {
                    Globals.onGroupTimelineReportListener.onGroupTimelineEventStarted(id, reportJson);
                    /*for (IGroupTimelineListener listener : _groupTimelineListeners) {
                        listener.onGroupTimelineReport(gd, reportJson);
                    }*/
                }
            }
        });
    }

    @Override
    public void onGroupTimelineReportFailed(final String id, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logEvent(Analytics.GROUP_TIMELINE_REPORT_FAILED);

                Log.d(TAG, "onGroupTimelineReportFailed: " + id);

                final GroupDescriptor gd = getGroup(id);
                if (gd == null) {
                    Log.e(TAG, "onGroupTimelineReportFailed: cannot find group id='" + id + "'");
                    return;
                }

                synchronized (_groupTimelineListeners) {
                    for (IGroupTimelineListener listener : _groupTimelineListeners) {
                        listener.onGroupTimelineReportFailed(gd);
                    }
                }
            }
        });
    }

    @Override
    public void onGroupTimelineGroomed(final String id, final String eventListJson,
                                       final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //logEvent(Analytics.GROUP_TIMELINE_REPORT);

                Log.d(TAG, "onGroupTimelineGroomed: " + id);

                final GroupDescriptor gd = getGroup(id);
                if (gd == null) {
                    Log.e(TAG, "onGroupTimelineGroomed: cannot find group id='" + id + "'");
                    return;
                }

                synchronized (_groupTimelineListeners) {
                    for (IGroupTimelineListener listener : _groupTimelineListeners) {
                        listener.onGroupTimelineGroomed(gd, eventListJson);
                    }
                }
            }
        });
    }

    @Override
    public void onGroupStatsReport(final String id, final String reportJson,
                                   final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logEvent(Analytics.GROUP_STATS_REPORT);

                Log.d(TAG, "onGroupStatsReport: " + id);

                final GroupDescriptor gd = getGroup(id);
                if (gd == null) {
                    Log.e(TAG, "onGroupStatsReport: cannot find group id='" + id + "'");
                    return;
                }

                synchronized (_groupTimelineListeners) {
                    for (IGroupTimelineListener listener : _groupTimelineListeners) {
                        listener.onGroupStatsReport(gd, reportJson);
                    }
                }
            }
        });

    }

    @Override
    public void onGroupStatsReportFailed(final String id, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logEvent(Analytics.GROUP_STATS_REPORT_FAILED);

                Log.d(TAG, "onGroupStatsReportFailed: " + id);

                final GroupDescriptor gd = getGroup(id);
                if (gd == null) {
                    Log.e(TAG, "onGroupStatsReportFailed: cannot find group id='" + id + "'");
                    return;
                }

                synchronized (_groupTimelineListeners) {
                    for (IGroupTimelineListener listener : _groupTimelineListeners) {
                        listener.onGroupStatsReportFailed(gd);
                    }
                }
            }
        });
    }

    @Override
    public void onGroupRxVolumeChanged(final String id, final int leftLevelPerc, final int rightLevelPerc, final String eventExtraJson) {
        runOnUiThread(() -> {
            Log.e(TAG, "onGroupRxVolumeChanged: id='" + id + "'");
        });
    }


    @Override
    public void onGroupHealthReport(final String id, final String reportJson,
                                    final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logEvent(Analytics.GROUP_HEALTH_REPORT);

                Log.d(TAG, "onGroupHealthReport: " + id);

                final GroupDescriptor gd = getGroup(id);
                if (gd == null) {
                    Log.e(TAG, "onGroupHealthReport: cannot find group id='" + id + "'");
                    return;
                }

                synchronized (_groupTimelineListeners) {
                    for (IGroupTimelineListener listener : _groupTimelineListeners) {
                        listener.onGroupHealthReport(gd, reportJson);
                    }
                }
            }
        });
    }

    @Override
    public void onGroupHealthReportFailed(final String id, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logEvent(Analytics.GROUP_HEALTH_REPORT_FAILED);

                Log.d(TAG, "onGroupHealthReportFailed: " + id);

                final GroupDescriptor gd = getGroup(id);
                if (gd == null) {
                    Log.e(TAG, "onGroupHealthReportFailed: cannot find group id='" + id + "'");
                    return;
                }

                synchronized (_groupTimelineListeners) {
                    for (IGroupTimelineListener listener : _groupTimelineListeners) {
                        listener.onGroupHealthReportFailed(gd);
                    }
                }
            }
        });
    }

    @Override
    public void onRallypointPausingConnectionAttempt(final String id,
                                                     final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onRallypointPausingConnectionAttempt");
                // Stub
            }
        });
    }

    @Override
    public void onRallypointConnecting(final String id, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onRallypointConnecting: " + id);
                // Stub
            }
        });
    }

    @Override
    public void onRallypointConnected(final String id, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logEvent(Analytics.GROUP_RP_CONNECTED);

                Log.d(TAG, "onRallypointConnected: " + id);
                // Stub
            }
        });
    }

    @Override
    public void onRallypointDisconnected(final String id, final String eventExtraJson) {
        logEvent(Analytics.GROUP_RP_DISCONNECTED);

        Log.d(TAG, "onRallypointDisconnected: " + id);
        // Stub
    }

    @Override
    public void onRallypointRoundtripReport(final String id, final long rtMs,
                                            final long rtQualityRating, final String eventExtraJson) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (rtQualityRating >= 100) {
                    logEvent(Analytics.GROUP_RP_RT_100);
                } else if (rtQualityRating >= 75) {
                    logEvent(Analytics.GROUP_RP_RT_75);
                } else if (rtQualityRating >= 50) {
                    logEvent(Analytics.GROUP_RP_RT_50);
                } else if (rtQualityRating >= 25) {
                    logEvent(Analytics.GROUP_RP_RT_25);
                } else if (rtQualityRating >= 10) {
                    logEvent(Analytics.GROUP_RP_RT_10);
                } else {
                    logEvent(Analytics.GROUP_RP_RT_0);
                }

                Log.d(TAG, "onRallypointRoundtripReport: " + id + ", ms=" + rtMs + ", qual=" + rtQualityRating);
                // Stub
            }
        });
    }

    public void pauseLicenseActivation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "pauseLicenseActivation");
                _licenseActivationPaused = true;
            }
        });
    }

    public void resumeLicenseActivation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "resumeLicenseActivation");
                _licenseActivationPaused = false;
            }
        });
    }

    private void scheduleObtainingActivationCode() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (_licenseActivationTimer != null) {
                    return;
                }

                double delay = 0;

                if (_licenseExpired) {
                    delay = Constants.MIN_LICENSE_ACTIVATION_DELAY_MS;
                } else {
                    if (_licenseSecondsLeft > 0) {
                        delay = ((_licenseSecondsLeft / 2) * 1000);
                    } else {
                        delay = Constants.MIN_LICENSE_ACTIVATION_DELAY_MS;
                    }
                }

                if (delay < Constants.MIN_LICENSE_ACTIVATION_DELAY_MS) {
                    delay = Constants.MIN_LICENSE_ACTIVATION_DELAY_MS;
                } else if (delay > Constants.MAX_LICENSE_ACTIVATION_DELAY_MS) {
                    delay = Constants.MAX_LICENSE_ACTIVATION_DELAY_MS;
                }

                Log.i(TAG, "scheduling obtaining of activation code in " + (delay / 1000) + " seconds");

                _licenseActivationTimer = new Timer();
                _licenseActivationTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        obtainActivationCode();
                    }
                }, (long) delay);
            }
        });
    }

    private void cancelObtainingActivationCode() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (_licenseActivationTimer != null) {
                    _licenseActivationTimer.cancel();
                    _licenseActivationTimer = null;
                }
            }
        });
    }

    private void obtainActivationCode() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (_licenseActivationPaused) {
                    Log.d(TAG, "license activation paused - rescheduling");

                    // Schedule for another time
                    scheduleObtainingActivationCode();
                } else {
                    try {
                        Log.i(TAG, "attempting to obtain a license activation code");

                        cancelObtainingActivationCode();

                        String jsonData = getEngine().engageGetActiveLicenseDescriptor();
                        JSONObject obj = new JSONObject(jsonData);
                        String deviceId = obj.getString(Engine.JsonFields.License.deviceId);
                        if (Utils.isEmptyString(deviceId)) {
                            throw new Exception("no device id available for licensing");
                        }
                        Globals.getSharedPreferencesEditor().putString(PreferenceKeys.USER_LICENSING_KEY, "861B1070050242E1A60D5239");
                        Globals.getSharedPreferencesEditor().apply();
                        String key = "861B1070050242E1A60D5239";// Globals.getSharedPreferences().getString(PreferenceKeys.USER_LICENSING_KEY, "861B1070050242E1A60D5239");

                        if (Utils.isEmptyString(key)) {
                            throw new Exception("no license key available for licensing");
                        }

                        String url;
                        if (Globals.getSharedPreferences().getBoolean(PreferenceKeys.DEVELOPER_USE_DEV_LICENSING_SYSTEM, false)) {
                            url = getString(R.string.online_licensing_activation_url_dev);
                        } else {
                            url = getString(R.string.online_licensing_activation_url_prod);
                        }

                        String ac = Globals.getSharedPreferences().getString(PreferenceKeys.USER_LICENSING_ACTIVATION_CODE, "");

                        String stringToHash = key + deviceId + getString(R.string.licensing_entitlement);
                        String hValue = Utils.md5HashOfString(stringToHash);

                        LicenseActivationTask lat = new LicenseActivationTask(url, getString(R.string.licensing_entitlement), key, ac, deviceId, hValue, EngageApplication.this);

                        lat.execute();
                    } catch (Exception e) {
                        Log.d(TAG, "obtainActivationCode: " + e.getMessage());
                        scheduleObtainingActivationCode();
                    }
                }
            }
        });
    }

    @Override
    public void onLicenseActivationTaskComplete(final int result, final String activationCode,
                                                final String resultMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean needScheduling = false;

                if (_licenseActivationPaused) {
                    Log.d(TAG, "license activation paused - rescheduling");
                    needScheduling = true;
                } else {
                    if (result == 0 && !Utils.isEmptyString(activationCode)) {
                        String key = Globals.getSharedPreferences().getString(PreferenceKeys.USER_LICENSING_KEY, null);
                        if (!Utils.isEmptyString(key)) {
                            Log.i(TAG, "onLicenseActivationTaskComplete: attempt succeeded");

                            String ac = Globals.getSharedPreferences().getString(PreferenceKeys.USER_LICENSING_ACTIVATION_CODE, "");
                            if (ac.compareTo(activationCode) == 0) {
                                logEvent(Analytics.LICENSE_ACT_OK_ALREADY);
                                Log.w(TAG, "onLicenseActivationTaskComplete: new activation code matches existing activation code");
                            } else {
                                logEvent(Analytics.LICENSE_ACT_OK);
                            }

                            Globals.getSharedPreferencesEditor().putString(PreferenceKeys.USER_LICENSING_ACTIVATION_CODE, activationCode);
                            Globals.getSharedPreferencesEditor().apply();
                            getEngine().engageUpdateLicense(getString(R.string.licensing_entitlement), key, activationCode, getString(R.string.manufacturer_id));
                        } else {
                            logEvent(Analytics.LICENSE_ACT_FAILED_NO_KEY);
                            Log.e(TAG, "onLicenseActivationTaskComplete: no license key present");
                            needScheduling = true;
                        }
                    } else {
                        logEvent(Analytics.LICENSE_ACT_FAILED);
                        Log.e(TAG, "onLicenseActivationTaskComplete: attempting failed - " + resultMessage);
                        needScheduling = true;
                    }
                }

                if (needScheduling) {
                    scheduleObtainingActivationCode();
                } else {
                    cancelObtainingActivationCode();
                }
            }
        });
    }
}
