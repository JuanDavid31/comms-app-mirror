package com.rallytac.engageandroid.legba.fragment;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.rallytac.engageandroid.EngageApplication;
import com.rallytac.engageandroid.Globals;
import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.databinding.FragmentChannelHistoryBinding;
import com.rallytac.engageandroid.legba.HostActivity;
import com.rallytac.engageandroid.legba.data.DataManager;
import com.rallytac.engageandroid.legba.data.dto.Audio;
import com.rallytac.engageandroid.legba.util.RUtils;
import com.rallytac.engageandroid.legba.viewmodel.ChannelHistoryViewModel;
import com.rallytac.engageandroid.legba.viewmodel.ViewModelFactory;

import java.util.List;
import java.util.Objects;

public class ChannelHistoryFragment extends Fragment implements EngageApplication.IGroupTimelineListenerLegba {

    private FragmentChannelHistoryBinding binding;
    private AudioHistoryRecyclerViewAdapter audioHistoryRecyclerViewAdapter;
    private ChannelHistoryViewModel vm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Globals.onGroupTimelineReportListener = this;
        ViewModelFactory vmFactory = new ViewModelFactory((EngageApplication) getActivity().getApplication());
        vm = new ViewModelProvider(this, vmFactory).get(ChannelHistoryViewModel.class);
        ChannelHistoryFragmentArgs channelHistoryFragmentArgs = ChannelHistoryFragmentArgs.fromBundle(requireArguments());
        String channelId = channelHistoryFragmentArgs.getChannelId();
        vm.setupChannel(channelId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setupToolbar();
        setHasOptionsMenu(true);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_channel_history, container, false);

        DataManager.getInstance().askForAudiosHistory(vm.getChannelId());
        binding.historyRecycler.setHasFixedSize(true);

        return binding.getRoot();
    }

    private void setupToolbar() {
        requireActivity().findViewById(R.id.toolbar_left_title_text).setVisibility(View.VISIBLE);
        ((TextView) requireActivity().findViewById(R.id.toolbar_left_title_text)).setText(vm.getChannelName());
        requireActivity().findViewById(R.id.toolbar_background_image).setVisibility(View.VISIBLE);
        ((ImageView) requireActivity().findViewById(R.id.toolbar_background_image)).setImageResource(RUtils.getImageResource(vm.getChannelImage()));
        requireActivity().findViewById(R.id.logo_image).setVisibility(View.VISIBLE);
        Objects.requireNonNull(((HostActivity) requireActivity()).getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_round_keyboard_arrow_left_24); //Default dp is unknown
    }

    @Override
    public void onGroupTimelineEventStarted(String groupId, String eventJson) {
        List<Audio> audios = vm.mapTimelineAudiosToAudio(eventJson);
        audioHistoryRecyclerViewAdapter = new AudioHistoryRecyclerViewAdapter(new AudioHistoryRecyclerViewAdapter.AdapterDiffCallback(), audios, getContext());
        binding.historyRecycler.setAdapter(audioHistoryRecyclerViewAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Globals.onGroupTimelineReportListener = null;
        List<Audio> audios = audioHistoryRecyclerViewAdapter.getAudios();
        audios.stream()
                .map(audio -> audio.mediaPlayer)
                .filter(Objects::nonNull)
                .forEach(MediaPlayer::release);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.channel_history_fragment_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem item = menu.findItem(R.id.settings_action);
        item.getIcon().mutate().setAlpha(100);
    }

    /*@Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem item = menu.findItem(R.id.menu_my_item);

        if (myItemShouldBeEnabled) {
            item.setEnabled(true);
            item.getIcon().setAlpha(255);
        }
    }*/
}