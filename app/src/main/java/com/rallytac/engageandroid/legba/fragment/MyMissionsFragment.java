package com.rallytac.engageandroid.legba.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.databinding.FragmentMyMissionsBinding;

public class MyMissionsFragment extends Fragment {
    private FragmentMyMissionsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_missions, container, false);
        return binding.getRoot();
    }
}