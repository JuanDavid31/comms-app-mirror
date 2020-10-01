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
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.upstart13.legba.HostActivity;
import com.upstart13.legba.R;
import com.upstart13.legba.data.dto.Channel;
import com.upstart13.legba.data.dto.Mission;
import com.upstart13.legba.databinding.FragmentMissionBinding;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import timber.log.Timber;

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
        List<Channel> nonRadioChannels = mission.channels
                .stream()
                .filter(channel -> channel.type != Channel.ChannelType.RADIO)
                .collect(Collectors.toList());

        binding.missionViewPager.setAdapter(new ChannelSlidePageAdapter(this, nonRadioChannels));
        binding.springDotsIndicator.setViewPager2(binding.missionViewPager);

        //Primary channel

/*        Channel primaryChannel = mission.channels
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
        }*/

        //Sliding up layout
        binding.radioChannelsSlidingupLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                ImageButton toggleRadioChannelButton = binding.toggleRadioChannelButton;
                float newRotation = toggleRadioChannelButton.getRotation() + 180F;
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    toggleRadioChannelButton.animate().rotation(newRotation).setInterpolator(new AccelerateDecelerateInterpolator());
                } else if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
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

    public void goToChannelFragment(Channel channel) {
        NavHostFragment.findNavController(this)
                .navigate(MissionFragmentDirections.actionMissionFragmentToChannelFragment(channel));
    }

    private void updateToolbar() {
        Timber.i("updateToolbar MissionFragment");
        requireActivity().findViewById(R.id.logo_image).setVisibility(View.VISIBLE);
        requireActivity().findViewById(R.id.toolbar_title_text).setVisibility(View.VISIBLE);
        ((TextView) requireActivity().findViewById(R.id.toolbar_title_text)).setText(mission.name);
        requireActivity().findViewById(R.id.fragment_description).setVisibility(View.VISIBLE);
        ((TextView) requireActivity().findViewById(R.id.fragment_description)).setText("Main Channels");
        ((TextView) requireActivity().findViewById(R.id.fragment_description)).setTextColor(this.getResources().getColor(R.color.paleRed));
        Objects.requireNonNull(((HostActivity) requireActivity()).getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_round_keyboard_arrow_left_24);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.mission_fragment_menu, menu);
    }

    private class ChannelSlidePageAdapter extends RecyclerView.Adapter<ChannelSlidePageAdapter.ChannelViewHolder> {

        private Fragment fragment;
        private List<Channel> channels;

        private int priorityIndicator = 1;
        private boolean paleRed = true;
        private boolean waterBlue = false;
        private boolean orange = false;

        public void setChannels(List channels) {
            this.channels = channels;
            notifyDataSetChanged();
        }

        public ChannelSlidePageAdapter(Fragment fragment, List channels) {
            this.fragment = fragment;
            this.channels = channels;
        }

        @NonNull
        @Override
        public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Timber.i("Creando page");
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.channel_item, parent, false);
            return new ChannelViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position) {
            Timber.i("onBindViewHolder - ChannelSlidePageAdapter");
            Channel currentChannel = channels.get(position);
            holder.channelInfo.setOnClickListener(view -> NavHostFragment.findNavController(fragment)
                    .navigate(MissionFragmentDirections.actionMissionFragmentToChannelFragment(currentChannel)));
            holder.channelImage.setImageResource(getImageResource(currentChannel.image));
            holder.channelName.setText(currentChannel.name);
            holder.channelType.setText(getTypeString(currentChannel.type));

            if (paleRed) {
                paleRed = false;
                waterBlue = true;
            } else if (waterBlue) {
                waterBlue = false;
                orange = true;

                holder.channelImage.setBorderColor(getWaterBlueColor());
                holder.channelType.setTextColor(getWaterBlueColor());
            } else if (orange) {
                orange = false;
                paleRed = true;

                holder.channelImage.setBorderColor(getOrangeColor());
                holder.channelType.setTextColor(getOrangeColor());
            }
        }

        private int getWaterBlueColor() {
            return fragment.getResources().getColor(R.color.waterBlue);
        }

        private int getOrangeColor() {
            return fragment.getResources().getColor(R.color.orange);
        }

        private String getTypeString(Channel.ChannelType type) {
            switch (type) {
                case PRIMARY:
                    return "Primary Channel";
                case PRIORITY:
                    return "Priority Channel " + priorityIndicator;
                case RADIO:
                    return "Radio Channel";
                default:
                    return "";
            }
        }

        @Override
        public int getItemCount() {
            return channels.size();
        }

        class ChannelViewHolder extends RecyclerView.ViewHolder {

            private View channelInfo;
            private RoundedImageView channelImage;
            private TextView channelName;
            private TextView channelType;

            public ChannelViewHolder(@NonNull View itemView) {
                super(itemView);
                channelInfo = itemView.findViewById(R.id.channel_info);
                channelImage = itemView.findViewById(R.id.channel_image);
                channelName = itemView.findViewById(R.id.channel_name_text);
                channelType = itemView.findViewById(R.id.channel_type_text);
            }
        }
    }
}