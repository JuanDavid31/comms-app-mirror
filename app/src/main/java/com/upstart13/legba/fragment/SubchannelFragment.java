package com.upstart13.legba.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.upstart13.legba.HostActivity;
import com.upstart13.legba.R;
import com.upstart13.legba.data.dto.Subchannel;
import com.upstart13.legba.databinding.FragmentSubchannelBinding;

import java.util.Objects;

import timber.log.Timber;

public class SubchannelFragment extends Fragment {

    private Subchannel subchannel;
    private FragmentSubchannelBinding binding;

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_subchannel, container, false);

        MembersRecyclerViewAdapter adapter =
                new MembersRecyclerViewAdapter(new MembersRecyclerViewAdapter.AdapterDiffCallback(), this);
        binding.memberElementsRecycler.setHasFixedSize(true);
        binding.memberElementsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.memberElementsRecycler.setAdapter(adapter);
        adapter.setMembers(subchannel.members);

        return binding.getRoot();
    }

    private void updateToolbar() {
        requireActivity().findViewById(R.id.toolbar_title_text).setVisibility(View.VISIBLE);
        ((TextView) requireActivity().findViewById(R.id.toolbar_title_text)).setText(subchannel.name);

        requireActivity().findViewById(R.id.fragment_description).setVisibility(View.VISIBLE);
        ((TextView)requireActivity().findViewById(R.id.fragment_description)).setText("Subchannel");
        ((TextView)requireActivity().findViewById(R.id.fragment_description)).setTextColor(this.getResources().getColor(R.color.white));
        Objects.requireNonNull(((HostActivity) requireActivity()).getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_round_keyboard_arrow_left_24);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.channel_fragment_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.notifications_action).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }
}