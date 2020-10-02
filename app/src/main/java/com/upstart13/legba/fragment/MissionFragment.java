package com.upstart13.legba.fragment;

import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
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
import android.view.ViewTreeObserver;
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

import timber.log.Timber;

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

        setupViewPagerOnPageChangeListener();
        setupViewPagerDotIndicator(nonRadioChannels);
        setUpSlidingUpPanelListener();
        setUpSlidingUpChannels();

        return binding.getRoot();
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

    private void setupViewPagerOnPageChangeListener() {
        binding.missionViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
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
    }

    private void setupViewPagerDotIndicator(List<Channel> nonRadioChannels) {
        binding.tabLayout.removeAllViews();
        int dotNumber = nonRadioChannels.size() > 2 ? nonRadioChannels.size() + 1 : nonRadioChannels.size();
        dotIndicators = new ImageView[dotNumber];
        for (int i = 0; i < dotNumber; i++) {
            dotIndicators[i] = new ImageView(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(convertDpToPx(this, 5), convertDpToPx(this, 5));
            layoutParams.setMarginEnd(convertDpToPx(this, 8.3));
            dotIndicators[i].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.default_dot));
            dotIndicators[i].setLayoutParams(layoutParams);
            binding.tabLayout.addView(dotIndicators[i]);
        }
    }

    private void setUpSlidingUpPanelListener() {
        binding.toggleRadioChannelButton.setOnClickListener(view -> {
            if(binding.radioChannelsSlidingupLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED){
                binding.radioChannelsSlidingupLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }else if(binding.radioChannelsSlidingupLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
                binding.radioChannelsSlidingupLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        binding.radioChannelsSlidingupLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                ImageButton toggleRadioChannelButton = binding.toggleRadioChannelButton;
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    toggleRadioChannelButton.animate().rotation(180F).setInterpolator(new AccelerateDecelerateInterpolator());
                } else if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    toggleRadioChannelButton.animate().rotation(0F).setInterpolator(new AccelerateDecelerateInterpolator());
                }
            }
        });
    }

    private void setUpSlidingUpChannels() {
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.mission_fragment_menu, menu);
    }

    private class ChannelSlidePageAdapter extends RecyclerView.Adapter<ChannelSlidePageAdapter.GenericViewHolder> {

        private Fragment fragment;
        private List<Channel> channels;

        private int priorityIndicator = 1;
        private boolean paleRed = true;
        private boolean waterBlue = false;
        private boolean orange = false;

        private boolean primarySpeakerOn = true;
        private boolean priority1SpekearOn = true;
        private boolean priority2SpeakerOn = true;

        public void setChannels(List<Channel> channels) {
            this.channels = channels;
            notifyDataSetChanged();
        }

        public ChannelSlidePageAdapter(Fragment fragment, List<Channel> channels) {
            this.fragment = fragment;
            this.channels = channels;
        }

        @Override
        public int getItemViewType(int position) {
            if (position < 3) {
                return 0;
            } else {
                return 1;
            }
        }

        @NonNull
        @Override
        public GenericViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 0) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.channel_item, parent, false);
                return new ChannelViewHolder(itemView);
            } else {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.channels_resume_item, parent, false);
                return new ChannelResumeViewHolder(itemView);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull GenericViewHolder holder, int position) {
            if (holder instanceof ChannelViewHolder) {
                ChannelViewHolder channelHolder = (ChannelViewHolder) holder;

                Channel currentChannel = channels.get(position);
                channelHolder.channelInfo.setOnClickListener(view -> NavHostFragment.findNavController(fragment)
                        .navigate(MissionFragmentDirections.actionMissionFragmentToChannelFragment(currentChannel)));
                channelHolder.channelImage.setImageResource(getImageResource(currentChannel.image));
                channelHolder.channelName.setText(currentChannel.name);
                channelHolder.channelType.setText(getTypeString(currentChannel.type));

                if (paleRed) {
                    paleRed = false;
                    waterBlue = true;
                } else if (waterBlue) {
                    waterBlue = false;
                    orange = true;

                    channelHolder.channelImage.setBorderColor(getWaterBlueColor());
                    channelHolder.channelType.setTextColor(getWaterBlueColor());
                    channelHolder.lastMessageTime.setTextColor(getWaterBlueColor());
                } else if (orange) {
                    orange = false;
                    paleRed = true;

                    channelHolder.channelImage.setBorderColor(getOrangeColor());
                    channelHolder.channelType.setTextColor(getOrangeColor());
                    channelHolder.lastMessageTime.setTextColor(getOrangeColor());
                }
            } else if (holder instanceof ChannelResumeViewHolder) {
                ChannelResumeViewHolder channelResumeHolder = (ChannelResumeViewHolder) holder;

                channelResumeHolder.primaryChannelImage.setImageResource(getImageResource(channels.get(0).image));
                channelResumeHolder.primaryChannelName.setText(channels.get(0).name);
                channelResumeHolder.primaryChannelSpeaker.setOnClickListener(view -> {
                    primarySpeakerOn = !primarySpeakerOn;
                    toggleSpeakerIcon(primarySpeakerOn, (ImageButton) view);
                });

                channelResumeHolder.priorityChannel1Image.setImageResource(getImageResource(channels.get(1).image));
                channelResumeHolder.priorityChannel1Name.setText(channels.get(1).name);
                channelResumeHolder.priorityChannel1Speaker.setOnClickListener(view -> {
                    priority1SpekearOn = !priority1SpekearOn;
                    toggleSpeakerIcon(priority1SpekearOn, (ImageButton) view);
                });

                channelResumeHolder.priorityChannel2Image.setImageResource(getImageResource(channels.get(2).image));
                channelResumeHolder.priorityChannel2Name.setText(channels.get(2).name);
                channelResumeHolder.priorityChannel2Speaker.setOnClickListener(view -> {
                    priority2SpeakerOn = !priority2SpeakerOn;
                    toggleSpeakerIcon(priority2SpeakerOn, (ImageButton) view);
                });
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

        private void toggleSpeakerIcon(boolean isOn, ImageButton button) {
            if (isOn) {
                button.setImageResource(R.drawable.ic_speaker);
            } else {
                button.setImageResource(R.drawable.ic_speaker_off);
            }
        }

        @Override
        public int getItemCount() {
            return channels.size() > 2 ? channels.size() + 1 : channels.size();
        }

        class GenericViewHolder extends RecyclerView.ViewHolder {

            public GenericViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        class ChannelViewHolder extends GenericViewHolder {

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

        class ChannelResumeViewHolder extends GenericViewHolder {

            private RoundedImageView primaryChannelImage;
            private TextView primaryChannelName;
            private ImageButton primaryChannelSpeaker;
            private RoundedImageView priorityChannel1Image;
            private TextView priorityChannel1Name;
            private ImageButton priorityChannel1Speaker;
            private RoundedImageView priorityChannel2Image;
            private TextView priorityChannel2Name;
            private ImageButton priorityChannel2Speaker;

            public ChannelResumeViewHolder(@NonNull View itemView) {
                super(itemView);

                primaryChannelImage = itemView.findViewById(R.id.primary_channel_image);
                primaryChannelName = itemView.findViewById(R.id.primary_channel_name_text);
                primaryChannelSpeaker = itemView.findViewById(R.id.primary_channel_speaker);
                priorityChannel1Name = itemView.findViewById(R.id.priority_channel_1_name_text);
                priorityChannel1Image = itemView.findViewById(R.id.priority_channel_1_image);
                priorityChannel1Speaker = itemView.findViewById(R.id.priority_channel_1_speaker);
                priorityChannel2Name = itemView.findViewById(R.id.priority_channel_2_name_text);
                priorityChannel2Image = itemView.findViewById(R.id.priority_channel_2_image);
                priorityChannel2Speaker = itemView.findViewById(R.id.priority_channel_2_speaker);
            }
        }
    }
}