//
//  Copyright (c) 2019 Rally Tactical Systems, Inc.
//  All rights reserved.
//

package com.rallytac.engageandroid;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import androidx.appcompat.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;


import com.rallytac.engage.engine.Engine;
import com.rallytac.engageandroid.legba.data.DataManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

public class SettingsActivity extends AppCompatPreferenceActivity
{
    private static String TAG = SettingsActivity.class.getSimpleName();

    public static int MISSION_CHANGED_RESULT = (RESULT_FIRST_USER + 1);
    private static SettingsActivity _thisActivity;
    private static boolean _prefChangeIsBeingForcedByBinding = false;

    private void indicateMissionChanged()
    {
        ((EngageApplication) getApplication()).setMissionChangedStatus(true);
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener()
    {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value)
        {
            String stringValue = value.toString();

            String preferenceKey = preference.getKey();

            if (preferenceKey.equalsIgnoreCase("user_displayName")
                    || preferenceKey.equalsIgnoreCase("user_id")){ // Legba logic
                try{
                    DataManager.getInstance().updatePresenceDescriptor();
                }catch (IllegalStateException e){
                    e.printStackTrace();
                }
            }

            if(!_prefChangeIsBeingForcedByBinding)
            {
                String key = preference.getKey();
                if(key.startsWith("rallypoint_")//NON-NLS
                    || key.startsWith("user_")//NON-NLS
                    || key.startsWith("network_")//NON-NLS
                    || key.startsWith("mission_"))//NON-NLS
                {
                    Log.i(TAG, "mission parameters changed");//NON-NLS
                    _thisActivity.indicateMissionChanged();
                }
            }

            if (preference instanceof ListPreference)
            {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            }
            else
            {
                if( !(preference instanceof SwitchPreference) )
                {
                    preference.setSummary(stringValue);
                }
            }
            return true;
        }
    };

    private static boolean isXLargeTablet(Context context)
    {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static void bindPreferenceSummaryToValue(Preference preference)
    {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        _prefChangeIsBeingForcedByBinding = true;

        if(!(preference instanceof SwitchPreference))
        {
            String strVal;

            String className = preference.getClass().toString();

            if(className.contains("SeekBarPreference"))//NON-NLS
            {
                strVal = Integer.toString(PreferenceManager
                                    .getDefaultSharedPreferences(preference.getContext())
                                    .getInt(preference.getKey(), 0));
            }
            else
            {
                strVal = PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), "");
            }

            strVal = Utils.trimString(strVal);

            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, strVal);
        }

        _prefChangeIsBeingForcedByBinding = false;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        _thisActivity = this;
        super.onCreate(savedInstanceState);
        setupActionBar();

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new GeneralPreferenceFragment()).commit();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    private void setupActionBar()
    {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onIsMultiPane()
    {
        return isXLargeTablet(this);
    }

    protected boolean isValidFragment(String fragmentName)
    {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class BasePreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item)
        {
            int id = item.getItemId();
            if (id == android.R.id.home)
            {
                getActivity().onBackPressed();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends BasePreferenceFragment
    {
        private void hidePreferenceCategory(String nm)
        {
            Preference pc = this.findPreference(nm);
            if(pc != null)
            {
                PreferenceScreen ps = this.getPreferenceScreen();
                if(ps != null)
                {
                    ps.removePreference(pc);
                }
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            // Handle whether developer mode is active
            if(Globals.getSharedPreferences().getBoolean(PreferenceKeys.DEVELOPER_MODE_ACTIVE, false))
            {
                bindPreferenceSummaryToValue(findPreference(PreferenceKeys.DEVELOPER_USE_DEV_LICENSING_SYSTEM));
            }
            else
            {
                hidePreferenceCategory("prefcat_developer_options");//NON-NLS
            }

            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_ID));
            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_DISPLAY_NAME));
            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_ALIAS_ID));
            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_TONE_LEVEL_PTT));
            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_TONE_LEVEL_NOTIFICATION));
            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_TONE_LEVEL_ERROR));
            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_LOCATION_SHARED));
            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_LOCATION_ACCURACY));
            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_LOCATION_INTERVAL_SECS));
            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_LOCATION_MIN_DISPLACEMENT));
            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_NOTIFY_NODE_JOIN));
            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_NOTIFY_NODE_LEAVE));
            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_NOTIFY_NEW_AUDIO_RX));
            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_NOTIFY_NETWORK_ERROR));
            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_SPEAKER_OUTPUT_BOOST_FACTOR));

            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_AUDIO_AEC_ENABLED));
            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_AUDIO_AEC_MODE));
            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_AUDIO_AEC_CNG));
            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_AUDIO_AEC_SPEAKER_TAIL_MS));
            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_AUDIO_AEC_DISABLE_STEREO));
            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_AUDIO_JITTER_LOW_LATENCY_ENABLED));

            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_NOTIFY_VIBRATIONS));
            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_NOTIFY_PTT_EVERY_TIME));

            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_UI_PTT_LATCHING));
            bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_UI_PTT_VOICE_CONTROL));

            // NICs
            {
                HashMap<String, String> uniqueNics = new HashMap<>();
                String nicArrayJsonString = Globals.getEngageApplication().getEngine().engageGetNetworkInterfaceDevices();
                JSONArray ar;

                try
                {
                    JSONObject container = new JSONObject(nicArrayJsonString);
                    ar = container.getJSONArray(Engine.JsonFields.ListOfNetworkInterfaceDevice.objectName);

                    for(int idx = 0; idx < ar.length(); idx++)
                    {
                        JSONObject nic = ar.getJSONObject(idx);

                        String name = nic.optString(Engine.JsonFields.NetworkInterfaceDevice.name, null);
                        if(!Utils.isEmptyString(name))
                        {
                            if(!uniqueNics.containsKey(name))
                            {
                                String friendlyName = nic.optString(Engine.JsonFields.NetworkInterfaceDevice.friendlyName, null);
                                if(Utils.isEmptyString(friendlyName))
                                {
                                    friendlyName = name;
                                }

                                uniqueNics.put(name, friendlyName);
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    uniqueNics.clear();
                    e.printStackTrace();
                }
                
                if(uniqueNics.size() > 0)
                {
                    final CharSequence[] entries = new CharSequence[uniqueNics.size()];
                    final CharSequence[] entryValues = new CharSequence[uniqueNics.size()];

                    int index = 0;
                    for (String s : uniqueNics.keySet())
                    {
                        entryValues[index] = s;
                        entries[index] = uniqueNics.get(s);
                        index++;
                    }

                    final ListPreference listPreference = (ListPreference) findPreference(PreferenceKeys.NETWORK_BINDING_NIC_NAME);
                    listPreference.setEntries(entries);
                    listPreference.setEntryValues(entryValues);
                    bindPreferenceSummaryToValue(findPreference(PreferenceKeys.NETWORK_BINDING_NIC_NAME));
                }

                bindPreferenceSummaryToValue(findPreference(PreferenceKeys.NETWORK_MULTICAST_FAILOVER_ENABLED));
                bindPreferenceSummaryToValue(findPreference(PreferenceKeys.NETWORK_MULTICAST_FAILOVER_SECS));
            }



            // Audio devices
            {

                String audioDeviceArrayJsonString = Globals.getEngageApplication().getEngine().engageGetAudioDevices();

                JSONArray ar;
                ArrayList<JSONObject> inputs = new ArrayList<>();
                ArrayList<JSONObject> outputs = new ArrayList<>();

                // Split into inputs and outputs
                try
                {
                    JSONObject container = new JSONObject(audioDeviceArrayJsonString);
                    ar = container.getJSONArray(Engine.JsonFields.ListOfAudioDevice.objectName);

                    for(int idx = 0; idx < ar.length(); idx++)
                    {
                        JSONObject ad = ar.getJSONObject(idx);

                        int direction = ad.getInt(Engine.JsonFields.AudioDevice.direction);
                        int deviceId = ad.getInt(Engine.JsonFields.AudioDevice.deviceId);
                        String name = ad.optString(Engine.JsonFields.AudioDevice.name, null);
                        String hardwareId = ad.optString(Engine.JsonFields.AudioDevice.hardwareId, null);

                        if(deviceId >= 0 && !Utils.isEmptyString(name) && !Utils.isEmptyString(hardwareId))
                        {
                            if(direction == 1)      // dirInput
                            {
                                inputs.add(ad);
                            }
                            else if(direction == 2) // dirOutput
                            {
                                outputs.add(ad);
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                // Fill the input selector
                for(JSONObject ad : inputs)
                {
                    //Log.e(TAG, ad.toString());
                }

                // Fill the output selector
                for(JSONObject ad : outputs)
                {
                    //Log.e(TAG, ad.toString());
                }
            }




            // Bluetooth audio device
            {
                final ListPreference listPreference = (ListPreference) findPreference(PreferenceKeys.USER_BT_DEVICE_ADDRESS);
                ArrayList<BluetoothDevice> btDevs = BluetoothManager.getDevices();
                final CharSequence[] entries = new CharSequence[btDevs.size()];
                final CharSequence[] entryValues = new CharSequence[btDevs.size()];
                int index = 0;
                for (BluetoothDevice dev : btDevs)
                {
                    entries[index] = dev.getName();
                    entryValues[index] = dev.getAddress();
                    index++;
                }
                listPreference.setEntries(entries);
                listPreference.setEntryValues(entryValues);
                bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_BT_DEVICE_ADDRESS));
            }

            if(!Utils.boolOpt(getString(R.string.opt_experimental_general_enabled), false))
            {
                hidePreferenceCategory("prefcat_experimental_general");//NON-NLS
            }
            else
            {
                bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_EXPERIMENT_ENABLE_SSDP_DISCOVERY));
                bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_EXPERIMENT_ENABLE_CISTECH_GV1_DISCOVERY));
                bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_EXPERIMENT_CISTECH_GV1_DISCOVERY_ADDRESS));
                bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_EXPERIMENT_CISTECH_GV1_DISCOVERY_PORT));
                bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_EXPERIMENT_CISTECH_GV1_DISCOVERY_TIMEOUT_SECS));

                bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_EXPERIMENT_ENABLE_TRELLISWARE_DISCOVERY));

                bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_EXPERIMENT_ENABLE_DEVICE_REPORT_CONNECTIVITY));
                bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_EXPERIMENT_ENABLE_DEVICE_REPORT_POWER));
            }


            if(!Utils.boolOpt(getString(R.string.opt_experimental_human_biometrics_enabled), false))
            {
                hidePreferenceCategory("prefcat_experimental_human_biometrics");//NON-NLS
            }
            else
            {
                bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_EXPERIMENT_ENABLE_HBM));
                bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_EXPERIMENT_HBM_INTERVAL_SECS));
                bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_EXPERIMENT_HBM_ENABLE_HEART_RATE));
                bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_EXPERIMENT_HBM_ENABLE_SKIN_TEMP));
                bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_EXPERIMENT_HBM_ENABLE_CORE_TEMP));
                bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_EXPERIMENT_HBM_ENABLE_BLOOD_OXY));
                bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_EXPERIMENT_HBM_ENABLE_BLOOD_HYDRO));
                bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_EXPERIMENT_HBM_ENABLE_FATIGUE_LEVEL));
                bindPreferenceSummaryToValue(findPreference(PreferenceKeys.USER_EXPERIMENT_HBM_ENABLE_TASK_EFFECTIVENESS_LEVEL));
            }
        }
    }
}
