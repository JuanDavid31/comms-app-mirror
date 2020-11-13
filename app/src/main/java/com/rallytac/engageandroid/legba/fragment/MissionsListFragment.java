package com.rallytac.engageandroid.legba.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.NavHostController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rallytac.engageandroid.EngageApplication;
import com.rallytac.engageandroid.MissionListActivity;
import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.SettingsActivity;
import com.rallytac.engageandroid.legba.HostActivity;

import com.rallytac.engageandroid.legba.data.DataManager;
import com.rallytac.engageandroid.legba.data.dto.Mission;
import com.rallytac.engageandroid.databinding.FragmentMissionsListBinding;
import com.rallytac.engageandroid.legba.data.dto.MissionDao;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import timber.log.Timber;

public class MissionsListFragment extends Fragment {

    FragmentMissionsListBinding binding;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_missions_list, container, false);
        setupToolbar();

        binding.missionsListRecyclerView.setHasFixedSize(true);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.missionsListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.missionsListRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }

        return binding.getRoot();
    }

    private void setupToolbar() {
        Timber.i("updateToolbar");
        setHasOptionsMenu(true);
        requireActivity().findViewById(R.id.toolbar_title_text).setVisibility(View.VISIBLE);

        ((TextView) requireActivity().findViewById(R.id.toolbar_title_text)).setText("My Missions");
        HostActivity hostActivity = (HostActivity) requireActivity();
        ActionBar actionBar = hostActivity.getSupportActionBar();

        Objects.requireNonNull(actionBar)
                .setHomeAsUpIndicator(R.drawable.ic_hamburguer_icon);
    }

/*    private void setupNFCActionsView() {
        HostActivity hostActivity = (HostActivity) requireActivity();
        hostActivity.binding.logout.setOnClickListener(view -> {
            hostActivity.binding.drawer.closeDrawers();
            NavDirections action = MissionsListFragmentDirections.actionMissionsFragmentToLargeCardFragment();
            NavHostFragment.findNavController(this).navigate(action);
        });
    }*/

    @Override
    public void onStart() {
        super.onStart();

        String defaultMissionName = "OVERLORD";

        MissionDao missionDao = ((EngageApplication) getActivity().getApplication())
                .getDaoSession()
                .getMissionDao();

        Mission jsonMission = DataManager
                .getInstance()
                .getMissions()
                .get(0);

        List<Mission> missions = missionDao.loadAll();

        Optional<Mission> overlord = missions.stream()
                .filter(mission -> mission.getName().equalsIgnoreCase(defaultMissionName))
                .findFirst();

        if (overlord.isPresent()) {
            int i = missions.indexOf(overlord.get());
            if (i != 0) {
                Collections.swap(missions, i, 0);
            }
        } else {
            missions.add(0, jsonMission);
        }

        Timber.i("Number of missions %s", missions.size());
        missions.forEach(mission -> Timber.i(mission.toString()));

        MissionsRecyclerViewAdapter adapter = new MissionsRecyclerViewAdapter(new MissionsRecyclerViewAdapter.AdapterDiffCallback(), this);
        binding.missionsListRecyclerView.setAdapter(adapter);
        adapter.setMissions(missions, getContext());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.missions_list_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.create_manually_action) {
            NavHostFragment.findNavController(this)
                    .navigate(MissionsListFragmentDirections.actionMissionsFragmentToMissionEditActivity(null));
            return true;
        } else if (itemId == R.id.load_from_json_action) {
            startLoadMissionFromLocalFile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startLoadMissionFromLocalFile() {
        try {
            int PICK_MISSION_FILE_REQUEST_CODE = 44;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_a_file)), PICK_MISSION_FILE_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}