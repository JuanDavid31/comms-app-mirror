package com.upstart13.legba.fragment;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.upstart13.legba.R;
import com.upstart13.legba.databinding.FragmentMyMissionsBinding;

public class MyMissionsFragment extends Fragment {

    FragmentMyMissionsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_missions, container, false);

        //TODO: View manipulation.

        return binding.getRoot();
    }
}