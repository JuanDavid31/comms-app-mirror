package com.upstart13.legba.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.upstart13.legba.data.dto.Channel;
import com.upstart13.legba.data.dto.Mission;
import com.upstart13.legba.databinding.FragmentMissionBinding;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.upstart13.legba.util.RUtils.getImageResource;


public class MissionFragment extends Fragment {

    private FragmentMissionBinding binding;
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
        setHasOptionsMenu(true);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mission, container, false);


        //TODO: Fix and refactor.
        final String PRIMARY_TYPE = "primary";
        final String PRIORITARY_TYPE = "priority";
        final String RADIO_TYPE = "radio";

        //Primary channel

        Channel primaryChannel = mission.channels
                .stream()
                .filter(channel -> channel.type != null)
                .filter(channel -> channel.type.equals(PRIMARY_TYPE))
                .findFirst()
                .orElse(null);

        if (primaryChannel != null) {
            binding.primaryChannelLayout.setVisibility(View.VISIBLE);
            binding.primaryChannelImage.setImageResource(getImageResource(primaryChannel));
            binding.primaryChannelNameView.setText(primaryChannel.name);
            binding.primaryChannelLinkImage.setOnClickListener(view -> goToChannelFragment(primaryChannel));
        }

        //Priority channels

        List<Channel> priorityChannels = mission.channels
                .stream()
                .filter(channel -> channel.type != null)
                .filter(channel -> channel.type.equals(PRIORITARY_TYPE))
                .collect(Collectors.toList());

        if (priorityChannels.size() > 0) {

            Channel priorityChannel1 = priorityChannels.get(0);
            Channel priorityChannel2 = priorityChannels.get(1);

            if (priorityChannel1 != null) {
                binding.priorityChannel1Layout.setVisibility(View.VISIBLE);
                binding.priorityChannel1Image.setImageResource(getImageResource(priorityChannel1));
                binding.priorityChannel1Name.setText(priorityChannel1.name);
                binding.priorityChannel1LinkImage.setOnClickListener(view -> goToChannelFragment(priorityChannel1));
            }

            if (priorityChannels.get(1) != null) {
                binding.priorityChannel2Layout.setVisibility(View.VISIBLE);
                binding.priorityChannel2Image.setImageResource(getImageResource(priorityChannel2));
                binding.priorityChannel2Name.setText(priorityChannel2.name);
                binding.priorityChannel2LinkImage.setOnClickListener(view -> goToChannelFragment(priorityChannel2));
            }
        }


        // RECYCLERVIEW

        List<Channel> radioChannels = mission.channels
                .stream()
                .filter(channel -> channel.type != null)
                .filter(channel -> channel.type.equals("radio"))
                .collect(Collectors.toList());

        RadioChannelsRecyclerViewAdapter adapter = new RadioChannelsRecyclerViewAdapter(new RadioChannelsRecyclerViewAdapter.AdapterDiffCallback(), this);
        binding.radioChannelsRecycler.setHasFixedSize(true);
        binding.radioChannelsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        binding.radioChannelsRecycler.setAdapter(adapter);
        adapter.setRadioChannels(radioChannels);
        //

        return binding.getRoot();
    }

    private void updateToolbar() {
        ((TextView) requireActivity().findViewById(R.id.toolbar_title_text)).setText(mission.name);
        requireActivity().findViewById(R.id.sos_action).setVisibility(View.VISIBLE);
        Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_round_keyboard_arrow_left_24);
    }

    public void goToChannelFragment(Channel channel) {
        NavHostFragment.findNavController(this)
                .navigate(MissionFragmentDirections.actionMissionFragmentToChannelFragment(channel));
    }

    @Override
    public void onStop() {
        super.onStop();
        requireActivity().findViewById(R.id.sos_action).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.sort_type_menu, menu);
    }

/*    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem item = menu.findItem(R.id.show_list_action);
        View root = item.getActionView();
        root.setOnClickListener(view -> onOptionsItemSelected(item));
        super.onPrepareOptionsMenu(menu);
    }*/

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.show_list_action) {
            Toast.makeText(getContext(), "List type pressed", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.show_grid_action) {
            Toast.makeText(getContext(), "Grid Type pressed", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}