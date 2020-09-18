package com.upstart13.legba.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.upstart13.legba.MainActivity;
import com.upstart13.legba.R;
import com.upstart13.legba.data.DataManager;
import com.upstart13.legba.data.dto.Mission;
import com.upstart13.legba.databinding.FragmentMyMissionsBinding;

import java.util.List;
import java.util.Objects;

public class MyMissionsFragment extends Fragment {

    FragmentMyMissionsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_missions, container, false);
        //TODO: Change 
        List<Mission> missions = new DataManager().getMissions();
        MissionsRecyclerViewAdapter adapter = new MissionsRecyclerViewAdapter(new MissionsRecyclerViewAdapter.AdapterDiffCallback(), getContext());
        binding.missionsListRecyclerView.setHasFixedSize(true);
        binding.missionsListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.missionsListRecyclerView.setAdapter(adapter);
        adapter.setMissions(missions);
        return binding.getRoot();
    }

}