package com.rallytac.engageandroid.legba;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.SoundPool;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.customview.widget.Openable;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.navigation.NavigationView;
import com.rallytac.engageandroid.AboutActivity;
import com.rallytac.engageandroid.ActiveConfiguration;
import com.rallytac.engageandroid.BuildConfig;
import com.rallytac.engageandroid.Globals;
import com.rallytac.engageandroid.GroupSelectorAdapter;
import com.rallytac.engageandroid.MapTracker;
import com.rallytac.engageandroid.PreferenceKeys;
import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.SettingsActivity;
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
        Log.w("LEGBA SAYS:", _ac.getSelectedGroups().size() + "");
        Log.w("License time left", Globals.getEngageApplication().getLicenseSecondsLeft() + "");
    }

    public void setupConf() {
        NavController navController = Navigation.findNavController(this, R.id.host_fragment);
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph())
                        .setOpenableLayout((Openable) findViewById(R.id.drawer))
                        .build();

        setupDrawerConfiguration(navController);
        setupToolbarConfiguration(navController, appBarConfiguration);

        NavigationUI.setupWithNavController(binding.navView, navController);
        setNavigationDrawer();
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            Timber.i("onDestinationChangedListener");
            binding.logoImage.setVisibility(View.GONE);
            binding.toolbarTitleText.setVisibility(View.GONE);
            binding.fragmentDescription.setVisibility(View.GONE);
            binding.showCreateEditChannelsGroupButton.setVisibility(View.GONE);
        });
        setHeaderLayoutNavigationView();
    }

    private void setNavigationDrawer() {
        // Version bottom text.
        TextView navHeaderVersion = findViewById(R.id.nav_header_version);
        int shutdownColor = ContextCompat.getColor(this, R.color.shutdown_color);

        String[] a = getString(R.string.nav_drawer_version).split("\n");
        String[] b = a[0].split(" ");
        String version = BuildConfig.VERSION_NAME;
        String versionText = "  " + b[0] + " " + version + "\n" + "  " + a[1];

        Spannable spannable = new SpannableString(versionText);
        spannable.setSpan(
                new ForegroundColorSpan(shutdownColor),
                10, (10 + version.length() + 1), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        navHeaderVersion.setText(spannable);

        // Background.
        Drawable blackDrawable = ContextCompat.getDrawable(this, R.color.navigationViewBlack);
        binding.navView.setBackground(blackDrawable);

        // Shutdown item.
        MenuItem shutdownItem = binding.navView.getMenu().getItem(5);
        String shutdownText = shutdownItem.getTitle().toString();
        setFont("font/open_sans_semi_bold.ttf", "  " + shutdownText, shutdownItem);
        shutdownItem.setActionView(R.layout.menu_shutdown_image);

        SpannableString spanString = new SpannableString(getString(R.string.nav_drawer_shutdown_padding));
        spanString.setSpan(new ForegroundColorSpan(shutdownColor), 0, spanString.length(), 0);
        shutdownItem.setTitle(spanString);

        // Other items.
        for (int i = 0; i < 5; i++) {
            MenuItem currentItem = binding.navView.getMenu().getItem(i);
            currentItem.setActionView(R.layout.menu_drawer_image);
            String currentText = currentItem.getTitle().toString();
            setFont("font/open_sans_semi_bold.ttf", "  " + currentText, currentItem);
        }
    }

    private void setHeaderLayoutNavigationView() {
        String displayName = Globals.getSharedPreferences().getString(PreferenceKeys.USER_DISPLAY_NAME, null);
        String alias = Globals.getSharedPreferences().getString(PreferenceKeys.USER_ALIAS_ID, null);

        View headerLayout = binding.navView.inflateHeaderView(R.layout.nav_header);
        ((TextView) headerLayout.findViewById(R.id.nav_header_alias)).setText(alias);
        ((TextView) headerLayout.findViewById(R.id.nav_header_display_name)).setText(displayName.toUpperCase());
        ((TextView) headerLayout.findViewById(R.id.nav_header_display_team)).setText(getString(R.string.nav_drawer_team));

        headerLayout.findViewById(R.id.nav_header_close_image).setOnClickListener(v -> {
            DrawerLayout drawerLayout = findViewById(R.id.drawer);
            drawerLayout.closeDrawers();
        });
    }

    private void setupDrawerConfiguration(NavController navController) {
        NavigationView navView = binding.navView;
        NavigationUI.setupWithNavController(navView, navController);

        Menu menu = binding.navView.getMenu();

        menu.findItem(R.id.drawer_settings_action)
                .setOnMenuItemClickListener(menuItem -> {
                    Intent intent = new Intent(this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                });

        menu.findItem(R.id.drawer_about_action)
                .setOnMenuItemClickListener(menuItem -> {
                    Intent intent = new Intent(this, AboutActivity.class);
                    startActivity(intent);
                    return true;
                });
    }

    private void setFont(String fontType, String text, MenuItem menuItem) {
        TypefaceSpan face = new TypefaceSpan(fontType);
        SpannableStringBuilder title = new SpannableStringBuilder(text);
        title.setSpan(face, 0, title.length(), 0);
        menuItem.setTitle(title);
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