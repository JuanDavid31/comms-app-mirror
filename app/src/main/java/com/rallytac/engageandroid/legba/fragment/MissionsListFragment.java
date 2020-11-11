package com.rallytac.engageandroid.legba.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.SettingsActivity;
import  com.rallytac.engageandroid.legba.HostActivity;

import  com.rallytac.engageandroid.legba.data.DataManager;
import  com.rallytac.engageandroid.legba.data.dto.Mission;
import  com.rallytac.engageandroid.databinding.FragmentMissionsListBinding;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class MissionsListFragment extends Fragment {

    FragmentMissionsListBinding binding;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_missions_list, container, false);
        setupToolbar();
        List<Mission> missions = DataManager.getInstance(getContext()).getMissions();
        MissionsRecyclerViewAdapter adapter = new MissionsRecyclerViewAdapter(new MissionsRecyclerViewAdapter.AdapterDiffCallback(), this);
        binding.missionsListRecyclerView.setHasFixedSize(true);

        int orientation = getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT){
            binding.missionsListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }else if(orientation == Configuration.ORIENTATION_LANDSCAPE){
            binding.missionsListRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }


        binding.missionsListRecyclerView.setAdapter(adapter);
        adapter.setMissions(missions, getContext());
        setupNFCActionsView();
        return binding.getRoot();
    }

    private void setupToolbar() {
        Timber.i("updateToolbar");
        requireActivity().findViewById(R.id.toolbar_title_text).setVisibility(View.VISIBLE);

        ((TextView)requireActivity().findViewById(R.id.toolbar_title_text)).setText("My Missions");
        HostActivity hostActivity = (HostActivity) requireActivity();
        ActionBar actionBar = hostActivity.getSupportActionBar();

        Objects.requireNonNull(actionBar)
                .setHomeAsUpIndicator(R.drawable.ic_hamburguer_icon);
    }

    private void setupNFCActionsView() {
        HostActivity hostActivity = (HostActivity) requireActivity();
        hostActivity.binding.logout.setOnClickListener(view -> {
            hostActivity.binding.drawer.closeDrawers();
            NavDirections action = MissionsListFragmentDirections.actionMissionsFragmentToLargeCardFragment();
            NavHostFragment.findNavController(this).navigate(action);
        });
    }
}