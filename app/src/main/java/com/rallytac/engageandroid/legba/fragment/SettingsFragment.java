package com.rallytac.engageandroid.legba.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.databinding.FragmentSettingsBinding;
import com.rallytac.engageandroid.legba.HostActivity;

import timber.log.Timber;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupToolbar();
    }

    private void setupToolbar() {
        Timber.i("updateToolbar");
        requireActivity().findViewById(R.id.toolbar_title_text).setVisibility(View.VISIBLE);

        String title = requireActivity().getString(R.string.nav_drawer_settings);
        ((TextView)requireActivity().findViewById(R.id.toolbar_title_text)).setText(title);
        HostActivity hostActivity = (HostActivity)requireActivity();
        ActionBar actionBar = hostActivity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_close_toolbar);
    }
}