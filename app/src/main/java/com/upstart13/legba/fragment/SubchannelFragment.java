package com.upstart13.legba.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.upstart13.legba.MainActivity;
import com.upstart13.legba.R;
import com.upstart13.legba.data.dto.Subchannel;

import java.util.Objects;

public class SubchannelFragment extends Fragment {

    private Subchannel subchannel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SubchannelFragmentArgs subchannelFragmentArgs = SubchannelFragmentArgs.fromBundle(requireArguments());
        subchannel = subchannelFragmentArgs.getSubchannel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        updateToolbar();
        setHasOptionsMenu(true);


        return inflater.inflate(R.layout.fragment_subchannel, container, false);
    }

    private void updateToolbar() {
        requireActivity().findViewById(R.id.toolbar_title_text).setVisibility(View.VISIBLE);
        ((TextView) requireActivity().findViewById(R.id.toolbar_title_text)).setText(subchannel.name);
        Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_round_keyboard_arrow_left_24);
    }

    @Override
    public void onStop() {
        super.onStop();
        requireActivity().findViewById(R.id.toolbar_title_text).setVisibility(View.INVISIBLE);
    }
}