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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.upstart13.legba.HostActivity;
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
    private Menu sortTypeMenu;
    private Mission mission;

    private enum SortType {List, Grid}
    private SortType currentSortType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MissionFragmentArgs missionFragmentArgs = MissionFragmentArgs.fromBundle(requireArguments());
        mission = missionFragmentArgs.getMission();
        currentSortType = SortType.Grid; //Load and save in MissionFragmentViewModel
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        updateToolbar();
        setHasOptionsMenu(true);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mission, container, false);

        //Primary channel

        Channel primaryChannel = mission.channels
                .stream()
                .filter(channel -> channel.type != null)
                .filter(channel -> channel.type == Channel.ChannelType.PRIMARY)
                .findFirst()
                .orElse(null);

        if (primaryChannel != null) {
            binding.primaryChannelLayout.setVisibility(View.VISIBLE);
            binding.primaryChannelImage.setImageResource(getImageResource(primaryChannel.image));
            binding.primaryChannelNameView.setText(primaryChannel.name);
            binding.primaryChannelInfo.setOnClickListener(view -> goToChannelFragment(primaryChannel));
        }

        //Priority channels

        List<Channel> priorityChannels = mission.channels
                .stream()
                .filter(channel -> channel.type != null)
                .filter(channel -> channel.type == Channel.ChannelType.PRIORITY)
                .collect(Collectors.toList());

        if (priorityChannels.size() > 0) {

            Channel priorityChannel1 = priorityChannels.get(0);
            Channel priorityChannel2 = priorityChannels.get(1);

            if (priorityChannel1 != null) {
                binding.priorityChannel1Layout.setVisibility(View.VISIBLE);
                binding.priorityChannel1Image.setImageResource(getImageResource(priorityChannel1.image));
                binding.priorityChannel1Name.setText(priorityChannel1.name);
                binding.priorityChannel1LinkImage.setOnClickListener(view -> goToChannelFragment(priorityChannel1));
            }

            if (priorityChannels.get(1) != null) {
                binding.priorityChannel2Layout.setVisibility(View.VISIBLE);
                binding.priorityChannel2Image.setImageResource(getImageResource(priorityChannel2.image));
                binding.priorityChannel2Name.setText(priorityChannel2.name);
                binding.priorityChannel2LinkImage.setOnClickListener(view -> goToChannelFragment(priorityChannel2));
            }
        }

        //Sliding up layout
        binding.radioChannelsSlidingupLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                ImageButton toggleRadioChannelButton = binding.toggleRadioChannelButton;
                float newRotation = toggleRadioChannelButton.getRotation() + 180F;
                if(newState == SlidingUpPanelLayout.PanelState.EXPANDED){
                    toggleRadioChannelButton.animate().rotation(newRotation).setInterpolator(new AccelerateDecelerateInterpolator());
                }else if(newState == SlidingUpPanelLayout.PanelState.COLLAPSED){
                    toggleRadioChannelButton.animate().rotation(newRotation).setInterpolator(new AccelerateDecelerateInterpolator());
                }
            }
        });

        //

        // RECYCLERVIEW

        List<Channel> radioChannels = mission.channels
                .stream()
                .filter(channel -> channel.type != null)
                .filter(channel -> channel.type == Channel.ChannelType.RADIO)
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
        requireActivity().findViewById(R.id.logo_image).setVisibility(View.VISIBLE);
        requireActivity().findViewById(R.id.sos_action).setVisibility(View.VISIBLE);
        Objects.requireNonNull(((HostActivity) requireActivity()).getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_round_keyboard_arrow_left_24);
    }

    public void goToChannelFragment(Channel channel) {
        NavHostFragment.findNavController(this)
                .navigate(MissionFragmentDirections.actionMissionFragmentToChannelFragment(channel));
    }

    @Override
    public void onStop() {
        super.onStop();
        requireActivity().findViewById(R.id.logo_image).setVisibility(View.GONE);
        requireActivity().findViewById(R.id.sos_action).setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.sort_type_menu, menu);
        sortTypeMenu = menu;
        updateSortIcon();
    }

    private void updateSortIcon(){
        if(currentSortType == SortType.List){
            sortTypeMenu.findItem(R.id.toggle_sort_type_action).setIcon(R.drawable.ic_view_agenda);
        }else {
            sortTypeMenu.findItem(R.id.toggle_sort_type_action).setIcon(R.drawable.ic_view_grid);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.toggle_sort_type_action && currentSortType == SortType.List) {
            currentSortType = SortType.Grid;
            updateSortIcon();
            return true;
        } else if (item.getItemId() == R.id.toggle_sort_type_action && currentSortType == SortType.Grid) {
            currentSortType = SortType.List;
            updateSortIcon();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}