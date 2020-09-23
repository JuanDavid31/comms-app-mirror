package com.upstart13.legba.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.upstart13.legba.MainActivity;
import com.upstart13.legba.R;
import com.upstart13.legba.data.DataManager;
import com.upstart13.legba.data.dto.Channel;
import com.upstart13.legba.data.dto.Mission;
import com.upstart13.legba.databinding.FragmentChannelBinding;

import java.util.List;
import java.util.Objects;


public class ChannelFragment extends Fragment {

    private FragmentChannelBinding binding;
    private Channel channel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChannelFragmentArgs channelFragmentArgs = ChannelFragmentArgs.fromBundle(requireArguments());
        channel = channelFragmentArgs.getChannel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        updateToolbar();
        setHasOptionsMenu(true);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_channel, container, false);
        ChannelElementsRecyclerViewAdapter adapter =
                new ChannelElementsRecyclerViewAdapter(new ChannelElementsRecyclerViewAdapter.AdapterDiffCallback(), this);
        binding.channelElementsRecycler.setHasFixedSize(true);
        binding.channelElementsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.channelElementsRecycler.setAdapter(adapter);
        adapter.setSubChannelsAndMembers(channel.subChannelsAndMembers);

        return binding.getRoot();
    }

    private void updateToolbar() {
        ((TextView) requireActivity().findViewById(R.id.toolbar_title_text)).setText(channel.name);
        Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_round_keyboard_arrow_left_24);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.history_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem item = menu.findItem(R.id.history_action);
        View root = item.getActionView();
        root.setOnClickListener(view -> onOptionsItemSelected(item));
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.history_action) {
            Toast.makeText(getContext(), "History pressed", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}