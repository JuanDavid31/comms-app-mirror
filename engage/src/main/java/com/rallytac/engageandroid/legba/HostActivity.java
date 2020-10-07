package com.rallytac.engageandroid.legba;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.rallytac.engageandroid.R;

import java.util.Objects;

import timber.log.Timber;

public class HostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        setSupportActionBar(findViewById(R.id.toolbar));
        setupConf();
    }

    public void setupConf(){
        NavController navController = Navigation.findNavController(this, R.id.host_fragment);

        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph())
                        //.setOpenableLayout((Openable) findViewById(R.id.drawer))
                        .build();

        setupDrawerConfiguration(navController);
        setupToolbarConfiguration(navController, appBarConfiguration);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            Timber.i("onDestinationChangedListener");
            findViewById(R.id.logo_image).setVisibility(View.GONE);
            findViewById(R.id.toolbar_title_text).setVisibility(View.GONE);
            findViewById(R.id.fragment_description).setVisibility(View.GONE);
        });
    }

    private void setupDrawerConfiguration(NavController navController) {
        NavigationView navView = findViewById(R.id.nav_view);
        NavigationUI.setupWithNavController(navView, navController);
    }

    private void setupToolbarConfiguration(NavController navController, AppBarConfiguration appBarConfiguration) {
        Objects.requireNonNull(getSupportActionBar())
                .setDisplayShowTitleEnabled(false);

        Toolbar toolbar = findViewById(R.id.toolbar);

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