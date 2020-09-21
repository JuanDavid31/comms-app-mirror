package com.upstart13.legba.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.upstart13.legba.MainActivity;
import com.upstart13.legba.R;
import com.upstart13.legba.data.DataManager;
import com.upstart13.legba.data.dto.Mission;
import com.upstart13.legba.databinding.FragmentMissionsListBinding;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class MissionsListFragment extends Fragment {

    FragmentMissionsListBinding binding;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_missions_list, container, false);
        setupToolbar();
        List<Mission> missions = new DataManager().getMissions();
        MissionsRecyclerViewAdapter adapter = new MissionsRecyclerViewAdapter(new MissionsRecyclerViewAdapter.AdapterDiffCallback(), this);
        binding.missionsListRecyclerView.setHasFixedSize(true);
        binding.missionsListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.missionsListRecyclerView.setAdapter(adapter);
        adapter.setMissions(missions);
        return binding.getRoot();
    }

    private void setupToolbar() {
        ((TextView) requireActivity().findViewById(R.id.toolbar_title_text)).setText(R.string.mission_list_title);

        MainActivity mainActivity = (MainActivity) requireActivity();
        ActionBar actionBar = mainActivity.getSupportActionBar();
        Objects.requireNonNull(actionBar)
                .setHomeAsUpIndicator(R.drawable.ic_hamburguer_icon);
    }
}