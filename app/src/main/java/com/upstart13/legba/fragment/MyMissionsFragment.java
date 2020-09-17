package com.upstart13.legba.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.upstart13.legba.R;
import com.upstart13.legba.data.DataManager;
import com.upstart13.legba.data.dto.Mission;
import com.upstart13.legba.databinding.FragmentMyMissionsBinding;

import java.util.List;

public class MyMissionsFragment extends Fragment {

    private static final String LOG_TAG = "MyMissionsFragment";
    FragmentMyMissionsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_missions, container, false);
        List<Mission> missions = new DataManager().getMissions();

        MissionsRecyclerViewAdapter adapter = new MissionsRecyclerViewAdapter(new MissionsRecyclerViewAdapter.AdapterDiffCallback(), getContext());
        binding.missionsListRecyclerView.setHasFixedSize(true);
        binding.missionsListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.missionsListRecyclerView.setAdapter(adapter);
        adapter.setMissions(missions);
        return binding.getRoot();
    }
}