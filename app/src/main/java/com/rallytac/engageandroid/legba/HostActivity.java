package com.rallytac.engageandroid.legba;

import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.navigation.NavigationView;
import com.rallytac.engageandroid.ActiveConfiguration;
import com.rallytac.engageandroid.Globals;
import com.rallytac.engageandroid.GroupSelectorAdapter;
import com.rallytac.engageandroid.MapTracker;
import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.SimpleUiMainActivity;
import com.rallytac.engageandroid.VolumeLevels;
import com.rallytac.engageandroid.databinding.ActivityHostBinding;

import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;

import timber.log.Timber;

public class HostActivity extends AppCompatActivity {

    private static String TAG = SimpleUiMainActivity.class.getSimpleName();

    private static int SETTINGS_REQUEST_CODE = 42;
    private static int MISSION_LISTING_REQUEST_CODE = 43;
    private static int PICK_MISSION_FILE_REQUEST_CODE = 44;
    private static int CERTIFICATE_MANAGER_REQUEST_CODE = 45;
    private static int ENGINE_POLICY_EDIT_REQUEST_CODE = 46;

    private ActiveConfiguration _ac = null;
    private Timer _waitForEngineStartedTimer = null;
    private boolean _anyTxActive = false;
    private boolean _anyTxPending = false;
    private Animation _notificationBarAnimation = null;
    private Runnable _actionOnNotificationBarClick = null;
    private boolean _pttRequested = false;
    private boolean _pttHardwareButtonDown = false;

    private Animation _licensingBarAnimation = null;
    private Runnable _actionOnLicensingBarClick = null;

    private Animation _humanBiometricsAnimation = null;
    private Runnable _actionOnHumanBiometricsClick = null;

    private long _lastHeadsetKeyhookDown = 0;

    private VolumeLevels _vlSaved;
    private VolumeLevels _vlInProgress;
    private boolean _volumeSynced = true;
    private SeekBar _volumeSliderLastMoved = null;

    private boolean _optAllowMultipleChannelView = true;

    private GoogleMap _map;
    private boolean _firstCameraPositioningDone = false;
    private HashMap<String, MapTracker> _mapTrackers = new HashMap<>();

    private RecyclerView _groupSelectorView = null;
    private GroupSelectorAdapter _groupSelectorAdapter = null;

    private int _keycodePtt = 0;
    private SoundPool _soundpool = null;

    public ActivityHostBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_host);
        setSupportActionBar(binding.toolbar);
        setupConf();
        setUpEngage();
    }

    private void setUpEngage() {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        _ac = Globals.getEngageApplication().getActiveConfiguration();
        Log.w("LEGBA SAYS:",_ac.getSelectedGroups().size()+"");
        Log.w("License time left",Globals.getEngageApplication().getLicenseSecondsLeft()+"");
    }

    public void setupConf(){
        NavController navController = Navigation.findNavController(this, R.id.host_fragment);

        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph())
                        .build();

        setupDrawerConfiguration(navController);
        setupToolbarConfiguration(navController, appBarConfiguration);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            Timber.i("onDestinationChangedListener");
            binding.logoImage.setVisibility(View.GONE);
            binding.toolbarTitleText.setVisibility(View.GONE);
            binding.fragmentDescription.setVisibility(View.GONE);
            binding.editCurrentChannelGroupButton.setVisibility(View.GONE);
        });
    }

    private void setupDrawerConfiguration(NavController navController) {
        NavigationView navView = binding.navView;
        NavigationUI.setupWithNavController(navView, navController);
    }

    private void setupToolbarConfiguration(NavController navController, AppBarConfiguration appBarConfiguration) {
        Objects.requireNonNull(getSupportActionBar())
                .setDisplayShowTitleEnabled(false);

        Toolbar toolbar = binding.toolbar;

        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

}