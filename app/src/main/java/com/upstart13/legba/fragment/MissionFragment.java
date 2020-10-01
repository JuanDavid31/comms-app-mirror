package com.upstart13.legba.fragment;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import static com.upstart13.legba.util.DimUtils.convertDpToPx;
import static com.upstart13.legba.util.RUtils.getImageResource;


public class MissionFragment extends Fragment {

    private FragmentMissionBinding binding;
    private Mission mission;
    private TextView fragmentDescriptionText;
    private ImageView[] dotIndicators;

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
        //binding.springDotsIndicator.setViewPager2(binding.missionViewPager);

        binding.missionViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);

                updateDots(position);

                String ordinalPosition;
                switch (position) {
                    case 0:
                        ordinalPosition = "First";
                        break;
                    case 1:
                        ordinalPosition = "Second";
                        break;
                    case 2:
                        ordinalPosition = "Third";
                        break;
                    case 3:
                        ordinalPosition = "Fourth";
                        break;
                    case 4:
                        ordinalPosition = "Fifth";
                        break;
                    default:
                        ordinalPosition = "";
                        break;
                }

                SpannableStringBuilder ssb = new SpannableStringBuilder(String.format("%s View", ordinalPosition));
                final ForegroundColorSpan fcs = new ForegroundColorSpan(getResources().getColor(R.color.pinkish_grey));

                ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, ordinalPosition.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                ssb.setSpan(fcs, ordinalPosition.length() + 1, ssb.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                fragmentDescriptionText.setText(ssb);
            }
        });

        binding.tabLayout.removeAllViews();
        dotIndicators = new ImageView[nonRadioChannels.size()];
        for (int i = 0; i < nonRadioChannels.size(); i++) {
            dotIndicators[i] = new ImageView(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(convertDpToPx(this, 5), convertDpToPx(this, 5));
            layoutParams.setMarginEnd(convertDpToPx(this, 8.3));
            dotIndicators[i].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.default_dot));
            dotIndicators[i].setLayoutParams(layoutParams);
            binding.tabLayout.addView(dotIndicators[i]);
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

    private void updateDots(int position) {
        for (int i = 0; i < dotIndicators.length; i++) {
            if (i == position) {
                dotIndicators[i].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.selected_dot));
            } else {
                dotIndicators[i].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.default_dot));
            }
        }
    }

    private void updateToolbar() {
        requireActivity().findViewById(R.id.logo_image).setVisibility(View.VISIBLE);
        requireActivity().findViewById(R.id.toolbar_title_text).setVisibility(View.VISIBLE);
        ((TextView) requireActivity().findViewById(R.id.toolbar_title_text)).setText(mission.name);
        requireActivity().findViewById(R.id.fragment_description).setVisibility(View.VISIBLE);
        fragmentDescriptionText = requireActivity().findViewById(R.id.fragment_description);
        fragmentDescriptionText.setTextColor(this.getResources().getColor(R.color.paleRed));
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

        public void setChannels(List<Channel> channels) {
            this.channels = channels;
            notifyDataSetChanged();
        }

        public ChannelSlidePageAdapter(Fragment fragment, List<Channel> channels) {
            this.fragment = fragment;
            this.channels = channels;
        }

        @NonNull
        @Override
        public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.channel_item, parent, false);
            return new ChannelViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position) {
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
                holder.lastMessageTime.setTextColor(getWaterBlueColor());
            } else if (orange) {
                orange = false;
                paleRed = true;

                holder.channelImage.setBorderColor(getOrangeColor());
                holder.channelType.setTextColor(getOrangeColor());
                holder.lastMessageTime.setTextColor(getOrangeColor());
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
                    String priority = "Priority Channel " + (priorityIndicator + 1);
                    priorityIndicator++;
                    return priority;
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
            private TextView lastMessageTime;

            public ChannelViewHolder(@NonNull View itemView) {
                super(itemView);
                channelInfo = itemView.findViewById(R.id.channel_info);
                channelImage = itemView.findViewById(R.id.channel_image);
                channelName = itemView.findViewById(R.id.channel_name_text);
                channelType = itemView.findViewById(R.id.channel_type_text);
                lastMessageTime = itemView.findViewById(R.id.last_message_time);
            }
        }
    }
}