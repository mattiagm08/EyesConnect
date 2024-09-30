package com.nvision.eyesconnect.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nvision.eyesconnect.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private CameraAdapter cameraAdapter;
    private List<CameraItem> cameraList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inizializza la lista delle telecamere
        cameraList = new ArrayList<>();

        // Imposta l'adattatore per la RecyclerView
        cameraAdapter = new CameraAdapter(requireContext(), cameraList, new CameraAdapter.CameraItemListener() {
            @Override
            public void onCameraItemUpdated(CameraItem cameraItem) {
                // Gestisci l'aggiornamento del nome della telecamera
                saveCameraItemToPreferences(cameraItem);
            }

            @Override
            public void onCameraItemDeleted(CameraItem cameraItem) {
                // Gestisci l'eliminazione della telecamera
                removeCameraIdFromPreferences(cameraItem.getCameraId());
                updateNoCamerasMessageVisibility();
            }
        });
        recyclerView.setAdapter(cameraAdapter);

        // Carica gli ID e i nomi delle telecamere dalle preferenze
        loadCameraItems();

        // Ottieni il roomId, deviceId1, e deviceId2 dal Bundle
        Bundle arguments = getArguments();
        if (arguments != null) {
            String roomId = arguments.getString("ROOM_ID");
            String deviceId1 = arguments.getString("DEVICE_ID_1");
            String deviceId2 = arguments.getString("DEVICE_ID_2");

            if (roomId != null && !roomId.isEmpty()) {
                // Aggiungi una nuova telecamera con l'ID scansionato se non esiste già
                boolean cameraExists = false;
                for (CameraItem item : cameraList) {
                    if (item.getRoomId().equals(roomId)) {
                        cameraExists = true;
                        break;
                    }
                }

                if (!cameraExists) {
                    // Aggiungi la nuova telecamera con roomId, deviceId1 e deviceId2
                    cameraList.add(new CameraItem(roomId, "", roomId, deviceId1, deviceId2));
                    cameraAdapter.notifyItemInserted(cameraList.size() - 1);
                    saveCameraItemToPreferences(new CameraItem(roomId, "", roomId, deviceId1, deviceId2));
                }
            }
        }

        updateNoCamerasMessageVisibility();

        return root;
    }

    private void loadCameraItems() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("CameraPreferences", Context.MODE_PRIVATE);
        Set<String> cameraIds = sharedPreferences.getStringSet("camera_ids", new HashSet<>());
        for (String cameraId : cameraIds) {
            String cameraName = sharedPreferences.getString(cameraId, "");
            String roomId = sharedPreferences.getString(cameraId + "_roomId", "");
            String deviceId1 = sharedPreferences.getString(cameraId + "_deviceId1", "");
            String deviceId2 = sharedPreferences.getString(cameraId + "_deviceId2", "");
            cameraList.add(new CameraItem(cameraId, cameraName, roomId, deviceId1, deviceId2));
        }
        cameraAdapter.notifyDataSetChanged();
    }

    private void saveCameraItemToPreferences(CameraItem cameraItem) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("CameraPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Set<String> cameraIds = sharedPreferences.getStringSet("camera_ids", new HashSet<>());
        if (cameraIds == null) {
            cameraIds = new HashSet<>();
        }
        cameraIds.add(cameraItem.getCameraId());

        editor.putStringSet("camera_ids", cameraIds);
        editor.putString(cameraItem.getCameraId(), cameraItem.getCameraName());
        editor.putString(cameraItem.getCameraId() + "_roomId", cameraItem.getRoomId());
        editor.putString(cameraItem.getCameraId() + "_deviceId1", cameraItem.getDeviceId1());
        editor.putString(cameraItem.getCameraId() + "_deviceId2", cameraItem.getDeviceId2());

        editor.apply();
    }

    private void removeCameraIdFromPreferences(String cameraId) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("CameraPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Set<String> cameraIds = sharedPreferences.getStringSet("camera_ids", new HashSet<>());
        if (cameraIds != null && cameraIds.contains(cameraId)) {
            cameraIds.remove(cameraId);
            editor.putStringSet("camera_ids", cameraIds);
            editor.remove(cameraId);
            editor.remove(cameraId + "_roomId");
            editor.remove(cameraId + "_deviceId1");
            editor.remove(cameraId + "_deviceId2");
            editor.apply();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateNoCamerasMessageVisibility() {
        if (cameraList.isEmpty()) {
            binding.textHome.setVisibility(View.VISIBLE);
        } else {
            binding.textHome.setVisibility(View.GONE);
        }
    }
}
