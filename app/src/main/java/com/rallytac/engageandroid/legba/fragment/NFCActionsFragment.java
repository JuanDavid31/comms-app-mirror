package com.rallytac.engageandroid.legba.fragment;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.databinding.FragmentMissionsListBinding;
import com.rallytac.engageandroid.databinding.FragmentNfcActionsBinding;
import com.rallytac.engageandroid.legba.HostActivity;
import com.rallytac.engageandroid.legba.adapter.NFCActionListAdapter;
import com.rallytac.engageandroid.legba.data.DataManager;
import com.rallytac.engageandroid.legba.data.dto.Mission;
import com.rallytac.engageandroid.legba.data.dto.NFCAction;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class NFCActionsFragment extends Fragment {

    FragmentNfcActionsBinding binding;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_nfc_actions, container, false);
        setupToolbar();

        NFCActionListAdapter adapter = new NFCActionListAdapter(getNFCActions(), this, this.getContext());
        binding.recyclerViewNfcActions.setHasFixedSize(true);

        int orientation = getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT){
            binding.recyclerViewNfcActions.setLayoutManager(new LinearLayoutManager(getContext()));
        }else if(orientation == Configuration.ORIENTATION_LANDSCAPE){
            binding.recyclerViewNfcActions.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }

        binding.recyclerViewNfcActions.setAdapter(adapter);
        return binding.getRoot();
    }

    private void setupToolbar() {
        Timber.i("updateToolbar");
        requireActivity().findViewById(R.id.toolbar_title_text).setVisibility(View.VISIBLE);
        ((TextView)requireActivity().findViewById(R.id.toolbar_title_text)).setText("NFC Actions");

        Objects.requireNonNull(((HostActivity) requireActivity())
                .getSupportActionBar())
                .setHomeAsUpIndicator(R.drawable.ic_round_keyboard_arrow_left_24);
    }

    private List<NFCAction> getNFCActions() {
        List<NFCAction> nfcActions = new ArrayList<>();
        nfcActions.add(new NFCAction("btn1", "BUTTON 1", "Setup the button"));
        nfcActions.add(new NFCAction("btn2", "BUTTON 2", "Setup the button"));
        nfcActions.add(new NFCAction("btn3", "BUTTON 3", "Setup the button"));
        nfcActions.add(new NFCAction("btn4", "BUTTON 4", "Setup the button"));
        return nfcActions;
    }
}