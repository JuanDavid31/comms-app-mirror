package com.rallytac.engageandroid.legba.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.rallytac.engageandroid.Globals;
import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.legba.adapter.ChannelListAdapter;
import com.rallytac.engageandroid.legba.adapter.ChannelSlidePageAdapter;
import com.rallytac.engageandroid.legba.data.DataManager;
import com.rallytac.engageandroid.legba.data.dto.ChannelGroup;
import com.rallytac.engageandroid.legba.engage.RxListener;
import com.rallytac.engageandroid.legba.util.StringUtils;
import com.rallytac.engageandroid.legba.view.SwipeButton;
import com.rallytac.engageandroid.legba.viewmodel.MissionViewModel;
import com.rallytac.engageandroid.legba.viewmodel.ViewModelFactory;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.rallytac.engageandroid.legba.HostActivity;
import com.rallytac.engageandroid.legba.data.dto.Channel;
import com.rallytac.engageandroid.legba.data.dto.Mission;
import com.rallytac.engageandroid.databinding.FragmentMissionBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import timber.log.Timber;

import static com.rallytac.engageandroid.legba.util.DimUtils.convertDpToPx;

public class MissionFragment extends Fragment implements RxListener {

    private HostActivity activity;
    public FragmentMissionBinding binding;
    private ChannelSlidePageAdapter channelSlidePageAdapter;
    private ChannelListAdapter channelListAdapter;
    private ImageView[] dotIndicators;
    private MenuItem sosAction;
    private TransitionDrawable transition;
    private Context context;
    private MissionViewModel vm;
    private int currentPage;
    private boolean lastPage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewModelFactory vmFactory = new ViewModelFactory(getActivity());
        vm = new ViewModelProvider(this, vmFactory).get(MissionViewModel.class);
        context = getContext();
        Globals.rxListeners.add(this);

        setupMission();
    }

    private void setupMission() {
        MissionFragmentArgs missionFragmentArgs = MissionFragmentArgs.fromBundle(requireArguments());
        Mission mission = missionFragmentArgs.getMission(); //TODO: the id should be the only one to be passed as an argument instead of a Mission instance
        vm.setupMission(mission);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (HostActivity) requireActivity();

        transition = (TransitionDrawable) activity.binding.sosOverlapLayout.getBackground();

        updateToolbar();
        setHasOptionsMenu(true);
        setupEmergencyListeners();
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mission, container, false);
        binding.toggleRadioChannelButton.setRotation(vm.getToggleRadioChannelButtonRotation());

        channelSlidePageAdapter = new ChannelSlidePageAdapter(this, getChannelsGroup());
        binding.missionViewPager.setAdapter(channelSlidePageAdapter);

        setupPTTOnMic();
        setupViewPagerOnPageChangeListener();
        setupViewPagerDotIndicator(getChannelsGroup());
        setUpSlidingUpPanelListener();
        setUpSlidingUpChannels();
        updateDots(0);
        setupEditCurrentChannelGroupLayout();

        return binding.getRoot();
    }

    /*public Mission getMission() {
        return mission;
    }*/

    public List<ChannelGroup> getChannelsGroup() {
        return vm.getChannelsGroup() == null ? new ArrayList<>() : vm.getChannelsGroup();
    }

    private void updateToolbar() {
        activity.binding.logoImage.setVisibility(View.VISIBLE);
        activity.binding.toolbarTitleText.setVisibility(View.VISIBLE);
        activity.binding.toolbarTitleText.setText(vm.getMission().getName());
        activity.binding.fragmentDescription.setVisibility(View.VISIBLE);
        activity.binding.editCurrentChannelGroupButton.setVisibility(View.VISIBLE);
        activity.binding.fragmentDescription.setTextColor(this.getResources().getColor(R.color.paleRed));
        Objects.requireNonNull(((HostActivity) requireActivity()).getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_round_keyboard_arrow_left_24);
    }

    private void setupEmergencyListeners() {
        activity.binding.sosSwipeButton.setSosEmergencyListener(new SwipeButton.SOSEmergencyListener() {

            boolean isGradientActive = false;

            @Override
            public void onSwipeStart() {
                Timber.i("onSwipeStart");
                sosAction.getActionView()
                        .animate()
                        .alpha(0f)
                        .setDuration(1);

                if (!isGradientActive) {
                    transition.startTransition(300);
                    isGradientActive = true;
                }
            }

            @Override
            public void onSwipeStartEnd() {
                if (isGradientActive) {
                    transition.reverseTransition(300);
                    isGradientActive = false;
                }
            }

            @Override
            public void onSosStart() {
                activity.binding.eyesGlowAnimation.setVisibility(View.VISIBLE);
                activity.binding.sosButtonGlowAnimation.setVisibility(View.VISIBLE);
                activity.binding.sosTxImage.animate()
                        .alpha(1f)
                        .setDuration(900);
                Globals.getEngageApplication().startTx(0, 1);
            }

            @Override
            public void onSosStop() {
                activity.binding.eyesGlowAnimation.setVisibility(View.GONE);
                activity.binding.sosButtonGlowAnimation.setVisibility(View.GONE);
                activity.binding.sosTxImage.animate()
                        .alpha(0.0f)
                        .setDuration(300);
                Globals.getEngageApplication().endTx();
            }

            @Override
            public void onFreeButton(double ms) {
                if (isGradientActive) {
                    transition.reverseTransition((int) ms);
                    isGradientActive = false;
                }
            }

            @Override
            public void onSwipeFinish() {
                toggleLayoutVisiblity(activity.binding.sosOverlapLayout);
                sosAction.getActionView()
                        .animate()
                        .alpha(1f)
                        .setDuration(300);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPTTOnMic() {
        binding.icMicCard.setOnTouchListener((view, event) -> {

            String[] activeGroupIds = vm.getChannelsGroup()
                    .get(currentPage)
                    .getChannels()
                    .stream()
                    .map(Channel::getId)
                    .toArray(String[]::new);

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                binding.txImage.setVisibility(View.VISIBLE);
                Log.w("sending", "#SB#: onTouch ACTION_DOWN - startTx");//NON-NLS
                Timber.i("Tx to %s", activeGroupIds);
                DataManager.getInstance(context).startTx(activeGroupIds);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                binding.txImage.setVisibility(View.INVISIBLE);
                Log.w("Stop sending", "#SB#: onTouch ACTION_UP - endTx");//NON-NLS
                DataManager.getInstance(context).endTx(activeGroupIds);
            }
            return true;
        });
    }

    private void setupViewPagerOnPageChangeListener() {
        binding.missionViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                currentPage = position;
                int channelsGroupsSize = vm.getChannelsGroup().size();
                lastPage = currentPage == channelsGroupsSize;
                updateDots(position);

                if (position < channelsGroupsSize) {
                    activity.binding.editCurrentChannelGroupButton.setVisibility(View.VISIBLE);
                    String name = StringUtils.capitalize(vm.getChannelsGroup().get(position).getName());
                    activity.binding.fragmentDescription.setText(name);
                } else {
                    activity.binding.fragmentDescription.setText("");
                    activity.binding.editCurrentChannelGroupButton.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupViewPagerDotIndicator(List channelGroup) {
        binding.tabLayout.removeAllViews();
        int dotNumber = channelGroup.size() + 1;
        dotIndicators = new ImageView[dotNumber];
        for (int i = 0; i < dotNumber; i++) {
            dotIndicators[i] = new ImageView(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(convertDpToPx(this, 4.5), convertDpToPx(this, 4.5));
            layoutParams.setMarginEnd(convertDpToPx(this, 6.3));
            dotIndicators[i].setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.default_dot));
            dotIndicators[i].setLayoutParams(layoutParams);
            binding.tabLayout.addView(dotIndicators[i]);
        }
    }

    private void setUpSlidingUpPanelListener() {
        binding.toggleRadioChannelButton.setOnClickListener(view -> {
            if (binding.radioChannelsSlidingupLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                binding.radioChannelsSlidingupLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            } else if (binding.radioChannelsSlidingupLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
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
                    vm.setToggleRadioChannelButtonRotation(180F);
                } else if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    toggleRadioChannelButton.animate().rotation(0F).setInterpolator(new AccelerateDecelerateInterpolator());
                    vm.setToggleRadioChannelButtonRotation(0F);
                }
            }
        });
    }

    private void setUpSlidingUpChannels() {
        RadioChannelsRecyclerViewAdapter adapter = new RadioChannelsRecyclerViewAdapter(new RadioChannelsRecyclerViewAdapter.AdapterDiffCallback(), this);
        binding.radioChannelsRecycler.setHasFixedSize(true);
        binding.radioChannelsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        binding.radioChannelsRecycler.setAdapter(adapter);
        adapter.setRadioChannels(vm.getAllChannels());
    }

    private void updateDots(int position) {
        for (int i = 0; i < dotIndicators.length; i++) {
            double dp;
            int drawableId;

            if (i == position) {
                dp = 5.5;
                drawableId = R.drawable.selected_dot;
            } else {
                dp = 4.5;
                drawableId = R.drawable.default_dot;
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(convertDpToPx(this, dp), convertDpToPx(this, dp));
            layoutParams.setMarginEnd(convertDpToPx(this, 6.3));
            dotIndicators[i].setImageDrawable(ContextCompat.getDrawable(getContext(), drawableId));
            dotIndicators[i].setLayoutParams(layoutParams);
        }
    }

    private void setupEditCurrentChannelGroupLayout() {
        channelListAdapter = new ChannelListAdapter(vm.getAllChannels(), this);
        activity.binding.channelsRecycler.setHasFixedSize(true);
        activity.binding.channelsRecycler.setAdapter(channelListAdapter);

        activity.binding.editCurrentChannelGroupButton.setOnClickListener(view -> toggleCreateEditChannelsGroupLayoutvisibility());
        activity.binding.closeCreateEditChannelsViewLayout.setOnClickListener(view -> toggleCreateEditChannelsGroupLayoutvisibility());
        activity.binding.closeCreateEditChannelsViewButton.setOnClickListener(view -> toggleCreateEditChannelsGroupLayoutvisibility()); //TODO: Replace for a proper fix
        activity.binding.createEditChannelsGroupButton.setOnClickListener(view -> updateCurrentChannelsGroup());

        if (lastPage) {
            activity.binding.createEditChannelsGroupButton.setClickable(false);
            activity.binding.createEditChannelsGroupButton.setBackground(ContextCompat.getDrawable(context, R.drawable.edit_channel_group_btn_fade_shape));
        }

        activity.binding.channelGroupNameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String channelGroupNameSearch = editable.toString().toLowerCase().trim();
                String currentChannelGroupName = activity.binding.fragmentDescription.getText().toString().toLowerCase().trim();
                Long coincidences = vm.getChannelsGroup().stream()
                        .filter(channelGroup -> channelGroup.getName().equalsIgnoreCase(channelGroupNameSearch))
                        .count();

                if ((coincidences < 1L || channelGroupNameSearch.equals(currentChannelGroupName)) && channelGroupNameSearch.length() > 0) {
                    activity.binding.createEditChannelsGroupButton.setClickable(true);
                    activity.binding.createEditChannelsGroupButton.setBackground(ContextCompat.getDrawable(context, R.drawable.edit_channel_group_btn_shape));
                } else {
                    activity.binding.createEditChannelsGroupButton.setClickable(false);
                    activity.binding.createEditChannelsGroupButton.setBackground(ContextCompat.getDrawable(context, R.drawable.edit_channel_group_btn_fade_shape));
                }
            }
        });
    }

    private void updateCurrentChannelsGroup() {
        List<Channel> channels = channelListAdapter.getCheckedChannels();
        if (channels.size() > 0) {
            updateNameAndActiveChannelsOnCurrentChannelsGroup(channels);
        } else if (!lastPage) {
            vm.deleteChannelGroup(currentPage);
            boolean channelGroupSize = currentPage >= vm.getChannelsGroup().size();

            if (channelGroupSize) {
                lastPage = true;
                activity.binding.fragmentDescription.setText("");
                activity.binding.editCurrentChannelGroupButton.setVisibility(View.GONE);
            } else {
                activity.binding.fragmentDescription.setText(vm.getChannelsGroup().get(currentPage).getName());
            }
        }

        toggleCreateEditChannelsGroupLayoutvisibility();
        channelSlidePageAdapter.setChannelsGroup(vm.getChannelsGroup());
        setupViewPagerDotIndicator(vm.getChannelsGroup());
        updateDots(currentPage);
    }

    private void updateNameAndActiveChannelsOnCurrentChannelsGroup(List<Channel> channels) {
        String newName = activity.binding.channelGroupNameText.getText().toString();

        if (lastPage) {
            ChannelGroup newChannelGroup = new ChannelGroup(newName, vm.getMission().getId(), channels);
            vm.addChannelGroup(newChannelGroup);
        } else {
            ChannelGroup currentChannelGroup = vm.getChannelsGroup().get(currentPage);
            currentChannelGroup.setName(newName);
            currentChannelGroup.setChannels(channels);
        }

        activity.binding.fragmentDescription.setText(newName);
        activity.binding.editCurrentChannelGroupButton.setVisibility(View.VISIBLE);
    }

    public void toggleCreateEditChannelsGroupLayoutvisibility() {
        activity.binding.channelGroupNameText.setText(activity.binding.fragmentDescription.getText().toString());
        toggleLayoutVisiblity(binding.icMicCard);
        toggleLayoutVisiblity(binding.radioChannelsSlidingupLayout);
        toggleLayoutVisiblity(activity.binding.channelGroupLayout);

        List<Channel> allChannels = vm
                .getAllChannels()
                .stream()
                .peek(channel -> channel.setActive(false))
                .collect(Collectors.toList());

        if (!lastPage) {
            List<Channel> activeChannels = vm.getChannelsGroup().get(currentPage).getChannels();
            allChannels = allChannels
                    .stream()
                    .map(channel -> {
                        activeChannels
                                .stream()
                                .filter(activeChannel -> activeChannel.getId().equals(channel.getId()))
                                .findFirst()
                                .ifPresent(activeChannel -> {
                                    channel.setActive(true);
                                });
                        return channel;
                    })
                    .collect(Collectors.toList());
        }

        channelListAdapter.setChannels(allChannels);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.mission_fragment_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        sosAction = menu.findItem(R.id.sos_action);
        View root = sosAction.getActionView();

        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        toggleLayoutVisiblity(activity.binding.sosOverlapLayout);
                        return true;
                    case MotionEvent.ACTION_UP:
                        activity.binding.sosSwipeButton.dispatchTouchEvent(MotionEvent.obtain(event.getDownTime(),
                                event.getEventTime(),
                                MotionEvent.ACTION_UP,
                                event.getX(),
                                event.getY(),
                                event.getMetaState()));

                        return true;
                    case MotionEvent.ACTION_MOVE:
                        activity.binding.sosSwipeButton.dispatchTouchEvent(MotionEvent.obtain(event.getDownTime(),
                                event.getEventTime(),
                                MotionEvent.ACTION_MOVE,
                                event.getX(),
                                event.getY(),
                                event.getMetaState()));

                        return false;
                    default:
                        return false;
                }
            }
        });

    }

    private void toggleLayoutVisiblity(View layout) {
        if (layout.getVisibility() == View.GONE) {
            layout.animate()
                    .alpha(0.95f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationEnd(animation);
                            layout.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            layout.animate()
                    .alpha(0.0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            layout.setVisibility(View.GONE);
                        }
                    });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Globals.rxListeners.remove(this);
    }

    @Override
    public void onRx(String id, String alias, String displayName) {
        boolean isIdPresent = vm.getChannelsGroup()
                .get(currentPage)
                .getChannels()
                .stream()
                .anyMatch(channel -> channel.getId().equals(id));

        if (!isIdPresent) return;
        ;

        int channelsSize = getChannelsGroup().get(currentPage).getChannels().size();

        if (channelsSize == 0) {
            //TODO: Standalone animation
            int currentItem = this.binding.missionViewPager.getCurrentItem();
            View viewById = this.binding.missionViewPager.findViewById(currentItem);
            Timber.i("viewById %s", viewById.getClass());
        } else {
            //TODO: Compound animation
            //this.channelSlidePageAdapter.initCompoundAnimation();
        }
    }

    @Override
    public void stopRx(String id, String eventExtraJson) {
        int channelsSize = getChannelsGroup().get(currentPage).getChannels().size();

        if (channelsSize == 0) {
            //TODO: Standalone animation
            int currentItem = this.binding.missionViewPager.getCurrentItem();
            View viewById = this.binding.missionViewPager.findViewById(currentItem);
            Timber.i("viewById %s", viewById.getClass());

        } else {
            //TODO: Compound animation
        }
    }
}