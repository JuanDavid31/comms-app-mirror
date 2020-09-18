package com.upstart13.legba.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.upstart13.legba.MainActivity;
import com.upstart13.legba.R;
import com.upstart13.legba.data.dto.Mission;

import java.util.Objects;


public class MissionFragment extends Fragment {

    private Mission mission;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MissionFragmentArgs missionFragmentArgs = MissionFragmentArgs.fromBundle(requireArguments());
        mission = missionFragmentArgs.getMission();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        updateToolbar();
        return inflater.inflate(R.layout.fragment_mission, container, false);
    }

    private void updateToolbar() {
        ((TextView) requireActivity().findViewById(R.id.toolbar_title_text)).setText(mission.name);
        Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_round_keyboard_arrow_left_24);
    }
}