package com.upstart13.legba;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.customview.widget.Openable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        setupConf();
    }

    public void setupConf(){
        NavController navController = Navigation.findNavController(this, R.id.host_fragment);

        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph())
                        .setOpenableLayout((Openable) findViewById(R.id.drawer))
                        .build();

        setupToolbarConfiguration(navController, appBarConfiguration);
        setupDrawerConfiguration(navController);
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


}