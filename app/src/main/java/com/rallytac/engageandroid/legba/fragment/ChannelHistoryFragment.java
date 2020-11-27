package com.rallytac.engageandroid.legba.fragment;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.rallytac.engageandroid.EngageApplication;
import com.rallytac.engageandroid.Globals;
import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.databinding.FragmentChannelHistoryBinding;
import com.rallytac.engageandroid.legba.data.DataManager;
import com.rallytac.engageandroid.legba.data.dto.Audio;
import com.rallytac.engageandroid.legba.viewmodel.ChannelHistoryViewModel;

import java.util.List;
import java.util.Objects;

public class ChannelHistoryFragment extends Fragment implements EngageApplication.IGroupTimelineListenerLegba {

    private FragmentChannelHistoryBinding binding;
    private AudioHistoryRecyclerViewAdapter audioHistoryRecyclerViewAdapter;
    //private
    private ChannelHistoryViewModel vm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Globals.onGroupTimelineReportListener = this;
        vm = new ViewModelProvider(this).get(ChannelHistoryViewModel.class);
        ChannelHistoryFragmentArgs channelHistoryFragmentArgs = ChannelHistoryFragmentArgs.fromBundle(requireArguments());
        String channelId = channelHistoryFragmentArgs.getChannelId();
        vm.setChannelId(channelId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_channel_history, container, false);

        DataManager.getInstance().askForAudiosHistory(vm.getChannelId());
        binding.historyRecycler.setHasFixedSize(true);

        return binding.getRoot();
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
}