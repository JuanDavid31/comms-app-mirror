package com.rallytac.engageandroid.legba.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import com.rallytac.engageandroid.legba.data.DataManager;
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
        DataManager.getInstance().leaveMissionActiveMission();

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
    public void onResume() {
        super.onResume();
        if(getActivity() != null){//Locks portrait mode
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(getActivity() != null){ //Unlocks rotation
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }
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
                            .ifPresent(this::shareMission);
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
            fos.write(vm.makeTemplate(mission).getBytes());
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
}