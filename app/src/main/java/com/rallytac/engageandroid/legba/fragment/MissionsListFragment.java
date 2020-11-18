package com.rallytac.engageandroid.legba.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.NavHostController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rallytac.engage.engine.Engine;
import com.rallytac.engageandroid.ActiveConfiguration;
import com.rallytac.engageandroid.Analytics;
import com.rallytac.engageandroid.DatabaseGroup;
import com.rallytac.engageandroid.DatabaseMission;
import com.rallytac.engageandroid.EngageApplication;
import com.rallytac.engageandroid.Globals;
import com.rallytac.engageandroid.GroupDescriptor;
import com.rallytac.engageandroid.MissionDatabase;
import com.rallytac.engageandroid.MissionListActivity;
import com.rallytac.engageandroid.R;
import com.rallytac.engageandroid.ShareHelper;
import com.rallytac.engageandroid.ShareMissionActivity;
import com.rallytac.engageandroid.ShareableData;
import com.rallytac.engageandroid.SimpleUiMainActivity;
import com.rallytac.engageandroid.UploadMissionTask;
import com.rallytac.engageandroid.Utils;
import com.rallytac.engageandroid.databinding.FragmentMissionsListBinding;
import com.rallytac.engageandroid.legba.HostActivity;
import com.rallytac.engageandroid.legba.data.DataManager;
import com.rallytac.engageandroid.legba.data.dto.Mission;
import com.rallytac.engageandroid.SettingsActivity;
import com.rallytac.engageandroid.legba.HostActivity;

import com.rallytac.engageandroid.legba.data.DataManager;
import com.rallytac.engageandroid.legba.data.dto.Mission;
import com.rallytac.engageandroid.databinding.FragmentMissionsListBinding;
import com.rallytac.engageandroid.legba.data.dto.MissionDao;
import com.rallytac.engageandroid.legba.viewmodel.MissionViewModel;
import com.rallytac.engageandroid.legba.viewmodel.MissionsListViewModel;
import com.rallytac.engageandroid.legba.viewmodel.ViewModelFactory;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import okio.BufferedSource;
import okio.Okio;
import okio.Source;
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.create_manually_action) {
            NavHostFragment.findNavController(this)
                    .navigate(MissionsListFragmentDirections.actionMissionsFragmentToMissionEditActivity(null));
            return true;
        } else if (itemId == R.id.load_from_json_action) {
            startLoadMissionFromLocalFile();
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

    public void onClickShare(Mission mission) {
/*        boolean sharingJson = true;
        try {
            String extraText;
            ShareableData data = new ShareableData();

            extraText = String.format(getString(R.string.fmt_load_this_json_file_to_join_the_mission), mission.getName());

            String fileName = String.format("mission-%s", mission.getName().replace(" ", "-");
            File fd = File.createTempFile(fileName, ".json", Environment.getExternalStorageDirectory());//NON-NLS

            FileOutputStream fos = new FileOutputStream(fd);

            fos.write(_jsonConfiguration.toString().getBytes());
            fos.close();

            Uri u = FileProvider.getUriForFile(this, getString(R.string.file_content_provider), fd);

            fd.deleteOnExit();
            data.addUri(u);

            Globals.getEngageApplication().logEvent(Analytics.MISSION_SHARE_JSON);


            data.setText(String.format(getString(R.string.share_mission_email_subject), getString(R.string.app_name), misison.getName()));

            data.setHtml(extraText);

            data.setSubject(getString(R.string.app_name) + " : " + misison.getName());
            startActivity(ShareHelper.buildShareIntent(this, data, getString(R.string.share_mission_upload_header)));
        } catch (Exception e) {
            Globals.getEngageApplication().logEvent(Analytics.MISSION_SHARE_EXCEPTION);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }*/
    }

    public String makeTemplate(DatabaseMission _mission) {
        JSONObject rc = new JSONObject();

        try {
            rc.put(Engine.JsonFields.Mission.id, _mission._id);

            if (!Utils.isEmptyString(_mission._name)) {
                rc.put(Engine.JsonFields.Mission.name, _mission._name);
            }

            if (!Utils.isEmptyString(_mission._description)) {
                rc.put(Engine.JsonFields.Mission.description, _mission._description);
            }

            /*if (!Utils.isEmptyString(_missionModPin)) {
                rc.put(Engine.JsonFields.Mission.modPin, _missionModPin);
            }*/

            rc.put("multicastFailoverPolicy", 0);

            if (!Utils.isEmptyString(_mission._rpAddress) && _mission._rpPort > 0) {
                JSONObject rallypoint = new JSONObject();
                rallypoint.put("use", false);
                rallypoint.put(Engine.JsonFields.Rallypoint.Host.address, _mission._rpAddress);
                rallypoint.put(Engine.JsonFields.Rallypoint.Host.port, _mission._rpPort);
                rc.put(Engine.JsonFields.Rallypoint.objectName, rallypoint);
            }

            if (_mission._groups != null && _mission._groups.size() > 0) {
                JSONArray groups = new JSONArray();

                for (DatabaseGroup gd : _mission._groups) {
                    //TODO: Create
                    /*if (!gd.isDynamic()) {
                        JSONObject group = new JSONObject(gd.jsonConfiguration);
                        groups.put(group);
                    }*/
                }

                rc.put(Engine.JsonFields.Group.arrayName, groups);
            }
        } catch (Exception e) {
            rc = null;
            e.printStackTrace();
        }

        return rc.toString();
    }
}