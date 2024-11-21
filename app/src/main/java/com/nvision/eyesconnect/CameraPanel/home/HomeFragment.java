package com.nvision.eyesconnect.CameraPanel.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.nvision.eyesconnect.databinding.FragmentHomeBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private CameraAdapter cameraAdapter;
    private boolean isDataLoaded = false; // Per assicurarsi che i dati vengano caricati una sola volta

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // Mantieni il fragment durante i cambi di configurazione
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        // Carica i dati salvati una sola volta
        if (!isDataLoaded) {
            loadCameraListFromPreferences();
            isDataLoaded = true;
        }

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Configura il RecyclerView
        cameraAdapter = new CameraAdapter(homeViewModel.getCameraList());
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(cameraAdapter);

        // Gestisci gli argomenti passati
        handleIncomingArguments(getArguments());

        // Mostra o nascondi il testo "No Cameras"
        updateNoCamerasText();
        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveCameraListToPreferences();
    }

    private void loadCameraListFromPreferences() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("camera_data", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("camera_list", null);
        if (json != null) {
            Type type = new TypeToken<List<CameraItem>>() {}.getType();
            List<CameraItem> savedList = new Gson().fromJson(json, type);
            if (savedList != null) {
                homeViewModel.getCameraList().clear();
                homeViewModel.getCameraList().addAll(savedList);
            }
        }
    }

    private void saveCameraListToPreferences() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("camera_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(homeViewModel.getCameraList());
        editor.putString("camera_list", json);
        editor.apply();
    }

    private void handleIncomingArguments(Bundle arguments) {
        if (arguments != null) {
            String roomID = arguments.getString("ROOM_ID");
            String deviceID1 = arguments.getString("DEVICE_ID_1");
            String deviceID2 = arguments.getString("DEVICE_ID_2");

            // Evita duplicati
            boolean alreadyExists = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                alreadyExists = homeViewModel.getCameraList().stream()
                        .anyMatch(item -> item.roomID.equals(roomID));
            }

            if (!alreadyExists) {
                // Aggiungi un nuovo CameraItem
                CameraItem camera = new CameraItem();
                camera.roomID = roomID;
                camera.deviceID1 = deviceID1;
                camera.deviceID2 = deviceID2;
                camera.cameraName = "Camera " + (homeViewModel.getCameraList().size() + 1);

                homeViewModel.addCamera(camera);
                cameraAdapter.notifyItemInserted(homeViewModel.getCameraList().size() - 1);
            }
        }
    }

    private void updateNoCamerasText() {
        // Mostra un messaggio se la lista è vuota
        if (homeViewModel.getCameraList().isEmpty()) {
            binding.textHome.setVisibility(View.VISIBLE);
        } else {
            binding.textHome.setVisibility(View.GONE);
        }
    }
}
