//
//  Copyright (c) 2019 Rally Tactical Systems, Inc.
//  All rights reserved.
//

package com.rallytac.engageandroid;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import com.rallytac.engage.engine.Engine;

public class ActiveConfiguration
{
    private static String TAG = ActiveConfiguration.class.getSimpleName();

    private static String JSON_FIELD_FOR_RP_USE = "use";//NON-NLS

    public enum MulticastFailoverPolicy
    {
        followAppSetting,       // 0
        overrideAndAllow,       // 1
        overrideAndPrevent      // 2
    }

    public static MulticastFailoverPolicy MulticastFailoverPolicyFromInt(int i)
    {
        if(i == 2)
        {
            return MulticastFailoverPolicy.overrideAndPrevent;
        }
        else if(i == 1)
        {
            return MulticastFailoverPolicy.overrideAndAllow;
        }
        else
        {
            return MulticastFailoverPolicy.followAppSetting;
        }
    }

    public static int IntFromMulticastFailoverPolicy(MulticastFailoverPolicy p)
    {
        if(p == MulticastFailoverPolicy.overrideAndPrevent)
        {
            return 2;
        }
        else if(p == MulticastFailoverPolicy.overrideAndAllow)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public static class LocationConfiguration
    {
        public boolean enabled;
        public int intervalMs;
        public int minIntervalMs;
        public int accuracy;
        public float minDisplacement;

        public LocationConfiguration()
        {
            LocationConfiguration.this.clear();
        }

        public void clear()
        {
            enabled = Constants.DEF_LOCATION_ENABLED;
            intervalMs = (Constants.DEF_LOCATION_INTERVAL_SECS * 1000);
            minIntervalMs = intervalMs;
            accuracy = Constants.DEF_LOCATION_ACCURACY;
            minDisplacement = Constants.DEF_LOCATION_MIN_DISPLACEMENT;
        }
    }

    public static class MulticastFailoverConfiguration
    {
        public boolean enabled;
        public int thresholdSecs;

        public MulticastFailoverConfiguration()
        {
            MulticastFailoverConfiguration.this.clear();
        }

        public void clear()
        {
            enabled = Constants.DEF_MULTICAST_FAILOVER_ENABLED;
            thresholdSecs = Constants.DEF_MULTICAST_FAILOVER_THRESHOLD_SECS;
        }
    }

    private String _missionId;
    private String _missionName;
    private String _missionDescription;
    private String _missionModPin;
    private MulticastFailoverPolicy _missionMcFailoverPolicy;
    private ArrayList<GroupDescriptor>   _missionGroups = new ArrayList<>();
    private HashMap<String, PresenceDescriptor> _nodes = new HashMap<String, PresenceDescriptor>();

    private String _networkInterfaceName;

    private boolean _useRP;
    private String _rpAddress;
    private int _rpPort;
    private int _multicastFailoverPolicy;

    private String _nodeId;
    private String _userId;
    private String _userDisplayName;
    private String _userAlias;

    private Constants.UiMode _uiMode;
    private float _pttToneNotificationLevel;
    private float _notificationToneNotificationLevel;
    private float _errorToneNotificationLevel;

    private int _speakerOutputBoostFactor;

    private boolean _notifyOnNodeJoin;
    private boolean _notifyOnNodeLeave;
    private boolean _notifyOnNewAudio;
    private boolean _notifyOnNetworkError;
    private boolean _enableVibrations;
    private boolean _notifyPttEveryTx;
    private boolean _pttLatching;
    private boolean _pttVoiceControl;

    private boolean _discoverSsdpAssets;
    private boolean _discoverTrelliswareAssets;

    private boolean _discoverCistechGv1Assets;
    private String _cistechGv1DiscoveryAddress;
    private int _cistechGv1DiscoveryPort;
    private int _cistechGv1DiscoveryTimeoutSecs;

    private String _inputJson;

    private LocationConfiguration _locationConfiguration = new LocationConfiguration();
    private MulticastFailoverConfiguration _multicastFailoverConfiguration = new MulticastFailoverConfiguration();

    public boolean getPttLatching()
    {
        return _pttLatching;
    }

    public void setPttLatching(boolean pttLatching)
    {
        _pttLatching = pttLatching;

        Globals.getSharedPreferencesEditor().putBoolean(PreferenceKeys.USER_UI_PTT_LATCHING, _pttLatching);
        Globals.getSharedPreferencesEditor().apply();
    }

    public boolean getPttVoiceControl()
    {
        return _pttVoiceControl;
    }

    public void setPttVoiceControl(boolean pttVoiceControl)
    {
        _pttVoiceControl = pttVoiceControl;

        Globals.getSharedPreferencesEditor().putBoolean(PreferenceKeys.USER_UI_PTT_VOICE_CONTROL, _pttVoiceControl);
        Globals.getSharedPreferencesEditor().apply();
    }

    public String getInputJson()
    {
        return _inputJson;
    }

    public boolean getDiscoverSsdpAssets()
    {
        return _discoverSsdpAssets;
    }

    public void setDiscoverSsdpAssets(boolean discover)
    {
        _discoverSsdpAssets = discover;
    }

    public boolean getDiscoverCistechGv1Assets()
    {
        return _discoverCistechGv1Assets;
    }

    public void setDiscoverCistechGv1Assets(boolean discover)
    {
        _discoverCistechGv1Assets = discover;
    }

    public void setCistechGv1DiscoveryAddress(String addr)
    {
        _cistechGv1DiscoveryAddress = addr;
    }

    public void setCistechGv1DiscoveryPort(int port)
    {
        _cistechGv1DiscoveryPort = port;
    }

    public void setCistechGv1DiscoveryTimeoutSecs(int secs)
    {
        _cistechGv1DiscoveryTimeoutSecs = secs;
    }

    public boolean getDiscoverTrelliswareAssets()
    {
        return _discoverTrelliswareAssets;
    }

    public void setDiscoverTrelliswareAssets(boolean discover)
    {
        _discoverTrelliswareAssets = discover;
    }

    public boolean getEnableVibrations()
    {
        return _enableVibrations;
    }

    public void setEnableVibrations(boolean enable)
    {
        _enableVibrations = enable;

        Globals.getSharedPreferencesEditor().putBoolean(PreferenceKeys.USER_NOTIFY_VIBRATIONS, _enableVibrations);
        Globals.getSharedPreferencesEditor().apply();
    }

    public boolean getNotifyPttEveryTime()
    {
        return _notifyPttEveryTx;
    }

    public void setNotifyPttEveryTime(boolean notify)
    {
        _notifyPttEveryTx = notify;

        Globals.getSharedPreferencesEditor().putBoolean(PreferenceKeys.USER_NOTIFY_PTT_EVERY_TIME, _notifyPttEveryTx);
        Globals.getSharedPreferencesEditor().apply();
    }


    public boolean getNotifyOnNodeJoin()
    {
        return _notifyOnNodeJoin;
    }

    public void setNotifyOnNodeJoin(boolean notify)
    {
        _notifyOnNodeJoin = notify;

        Globals.getSharedPreferencesEditor().putBoolean(PreferenceKeys.USER_NOTIFY_NODE_JOIN, _notifyOnNodeJoin);
        Globals.getSharedPreferencesEditor().apply();
    }

    public boolean getNotifyOnNodeLeave()
    {
        return _notifyOnNodeLeave;
    }

    public void setNotifyOnNodeLeave(boolean notify)
    {
        _notifyOnNodeLeave = notify;

        Globals.getSharedPreferencesEditor().putBoolean(PreferenceKeys.USER_NOTIFY_NODE_LEAVE, _notifyOnNodeLeave);
        Globals.getSharedPreferencesEditor().apply();
    }

    public boolean getNotifyOnNewAudio()
    {
        return _notifyOnNewAudio;
    }

    public void setNotifyOnNewAudio(boolean notify)
    {
        _notifyOnNewAudio = notify;

        Globals.getSharedPreferencesEditor().putBoolean(PreferenceKeys.USER_NOTIFY_NEW_AUDIO_RX, _notifyOnNewAudio);
        Globals.getSharedPreferencesEditor().apply();
    }

    public boolean getNotifyOnNetworkError()
    {
        return _notifyOnNetworkError;
    }

    public void setNotifyOnNetworkError(boolean notify)
    {
        _notifyOnNetworkError = notify;

        Globals.getSharedPreferencesEditor().putBoolean(PreferenceKeys.USER_NOTIFY_NETWORK_ERROR, _notifyOnNetworkError);
        Globals.getSharedPreferencesEditor().apply();
    }

    public int getSpeakerOutputBoostFactor()
    {
        return _speakerOutputBoostFactor;
    }

    public void setSpeakerOutputBoostFactor(int factor)
    {
        _speakerOutputBoostFactor = factor;

        Globals.getSharedPreferencesEditor().putInt(PreferenceKeys.USER_SPEAKER_OUTPUT_BOOST_FACTOR, _speakerOutputBoostFactor);
        Globals.getSharedPreferencesEditor().apply();
    }


    public float getPttToneNotificationLevel()
    {
        return _pttToneNotificationLevel;
    }

    public void setPttToneNotificationLevel(float level)
    {
        _pttToneNotificationLevel = level;

        Globals.getSharedPreferencesEditor().putString(PreferenceKeys.USER_TONE_LEVEL_PTT, Float.toString(_pttToneNotificationLevel));
        Globals.getSharedPreferencesEditor().apply();
    }

    public float getErrorToneNotificationLevel()
    {
        return _errorToneNotificationLevel;
    }

    public void setErrorToneNotificationLevel(float level)
    {
        _errorToneNotificationLevel = level;

        Globals.getSharedPreferencesEditor().putString(PreferenceKeys.USER_TONE_LEVEL_ERROR, Float.toString(_errorToneNotificationLevel));
        Globals.getSharedPreferencesEditor().apply();
    }


    public float getNotificationToneNotificationLevel()
    {
        return _notificationToneNotificationLevel;
    }

    public void setNotificationToneNotificationLevel(float level)
    {
        _notificationToneNotificationLevel = level;

        Globals.getSharedPreferencesEditor().putString(PreferenceKeys.USER_TONE_LEVEL_NOTIFICATION, Float.toString(_notificationToneNotificationLevel));
        Globals.getSharedPreferencesEditor().apply();
    }


    public Constants.UiMode getUiMode()
    {
        return _uiMode;
    }

    public void setUiMode(Constants.UiMode mode)
    {
        _uiMode = mode;

        Globals.getSharedPreferencesEditor().putInt(PreferenceKeys.UI_MODE, _uiMode.ordinal());
        Globals.getSharedPreferencesEditor().apply();
    }

    public void setMulticastFailoverConfiguration(MulticastFailoverConfiguration mc)
    {
        _multicastFailoverConfiguration = mc;
    }

    public MulticastFailoverConfiguration getMulticastFailoverConfiguration()
    {
        return _multicastFailoverConfiguration;
    }

    public void setLocationConfiguration(LocationConfiguration lc)
    {
        _locationConfiguration = lc;

        Globals.getSharedPreferencesEditor().putBoolean(PreferenceKeys.USER_LOCATION_SHARED, lc.enabled);
        Globals.getSharedPreferencesEditor().putString(PreferenceKeys.USER_LOCATION_INTERVAL_SECS, Integer.toString(lc.intervalMs / 1000));
        Globals.getSharedPreferencesEditor().putString(PreferenceKeys.USER_LOCATION_ACCURACY, Integer.toString(lc.accuracy));
        Globals.getSharedPreferencesEditor().putString(PreferenceKeys.USER_LOCATION_MIN_DISPLACEMENT, Float.toString(lc.minDisplacement));
        Globals.getSharedPreferencesEditor().apply();
    }

    public LocationConfiguration getLocationConfiguration()
    {
        return _locationConfiguration;
    }

    public boolean isValid()
    {
        return (!Utils.isEmptyString(_missionId));
    }

    public String getMissionId()
    {
        return _missionId;
    }

    public void set_missionId(String id){
        this._missionId = id;
    }

    public String getMissionName()
    {
        return _missionName;
    }

    public String getMissionDescription()
    {
        return _missionDescription;
    }

    public String getMissionModPin()
    {
        return _missionModPin;
    }

    public MulticastFailoverPolicy getMissionMulticastFailoverPolicy()
    {
        return _missionMcFailoverPolicy;
    }

    public boolean addDynamicGroup(GroupDescriptor gd)
    {
        boolean rc = false;

        try
        {
            GroupDescriptor existing = getGroupDescriptor(gd.id);

            if(existing == null)
            {
                _missionGroups.add(gd);
                rc = true;
            }
            else
            {
                if(existing.isDynamic())
                {
                    // We've rediscovered!!
                }
                else
                {
                    // Huh ???!!!
                    throw new Exception("Attempt to dynamically add a statically-defined group!");
                }
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "addDynamicGroup: " + e.getMessage());//NON-NLS
            rc = false;
        }

        return rc;
    }

    public boolean updateDynamicGroup(String id, String json)
    {
        boolean rc = false;

        try
        {
        }
        catch (Exception e)
        {
            Log.e(TAG, "updateDynamicGroup: " + e.getMessage());//NON-NLS
            rc = false;
        }

        return rc;
    }

    public boolean removeDynamicGroup(String id)
    {
        boolean rc = false;

        try
        {
        }
        catch (Exception e)
        {
            Log.e(TAG, "removeDynamicGroup: " + e.getMessage());//NON-NLS
            rc = false;
        }

        return rc;
    }

    public ArrayList<GroupDescriptor> getMissionGroups()
    {
        return _missionGroups;
    }

    public int getMissionNodeCount(String forGroupId)
    {
        int rc = 0;

        synchronized (_nodes)
        {
            if(Utils.isEmptyString(forGroupId))
            {
                rc = _nodes.size();
            }
            else
            {
                for (PresenceDescriptor pd : _nodes.values())
                {
                    if(pd.groupAliases.keySet().contains(forGroupId))
                    {
                        rc++;
                    }
                }
            }
        }

        return rc;
    }

    public ArrayList<PresenceDescriptor> getMissionNodes(String forGroupId)
    {
        ArrayList<PresenceDescriptor> rc = new ArrayList<>();

        synchronized(_nodes)
        {
            for (PresenceDescriptor pd : _nodes.values())
            {
                if(Utils.isEmptyString(forGroupId))
                {
                    rc.add(pd);
                }
                else
                {
                    if(pd.groupAliases.keySet().contains(forGroupId))
                    {
                        rc.add(pd);
                    }
                }
            }
        }

        return rc;
    }

    public PresenceDescriptor getPresenceDescriptor(String nodeId)
    {
        PresenceDescriptor rc = null;

        synchronized(_nodes)
        {
            rc = _nodes.get(nodeId);
        }

        return rc;
    }

    public GroupDescriptor getGroupDescriptor(String id)
    {
        if(_missionGroups != null)
        {
            for (GroupDescriptor currentDescriptor : _missionGroups)
            {
                if (currentDescriptor.id.compareTo(id) == 0)
                {
                    return currentDescriptor;
                }
            }
        }

        return null;
    }

    public void setNetworkInterfaceName(String nm)
    {
        _networkInterfaceName = nm;
    }

    public String getNetworkInterfaceName()
    {
        return _networkInterfaceName;
    }

    public void setUseRp(boolean b)
    {
        _useRP = b;
    }

    public boolean getUseRp()
    {
        return getCanUseRp();
        //return _useRP && getCanUseRp();
    }

    public boolean getCanUseRp()
    {
        return ((!Utils.isEmptyString(_rpAddress)) && (_rpPort > 0));
    }

    public void setRpAddress(String address)
    {
        _rpAddress = address;
    }

    public String getRpAddress()
    {
        return _rpAddress;
    }

    public void setRpPort(int port)
    {
        _rpPort = port;
    }

    public int getRpPort()
    {
        return _rpPort;
    }


    public String getNodeId()
    {
        return _nodeId;
    }

    public void setNodeId(String nodeId)
    {
        _nodeId = nodeId;
    }

    public String getUserId()
    {
        return _userId;
    }

    public void setUserId(String userId)
    {
        _userId = userId;
    }

    public String getUserDisplayName()
    {
        return _userDisplayName;
    }

    public void setUserDisplayName(String userDisplayName)
    {
        _userDisplayName = userDisplayName;
    }

    public String getUserAlias()
    {
        return _userAlias;
    }

    public void setUserAlias(String userAlias)
    {
        _userAlias = userAlias;
    }

    public void clear()
    {
        _missionId = "";
        _missionName = "";
        _missionDescription = "";
        _missionModPin = "";
        _missionMcFailoverPolicy = MulticastFailoverPolicy.followAppSetting;
        _missionGroups.clear();
        _nodes.clear();

        _useRP = false;
        _rpAddress = "";
        _rpPort = 0;
        _multicastFailoverPolicy = IntFromMulticastFailoverPolicy(MulticastFailoverPolicy.followAppSetting);
        _locationConfiguration.clear();
        _multicastFailoverConfiguration.clear();

        _uiMode = Constants.DEF_UI_MODE;
        _pttToneNotificationLevel = Constants.DEF_PTT_TONE_LEVEL;
        _errorToneNotificationLevel = Constants.DEF_ERROR_TONE_LEVEL;
        _notificationToneNotificationLevel = Constants.DEF_NOTIFICATION_TONE_LEVEL;

        _speakerOutputBoostFactor = Constants.DEF_SPEAKER_OUTPUT_BOOST_FACTOR;
    }

    public JSONObject makeTemplate()
    {
        JSONObject rc = new JSONObject();

        try
        {
            rc.put(Engine.JsonFields.Mission.id, _missionId);

            if(!Utils.isEmptyString(_missionName))
            {
                rc.put(Engine.JsonFields.Mission.name, _missionName);
            }

            if(!Utils.isEmptyString(_missionDescription))
            {
                rc.put(Engine.JsonFields.Mission.description, _missionDescription);
            }

            if(!Utils.isEmptyString(_missionModPin))
            {
                rc.put(Engine.JsonFields.Mission.modPin, _missionModPin);
            }

            rc.put("multicastFailoverPolicy", _multicastFailoverPolicy);

            if(!Utils.isEmptyString(_rpAddress) && _rpPort > 0)
            {
                JSONObject rallypoint = new JSONObject();
                rallypoint.put(JSON_FIELD_FOR_RP_USE, _useRP);
                rallypoint.put(Engine.JsonFields.Rallypoint.Host.address, _rpAddress);
                rallypoint.put(Engine.JsonFields.Rallypoint.Host.port, _rpPort);
                rc.put(Engine.JsonFields.Rallypoint.objectName, rallypoint);
            }

            if(_missionGroups != null && _missionGroups.size() > 0)
            {
                JSONArray groups = new JSONArray();

                for(GroupDescriptor gd : _missionGroups)
                {
                    if(!gd.isDynamic())
                    {
                        JSONObject group = new JSONObject(gd.jsonConfiguration);
                        groups.put(group);
                    }
                }

                rc.put(Engine.JsonFields.Group.arrayName, groups);
            }
        }
        catch (Exception e)
        {
            rc = null;
            e.printStackTrace();
        }

        return rc;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean parseTemplate(String json)
    {
        boolean rc = false;

        clear();

        try
        {
            JSONObject root = new JSONObject(json);

            // 0 = undefined, 1 = definedAndUse, 2 = definedAndDontUse
            int useRpFromTemplate = 0;

            _missionId = root.getString(Engine.JsonFields.Mission.id);
            _missionName = root.optString(Engine.JsonFields.Mission.name);
            _missionDescription = root.optString(Engine.JsonFields.Mission.description);
            _missionModPin = root.optString(Engine.JsonFields.Mission.modPin);

            _missionMcFailoverPolicy = MulticastFailoverPolicyFromInt(root.optInt("multicastFailoverPolicy", IntFromMulticastFailoverPolicy(MulticastFailoverPolicy.followAppSetting)));

            // Rallypoint (using default certificate)
            {
                JSONObject rallypoint = root.optJSONObject(Engine.JsonFields.Rallypoint.objectName);
                if(rallypoint != null)
                {
                    _rpAddress = rallypoint.getString(Engine.JsonFields.Rallypoint.Host.address);
                    _rpPort = rallypoint.getInt(Engine.JsonFields.Rallypoint.Host.port);

                    if(rallypoint.has(JSON_FIELD_FOR_RP_USE))
                    {
                        boolean tmp = rallypoint.optBoolean(JSON_FIELD_FOR_RP_USE, false);
                        if(tmp)
                        {
                            useRpFromTemplate = 1;
                        }
                        else
                        {
                            useRpFromTemplate = 2;
                        }
                    }
                }

                if(Utils.isEmptyString(_rpAddress))
                {
                    _rpAddress = Constants.DEF_RP_ADDRESS;
                }

                if(_rpPort <= 0)
                {
                    _rpPort = Constants.DEF_RP_PORT;
                }
            }

            // Groups
            {
                JSONArray groups = root.optJSONArray(Engine.JsonFields.Group.arrayName);
                if(groups != null)
                {
                    for(int x = 0; x < groups.length(); x++)
                    {
                        JSONObject group = groups.getJSONObject(x);
                        if(group != null)
                        {
                            GroupDescriptor g = new GroupDescriptor();

                            g.id = group.optString(Engine.JsonFields.Group.id, "");
                            g.type = GroupDescriptor.Type.values()[group.optInt(Engine.JsonFields.Group.type, 0)];
                            g.name = group.optString(Engine.JsonFields.Group.name, "");
                            g.isEncrypted = (!group.optString(Engine.JsonFields.Group.cryptoPassword, "").isEmpty());
                            g.jsonConfiguration = group.toString();

                            JSONObject txAudio = group.optJSONObject(Engine.JsonFields.TxAudio.objectName);
                            if(txAudio != null)
                            {
                                g.fdx = group.optBoolean(Engine.JsonFields.TxAudio.fdx, false);
                            }

                            JSONArray rallypointArray = group.optJSONArray(Engine.JsonFields.Rallypoint.arrayName);
                            if (rallypointArray != null)
                            {
                                if(Utils.isEmptyString(_rpAddress))
                                {
                                    for (int y = 0; y < rallypointArray.length(); y++)
                                    {
                                        JSONObject rp = rallypointArray.optJSONObject(y);
                                        if(rp != null)
                                        {
                                            JSONObject host = rp.optJSONObject(Engine.JsonFields.Rallypoint.Host.objectName);
                                            if( host != null)
                                            {
                                                String tmpAddr = host.optString(Engine.JsonFields.Rallypoint.Host.address, null);
                                                int tmpPort = host.optInt(Engine.JsonFields.Rallypoint.Host.port, 0);

                                                if(!Utils.isEmptyString(tmpAddr) && tmpPort > 0)
                                                {
                                                    _rpAddress = tmpAddr;
                                                    _rpPort = tmpPort;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }

                                // Remove the Rallypoint array
                                group.remove(Engine.JsonFields.Rallypoint.arrayName);
                            }

                            _missionGroups.add(g);
                        }
                    }
                }
            }

            // undefined
            if(useRpFromTemplate == 0 || useRpFromTemplate == 1)
            {
                if (!Utils.isEmptyString(_rpAddress) && _rpPort > 0)
                {
                    _useRP = true;
                }
            }
            else
            {
                _useRP = false;
            }

            _inputJson = json;

            rc = true;
        }
        catch (Exception e)
        {
            rc = false;
            e.printStackTrace();
        }

        return rc;
    }

    public static JSONObject makeBaselineEnginePolicyObject(String template)
    {
        JSONObject rc = null;


        try
        {
            if(!Utils.isEmptyString(template))
            {
                rc = new JSONObject(template);
            }
            else
            {
                rc = new JSONObject();
            }

            // This piece is meant for debugging
            /*
            try
            {
                Utils.copyFileOrDirectory(Globals.getContext().getFilesDir().toString(), "/sdcard/tmp");
                //Utils.deleteDirectory(Globals.getContext().getFilesDir().toString());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            */

            // Let's make the data directory private to this app only
            {
                rc.put(Engine.JsonFields.EnginePolicy.dataDirectory, Globals.getContext().getFilesDir().toString());
            }

            // Security (including the default certificate)
            {
                JSONObject security = rc.optJSONObject(Engine.JsonFields.EnginePolicy.Security.objectName);
                if(security == null)
                {
                    security = new JSONObject();
                }

                JSONObject certificate = security.optJSONObject(Engine.JsonFields.EnginePolicy.Certificate.objectName);
                if(certificate == null)
                {
                    certificate = new JSONObject();
                }

                certificate.put(Engine.JsonFields.EnginePolicy.Certificate.certificate, "@certstore://" + Globals.getContext().getString(R.string.certstore_default_certificate_id));//NON-NLS
                certificate.put(Engine.JsonFields.EnginePolicy.Certificate.key, "@certstore://" + Globals.getContext().getString(R.string.certstore_default_certificate_id));//NON-NLS

                security.put(Engine.JsonFields.EnginePolicy.Certificate.objectName, certificate);
                rc.put(Engine.JsonFields.EnginePolicy.Security.objectName, security);
            }

            // Licensing
            {
                JSONObject licensing = rc.optJSONObject(Engine.JsonFields.EnginePolicy.Licensing.objectName);
                if(licensing == null)
                {
                    licensing = new JSONObject();
                }

                licensing.put(Engine.JsonFields.EnginePolicy.Licensing.manufacturerId,
                            Globals.getContext().getString(R.string.licensing_manufacturerId));

                licensing.put(Engine.JsonFields.EnginePolicy.Licensing.entitlement,
                        Globals.getContext().getString(R.string.licensing_entitlement));

                licensing.put(Engine.JsonFields.EnginePolicy.Licensing.key,
                        Globals.getSharedPreferences().getString(PreferenceKeys.USER_LICENSING_KEY, ""));

                licensing.put(Engine.JsonFields.EnginePolicy.Licensing.activationCode,
                        Globals.getSharedPreferences().getString(PreferenceKeys.USER_LICENSING_ACTIVATION_CODE, ""));

                rc.put(Engine.JsonFields.EnginePolicy.Licensing.objectName, licensing);
            }

            // Audio
            {
                JSONObject audio = rc.optJSONObject(Engine.JsonFields.EnginePolicy.Audio.objectName);
                if(audio == null)
                {
                    audio = new JSONObject();
                }

                // Default to stereo output
                audio.put(Engine.JsonFields.EnginePolicy.Audio.internalChannels, 2);

                JSONObject aec = audio.optJSONObject(Engine.JsonFields.EnginePolicy.Audio.Aec.objectName);
                if(aec == null)
                {
                    aec = new JSONObject();
                }

                boolean aecEnabled = Globals.getSharedPreferences().getBoolean(PreferenceKeys.USER_AUDIO_AEC_ENABLED, Constants.DEF_AEC_ENABLED);
                aec.put(Engine.JsonFields.EnginePolicy.Audio.Aec.enabled, aecEnabled);
                aec.put(Engine.JsonFields.EnginePolicy.Audio.Aec.cng, Globals.getSharedPreferences().getBoolean(PreferenceKeys.USER_AUDIO_AEC_CNG, Constants.DEF_AEC_CNG));
                aec.put(Engine.JsonFields.EnginePolicy.Audio.Aec.mode, Integer.parseInt(Globals.getSharedPreferences().getString(PreferenceKeys.USER_AUDIO_AEC_MODE, Integer.toString(Constants.DEF_AEC_MODE))));
                aec.put(Engine.JsonFields.EnginePolicy.Audio.Aec.speakerTailMs, Integer.parseInt(Globals.getSharedPreferences().getString(PreferenceKeys.USER_AUDIO_AEC_SPEAKER_TAIL_MS, Integer.toString(Constants.DEF_AEC_SPEAKER_TAIL_MS))));

                audio.put(Engine.JsonFields.EnginePolicy.Audio.Aec.objectName, aec);

                // Maybe change to mono output if desired
                if(aecEnabled)
                {
                    boolean disableStereo = Globals.getSharedPreferences().getBoolean(PreferenceKeys.USER_AUDIO_AEC_DISABLE_STEREO, Constants.DEF_AEC_STEREO_DISABLED);
                    if(disableStereo)
                    {
                        audio.put(Engine.JsonFields.EnginePolicy.Audio.internalChannels, 1);
                    }
                }

                rc.put(Engine.JsonFields.EnginePolicy.Audio.objectName, audio);
            }


        }
        catch (Exception e)
        {
            e.printStackTrace();
            rc = null;
        }

        return rc;
    }

    public JSONObject makeEnginePolicyObjectFromBaseline(JSONObject baseLine)
    {
        JSONObject rc = baseLine;

        try
        {
            // Networking
            {
                JSONObject networking = rc.optJSONObject(Engine.JsonFields.EnginePolicy.Networking.objectName);
                if(networking == null)
                {
                    networking = new JSONObject();
                }

                networking.put(Engine.JsonFields.EnginePolicy.Networking.defaultNic, _networkInterfaceName);

                if(Globals.getSharedPreferences().getBoolean(PreferenceKeys.USER_AUDIO_JITTER_LOW_LATENCY_ENABLED, Constants.DEF_USER_AUDIO_JITTER_LOW_LATENCY_ENABLED))
                {
                    networking.put(Engine.JsonFields.EnginePolicy.Networking.rtpJtterLatencyMode, Engine.JitterBufferLatency.toInt(Engine.JitterBufferLatency.lowLatency));
                }
                else
                {
                    networking.put(Engine.JsonFields.EnginePolicy.Networking.rtpJtterLatencyMode, Engine.JitterBufferLatency.toInt(Engine.JitterBufferLatency.standard));
                }

                rc.put(Engine.JsonFields.EnginePolicy.Networking.objectName, networking);
            }

            // Discovery --- EXPERIMENTAL !!!
            if(_discoverSsdpAssets
                    || _discoverCistechGv1Assets
                    || _discoverTrelliswareAssets)
            {
                JSONObject discovery = rc.optJSONObject(Engine.JsonFields.EnginePolicy.Discovery.objectName);
                if(discovery == null)
                {
                    discovery = new JSONObject();
                }

                // SSDP
                if(_discoverSsdpAssets)
                {
                    JSONObject ssdp = discovery.optJSONObject(Engine.JsonFields.EnginePolicy.Discovery.Ssdp.objectName);
                    if(ssdp == null)
                    {
                        ssdp = new JSONObject();
                    }

                    ssdp.put(Engine.JsonFields.EnginePolicy.Discovery.Ssdp.enabled, true);
                    ssdp.put(Engine.JsonFields.EnginePolicy.Discovery.Ssdp.ageTimeoutMs, 30000);

                    // Address
                    {
                        JSONObject address = new JSONObject();

                        address.put(Engine.JsonFields.Address.address, "239.255.255.250");
                        address.put(Engine.JsonFields.Address.port, 1900);

                        ssdp.put(Engine.JsonFields.Address.objectName, address);
                    }

                    discovery.put(Engine.JsonFields.EnginePolicy.Discovery.Ssdp.objectName, ssdp);
                }

                // Cistech
                if(_discoverCistechGv1Assets)
                {
                    JSONObject cistech = discovery.optJSONObject(Engine.JsonFields.EnginePolicy.Discovery.Cistech.objectName);
                    if(cistech == null)
                    {
                        cistech = new JSONObject();
                    }

                    cistech.put(Engine.JsonFields.EnginePolicy.Discovery.Cistech.enabled, true);
                    cistech.put(Engine.JsonFields.EnginePolicy.Discovery.Cistech.ageTimeoutMs, _cistechGv1DiscoveryTimeoutSecs * 1000);

                    // Address
                    {
                        JSONObject address = new JSONObject();

                        address.put(Engine.JsonFields.Address.address, _cistechGv1DiscoveryAddress);
                        address.put(Engine.JsonFields.Address.port, _cistechGv1DiscoveryPort);

                        cistech.put(Engine.JsonFields.Address.objectName, address);
                    }

                    discovery.put(Engine.JsonFields.EnginePolicy.Discovery.Cistech.objectName, cistech);
                }

                // Trellisware
                if(_discoverTrelliswareAssets)
                {
                    JSONObject trellisware = discovery.optJSONObject(Engine.JsonFields.EnginePolicy.Discovery.Trellisware.objectName);
                    if(trellisware == null)
                    {
                        trellisware = new JSONObject();
                    }

                    trellisware.put(Engine.JsonFields.EnginePolicy.Discovery.Trellisware.enabled, true);

                    discovery.put(Engine.JsonFields.EnginePolicy.Discovery.Trellisware.objectName, trellisware);
                }

                rc.put(Engine.JsonFields.EnginePolicy.Discovery.objectName, discovery);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            rc = null;
        }

        return rc;
    }

    public JSONObject makeIdentityObject()
    {
        JSONObject rc = new JSONObject();

        try
        {
            rc.put(Engine.JsonFields.Identity.nodeId, _nodeId);
            rc.put(Engine.JsonFields.Identity.userId, _userId);
            rc.put(Engine.JsonFields.Identity.displayName, _userDisplayName);
            rc.put(Engine.JsonFields.Identity.avatar, "");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            rc = null;
        }

        return rc;
    }

    public PresenceDescriptor processNodeDiscovered(String nodeJson)
    {
        Log.d(TAG, "processNodeDiscovered > nodeJson=" + nodeJson);//NON-NLS

        PresenceDescriptor pd;

        try
        {
            PresenceDescriptor discoveredPd = new PresenceDescriptor();
            if(discoveredPd.deserialize(nodeJson))
            {
                synchronized (_nodes)
                {
                    pd = _nodes.get(discoveredPd.nodeId);

                    if(pd != null)
                    {
                        pd.updateFromPresenceDescriptor(discoveredPd);
                    }
                    else
                    {
                        _nodes.put(discoveredPd.nodeId, discoveredPd);
                        pd = discoveredPd;
                    }

                    Log.d(TAG, "processNodeDiscovered > nid=" + discoveredPd.nodeId + ", u=" + discoveredPd.userId + ", d=" + discoveredPd.displayName);//NON-NLS
                }
            }
            else
            {
                Log.w(TAG, "failed to parse node information");//NON-NLS
                pd = null;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            pd = null;
        }

        return pd;
    }

    public PresenceDescriptor processNodeUndiscovered(String nodeJson)
    {
        PresenceDescriptor pd;

        try
        {
            pd = new PresenceDescriptor();
            if(pd.deserialize(nodeJson))
            {
                synchronized (_nodes)
                {
                    _nodes.remove(pd.nodeId);

                    Log.d(TAG, "processNodeUndiscovered < nid=" + pd.nodeId + ", u=" + pd.userId + ", d=" + pd.displayName);//NON-NLS
                }
            }
            else
            {
                Log.w(TAG, "failed to parse node information");//NON-NLS
                pd = null;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            pd = null;
        }

        return pd;
    }

    public ArrayList<GroupDescriptor> getSelectedGroups()
    {
        ArrayList<GroupDescriptor> rc = new ArrayList<>();

        if(_missionGroups != null)
        {
            for (GroupDescriptor dm : _missionGroups)
            {
                if(_uiMode == Constants.UiMode.vSingle)
                {
                    if(dm.selectedForSingleView)
                    {
                        rc.add(dm);
                        break;
                    }
                }
                else if(_uiMode == Constants.UiMode.vMulti)
                {
                    if(dm.selectedForMultiView)
                    {
                        rc.add(dm);
                    }
                }
            }
        }

        return rc;
    }

    public static void installMissionJson(Context ctx, String json, boolean allowOverwrite)
    {
        ActiveConfiguration ac = new ActiveConfiguration();
        if(ac.parseTemplate(json))
        {
            // Open the mission database
            MissionDatabase database = MissionDatabase.load(Globals.getSharedPreferences(), Constants.MISSION_DATABASE_NAME);
            if(database != null)
            {
                // Find the mission.
                DatabaseMission dbm = database.getMissionById(ac.getMissionId());
                if(allowOverwrite || dbm != null)
                {
                    if( database.addOrUpdateMissionFromActiveConfiguration(ac) )
                    {
                        database.save(Globals.getSharedPreferences(), Constants.MISSION_DATABASE_NAME);
                        if(ctx != null)
                        {
                            Toast.makeText(ctx, R.string.installed_the_mission, Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        if(ctx != null)
                        {
                            Toast.makeText(ctx, R.string.failed_to_install_the_mission, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            else
            {
                if(ctx != null)
                {
                    Toast.makeText(ctx, R.string.cannot_open_mission_database, Toast.LENGTH_SHORT).show();
                }
            }

        }
        else
        {
            if(ctx != null)
            {
                Toast.makeText(ctx, R.string.cannot_parse_mission_template, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static ActiveConfiguration parseLegacyQrCode(String str, String pwd) throws Exception
    {
        String encryptedString = str;

        // Look for the "/??" to see if there's a deflection URL
        int endOfDeflection = encryptedString.indexOf("/??");

        // If it's there, strip it off
        if (endOfDeflection > 0)
        {
            encryptedString = encryptedString.substring(endOfDeflection + 3);
        }

        // Now we have a string with is Base91 encoded, we need to decode that
        byte[] base91DecodedBytes = Base91.decode(encryptedString.getBytes(Utils.getEngageCharSet()));
        if (base91DecodedBytes == null)
        {
            throw new SimpleMessageException(Globals.getEngageApplication().getString(R.string.qr_scan_decode_failed));
        }

        // It may be encrypted, so decrypt if we have a password
        if (!Utils.isEmptyString(pwd))
        {
            String pwdHexString = Utils.toHexString(pwd.getBytes(Utils.getEngageCharSet()));

            base91DecodedBytes = Globals.getEngageApplication().getEngine().decryptSimple(base91DecodedBytes, pwdHexString);
            if (base91DecodedBytes == null)
            {
                throw new SimpleMessageException(Globals.getEngageApplication().getString(R.string.qr_scan_decrypt_failed));
            }
        }

        // Next, we decompress the data
        byte[] decompressed = Utils.inflate(base91DecodedBytes);
        if (decompressed == null)
        {
            throw new SimpleMessageException(Globals.getEngageApplication().getString(R.string.qr_scan_decompress_failed));
        }

        // Now we have a string which should have a valid header
        String qrCodeDataString = new String(decompressed, Utils.getEngageCharSet());

        // Make sure it has our header
        if (!qrCodeDataString.startsWith(Constants.QR_CODE_HEADER))
        {
            throw new SimpleMessageException(Globals.getEngageApplication().getString(R.string.qr_scan_invalid));
        }

        // Strip the first part of the header
        qrCodeDataString = qrCodeDataString.substring(Constants.QR_CODE_HEADER.length());

        // Now, check the version - its "nnn"
        int checkVersion = Integer.parseInt(Constants.QR_VERSION);
        int qrVersion = Integer.parseInt(qrCodeDataString.substring(0, 3));
        if (qrVersion != checkVersion)
        {
            throw new SimpleMessageException(Globals.getEngageApplication().getString(R.string.qr_scn_invalid_version));
        }

        // Strip the version
        qrCodeDataString = qrCodeDataString.substring(3);

        // Finally, we have our JSON data as a string, create that object and handle it
        ActiveConfiguration ac = new ActiveConfiguration();
        if (!ac.parseTemplate(qrCodeDataString))
        {
            throw new SimpleMessageException(Globals.getEngageApplication().getString(R.string.qr_cannot_parse));
        }

        return ac;
    }

    private static ActiveConfiguration parseNewQrCode(String str, String pwd) throws Exception
    {
        return null;
        /* TODO: parseNewQrCode
        String workingStr = str;

        // Strip off the header
        workingStr = workingStr.substring(Constants.QR_CODE_HEADER.length() + Constants.QR_VERSION.length());

        // The rest is a byte array, put our chars into it
        byte[] ba = workingStr.get

        // Look for the "/??" to see if there's a deflection URL
        int endOfDeflection = encryptedString.indexOf("/??");

        // If it's there, strip it off
        if (endOfDeflection > 0)
        {
            encryptedString = encryptedString.substring(endOfDeflection + 3);
        }

        // Now we have a string with is Base91 encoded, we need to decode that
        byte[] base91DecodedBytes = Base91.decode(encryptedString.getBytes(Utils.getEngageCharSet()));
        if (base91DecodedBytes == null)
        {
            throw new SimpleMessageException(Globals.getEngageApplication().getString(R.string.qr_scan_decode_failed));
        }

        // It may be encrypted, so decrypt if we have a password
        if (!Utils.isEmptyString(pwd))
        {
            String pwdHexString = Utils.toHexString(pwd.getBytes(Utils.getEngageCharSet()));

            base91DecodedBytes = Globals.getEngageApplication().getEngine().decryptSimple(base91DecodedBytes, pwdHexString);
            if (base91DecodedBytes == null)
            {
                throw new SimpleMessageException(Globals.getEngageApplication().getString(R.string.qr_scan_decrypt_failed));
            }
        }

        // Next, we decompress the data
        byte[] decompressed = Utils.inflate(base91DecodedBytes);
        if (decompressed == null)
        {
            throw new SimpleMessageException(Globals.getEngageApplication().getString(R.string.qr_scan_decompress_failed));
        }

        // Now we have a string which should have a valid header
        String qrCodeDataString = new String(decompressed, Utils.getEngageCharSet());

        // Make sure it has our header
        if (!qrCodeDataString.startsWith(Constants.QR_CODE_HEADER))
        {
            throw new SimpleMessageException(Globals.getEngageApplication().getString(R.string.qr_scan_invalid));
        }

        // Strip the first part of the header
        qrCodeDataString = qrCodeDataString.substring(Constants.QR_CODE_HEADER.length());

        // Now, check the version - its "nnn"
        int checkVersion = Integer.parseInt(Constants.QR_VERSION);
        int qrVersion = Integer.parseInt(qrCodeDataString.substring(0, 3));
        if (qrVersion != checkVersion)
        {
            throw new SimpleMessageException(Globals.getEngageApplication().getString(R.string.qr_scn_invalid_version));
        }

        // Strip the version
        qrCodeDataString = qrCodeDataString.substring(3);

        // Finally, we have our JSON data as a string, create that object and handle it
        ActiveConfiguration ac = new ActiveConfiguration();
        if (!ac.parseTemplate(qrCodeDataString))
        {
            throw new SimpleMessageException(Globals.getEngageApplication().getString(R.string.qr_cannot_parse));
        }

        return ac;
        */
    }

    public static ActiveConfiguration parseEncryptedQrCodeString(String str, String pwd) throws Exception
    {
        if(str.startsWith(Constants.QR_CODE_HEADER))
        {
            return parseNewQrCode(str, pwd);
        }
        else
        {
            return parseLegacyQrCode(str, pwd);
        }
    }

    public static ActiveConfiguration loadFromDatabaseMission(DatabaseMission mission)
    {
        ActiveConfiguration rc;

        try
        {
            rc = new ActiveConfiguration();

            // Mission itself
            rc._missionId = mission._id;
            rc._missionModPin = mission._modPin;
            rc._missionName = mission._name;
            rc._missionDescription = mission._description;

            rc._useRP = mission._useRp;
            rc._rpAddress = mission._rpAddress;
            rc._rpPort = mission._rpPort;
            rc._multicastFailoverPolicy = mission._multicastFailoverPolicy;

            GroupDescriptor gd;
            JSONObject groupObject;
            JSONObject rxTx;
            JSONObject txAudio;

            // Presence group if we have one
            if(!Utils.isEmptyString(mission._mcId))
            {
                gd = new GroupDescriptor();
                gd.id = mission._mcId;
                gd.type = GroupDescriptor.Type.gtPresence;
                gd.name = "$MISSIONCONTROL$." + mission._id;//NON-NLS

                groupObject = new JSONObject();
                groupObject.put(Engine.JsonFields.Group.id, gd.id);
                groupObject.put(Engine.JsonFields.Group.name, gd.name);
                groupObject.put(Engine.JsonFields.Group.type, GroupDescriptor.Type.gtPresence.ordinal());
                groupObject.put(Engine.JsonFields.Group.cryptoPassword, mission._mcCryptoPassword);

                rxTx = new JSONObject();
                rxTx.put(Engine.JsonFields.Rx.address, mission._mcAddress);
                rxTx.put(Engine.JsonFields.Rx.port, mission._mcPort);
                groupObject.put(Engine.JsonFields.Rx.objectName, rxTx);

                rxTx = new JSONObject();
                rxTx.put(Engine.JsonFields.Tx.address, mission._mcAddress);
                rxTx.put(Engine.JsonFields.Tx.port, mission._mcPort);
                groupObject.put(Engine.JsonFields.Tx.objectName, rxTx);

                gd.jsonConfiguration = groupObject.toString();
                rc._missionGroups.add(gd);
            }

            // Audio groups
            {
                for(DatabaseGroup dbg : mission._groups)
                {
                    if(!Utils.isEmptyString(dbg._id))
                    {
                        gd = new GroupDescriptor();
                        gd.id = dbg._id;
                        gd.type = GroupDescriptor.Type.gtAudio;
                        gd.name = dbg._name;

                        groupObject = new JSONObject();
                        groupObject.put(Engine.JsonFields.Group.id, gd.id);
                        groupObject.put(Engine.JsonFields.Group.name, gd.name);
                        groupObject.put(Engine.JsonFields.Group.type, GroupDescriptor.Type.gtAudio.ordinal());
                        if (dbg._useCrypto && !Utils.isEmptyString(dbg._cryptoPassword))
                        {
                            groupObject.put(Engine.JsonFields.Group.cryptoPassword, dbg._cryptoPassword);
                        }

                        rxTx = new JSONObject();
                        rxTx.put(Engine.JsonFields.Rx.address, dbg._rxAddress);
                        rxTx.put(Engine.JsonFields.Rx.port, dbg._rxPort);
                        groupObject.put(Engine.JsonFields.Rx.objectName, rxTx);

                        rxTx = new JSONObject();
                        rxTx.put(Engine.JsonFields.Tx.address, dbg._txAddress);
                        rxTx.put(Engine.JsonFields.Tx.port, dbg._txPort);
                        groupObject.put(Engine.JsonFields.Tx.objectName, rxTx);

                        txAudio = new JSONObject();
                        txAudio.put(Engine.JsonFields.TxAudio.encoder, dbg._txCodecId);
                        txAudio.put(Engine.JsonFields.TxAudio.framingMs, dbg._txFramingMs);
                        txAudio.put(Engine.JsonFields.TxAudio.noHdrExt, dbg._noHdrExt);
                        txAudio.put(Engine.JsonFields.TxAudio.fdx, dbg._fdx);
                        txAudio.put(Engine.JsonFields.TxAudio.maxTxSecs, dbg._maxTxSecs);
                        groupObject.put(Engine.JsonFields.TxAudio.objectName, txAudio);

                        gd.jsonConfiguration = groupObject.toString();
                        rc._missionGroups.add(gd);
                    }
                }
            }
        }
        catch (Exception e )
        {
            rc = null;
        }

        return rc;
    }



    public void updateGroupStates(ActiveConfiguration ac)
    {
        if(ac == null || ac._missionGroups == null || ac._missionGroups.size() == 0 ||
           _missionGroups == null || _missionGroups.size() == 0)
        {
            return;
        }

        for(GroupDescriptor prevGroup : ac._missionGroups)
        {
            for(GroupDescriptor thisGroup : _missionGroups)
            {
                if(prevGroup.id.compareTo(thisGroup.id) == 0)
                {
                    thisGroup.updateStateFrom(prevGroup);
                }
            }
        }
    }
}
