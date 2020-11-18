package com.rallytac.engageandroid.legba.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rallytac.engage.engine.Engine;
import com.rallytac.engageandroid.DatabaseGroup;
import com.rallytac.engageandroid.DatabaseMission;
import com.rallytac.engageandroid.EngageApplication;
import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.ShareHelper;
import com.rallytac.engageandroid.ShareableData;
import com.rallytac.engageandroid.Utils;
import com.rallytac.engageandroid.databinding.FragmentMissionsListBinding;
import com.rallytac.engageandroid.legba.HostActivity;
import com.rallytac.engageandroid.legba.data.dto.Mission;

import com.rallytac.engageandroid.legba.util.MappingUtils;
import com.rallytac.engageandroid.legba.viewmodel.MissionsListViewModel;
import com.rallytac.engageandroid.legba.viewmodel.ViewModelFactory;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class MissionsListFragment extends Fragment {

    private FragmentMissionsListBinding binding;
    private MissionsListViewModel vm;
    private final int PICK_MISSION_FILE_REQUEST_CODE = 44;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewModelFactory vmFactory = new ViewModelFactory((EngageApplication) getActivity().getApplication());
        vm = new ViewModelProvider(this, vmFactory).get(MissionsListViewModel.class);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_missions_list, container, false);
        setupToolbar();

        binding.missionsListRecyclerView.setHasFixedSize(true);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.missionsListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.missionsListRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }

        return binding.getRoot();
    }

    private void setupToolbar() {
        setHasOptionsMenu(true);
        requireActivity().findViewById(R.id.toolbar_title_text).setVisibility(View.VISIBLE);

        ((TextView) requireActivity().findViewById(R.id.toolbar_title_text)).setText(getString(R.string.nav_drawer_my_missions));
        HostActivity hostActivity = (HostActivity) requireActivity();
        ActionBar actionBar = hostActivity.getSupportActionBar();

        Objects.requireNonNull(actionBar).setHomeAsUpIndicator(R.drawable.ic_hamburguer_icon);
    }

    @Override
    public void onStart() {
        super.onStart();
        List<Mission> missions = vm.getMissions();
        MissionsRecyclerViewAdapter adapter = new MissionsRecyclerViewAdapter(new MissionsRecyclerViewAdapter.AdapterDiffCallback(), this, vm);
        binding.missionsListRecyclerView.setAdapter(adapter);
        adapter.setMissions(missions);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.missions_list_fragment_menu, menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.create_manually_action:
                NavHostFragment.findNavController(this)
                        .navigate(MissionsListFragmentDirections.actionMissionsFragmentToMissionEditActivity(null));
                return true;
            case R.id.load_from_json_action:
                startLoadMissionFromLocalFile();
                return true;
            case R.id.export_to_json_action:
                showExportToJsonDialogDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startLoadMissionFromLocalFile() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_a_file)), PICK_MISSION_FILE_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Timber.i("OnActivityResult");
        if (data != null && requestCode == PICK_MISSION_FILE_REQUEST_CODE) {
            Uri fileUri = data.getData();
            vm.saveNewMission(fileUri, getContext());
            //Missions list will refresh automatically on the next lifecycle callback (onCreate)
        }
    }

    private String selectedMissionName = "";

    private void showExportToJsonDialogDialog() {
        String[] missionNames = vm.getMissions()
                .stream()
                .map(Mission::getName)
                .toArray(String[]::new);

        int checkedItem = 0;
        selectedMissionName = missionNames[checkedItem];

        new AlertDialog.Builder(getActivity())
                .setTitle("Choose a mission to export")
                .setSingleChoiceItems(missionNames, checkedItem, (dialogInterface, i) -> {
                    selectedMissionName = missionNames[i];
                })
                .setPositiveButton("Share", (dialog, id) -> {
                    //I'm asuming that mission names are unique
                    vm.getMissions()
                            .stream()
                            .filter(mission -> mission.getName().equals(selectedMissionName))
                            .findFirst()
                            .ifPresent(mission -> {
                                shareMission(mission);
                            });
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                .show();
    }

    public void shareMission(Mission mission) {
        try {
            String extraText;
            ShareableData data = new ShareableData();

            extraText = String.format(getString(R.string.fmt_load_this_json_file_to_join_the_mission), mission.getName());

            String newName = mission.getName().replace(" ", "-");
            String fileName = String.format("mission-%s", newName);
            File fd = File.createTempFile(fileName, ".json", Environment.getExternalStorageDirectory());//NON-NLS

            FileOutputStream fos = new FileOutputStream(fd);
            fos.write(makeTemplate(mission).getBytes());
            fos.close();

            Uri u = FileProvider.getUriForFile(getContext(), getString(R.string.file_content_provider), fd);

            fd.deleteOnExit();

            data.addUri(u);

            data.setText(String.format(getString(R.string.share_mission_email_subject), getString(R.string.app_name), mission.getName()));
            data.setHtml(extraText);
            data.setSubject(getString(R.string.app_name) + " : " + mission.getName());

            Intent intent = ShareHelper.buildShareIntent(getActivity(), data, getString(R.string.share_mission_upload_header));

            List<ResolveInfo> resInfoList = this.getActivity().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                this.getActivity().grantUriPermission(packageName, u, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public String makeTemplate(Mission mission) {

        DatabaseMission _mission = MappingUtils.mapMissionTo_Mission(mission);

        JSONObject jsonMission = new JSONObject();

        try {
            jsonMission.put(Engine.JsonFields.Mission.id, _mission._id);

            if (!Utils.isEmptyString(_mission._name)) {
                jsonMission.put(Engine.JsonFields.Mission.name, _mission._name);
            }

            if (!Utils.isEmptyString(_mission._description)) {
                jsonMission.put(Engine.JsonFields.Mission.description, _mission._description);
            }

            /*if (!Utils.isEmptyString(_missionModPin)) {
                jsonMission.put(Engine.JsonFields.Mission.modPin, _missionModPin);
            }*/

            jsonMission.put("multicastFailoverPolicy", 0);

            if (!Utils.isEmptyString(_mission._rpAddress) && _mission._rpPort > 0) {
                JSONObject rallypoint = new JSONObject();
                rallypoint.put("use", false);
                rallypoint.put(Engine.JsonFields.Rallypoint.Host.address, _mission._rpAddress);
                rallypoint.put(Engine.JsonFields.Rallypoint.Host.port, _mission._rpPort);
                jsonMission.put(Engine.JsonFields.Rallypoint.objectName, rallypoint);
            }

            if (_mission._groups != null && _mission._groups.size() > 0) {
                JSONArray groups = new JSONArray();

                for (DatabaseGroup _group : _mission._groups) {
                    JSONObject group = new JSONObject();
                    group.put("id", _group._id);
                    group.put("name", _group._name);
                    group.put("blockAdvertising", true);
                    group.put("cryptoPassword", _group._cryptoPassword);

                    JSONObject rx = new JSONObject();
                    rx.put("address", _group._rxAddress);
                    rx.put("port", _group._rxPort);
                    group.put("rx", rx);

                    JSONObject tx = new JSONObject();
                    tx.put("address", _group._txAddress);
                    tx.put("port", _group._txPort);
                    group.put("tx", tx);

                    group.put("type", _group._type);
                    if (_group._type == 1) { //Audio type
                        JSONObject timeline = new JSONObject();
                        timeline.put("enabled", true);
                        timeline.put("maxAudioTimeMs", 30000);
                        group.put("timeline", timeline);

                        JSONObject txAudio = new JSONObject();
                        txAudio.put("encoder", _group._txCodecId);
                        txAudio.put("fdx", false);
                        txAudio.put("framingMs", _group._txFramingMs);
                        txAudio.put("maxTxSecs", _group._maxTxSecs);
                        group.put("txAudio", txAudio);
                    }

                    groups.put(group);
                }

                jsonMission.put(Engine.JsonFields.Group.arrayName, groups);
            }
        } catch (Exception e) {
            jsonMission = null;
            e.printStackTrace();
        }

        return jsonMission.toString();
    }
}