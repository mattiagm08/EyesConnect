package com.nvision.eyesconnect.CameraPanel.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nvision.eyesconnect.CameraPanel.home.Connections.CallActivity;
import com.nvision.eyesconnect.R;
import com.nvision.eyesconnect.databinding.FragmentHomeBinding;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private CameraAdapter cameraAdapter;
    private boolean isDataLoaded = false; // Per evitare caricamenti multipli
    private DatabaseReference incomingCallsRef; // Firebase riferimento

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // Mantieni il fragment durante cambi di configurazione
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        if (!isDataLoaded) {
            loadCameraListFromPreferences();
            isDataLoaded = true;
        }

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        cameraAdapter = new CameraAdapter(
                homeViewModel.getCameraList(),
                this::updateNoCamerasText
        );

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(cameraAdapter);

        handleIncomingArguments(getArguments());
        updateNoCamerasText(homeViewModel.getCameraList().isEmpty());

        // Inizializzare ascolto delle chiamate in arrivo
        listenForIncomingCalls();

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
        String json = new Gson().toJson(homeViewModel.getCameraList());
        editor.putString("camera_list", json);
        editor.apply();
    }

    private void handleIncomingArguments(Bundle arguments) {
        if (arguments != null) {
            String roomID = arguments.getString("ROOM_ID");
            String deviceID1 = arguments.getString("DEVICE_ID_1");
            String deviceID2 = arguments.getString("DEVICE_ID_2");

            boolean alreadyExists = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                alreadyExists = homeViewModel.getCameraList().stream()
                        .anyMatch(item -> item.getRoomID().equals(roomID));
            }

            if (!alreadyExists) {
                CameraItem camera = new CameraItem();
                camera.setRoomID(roomID);
                camera.setDeviceID1(deviceID1);
                camera.setDeviceID2(deviceID2);
                camera.setCameraName("Camera's Name: ");

                homeViewModel.addCamera(camera);
                cameraAdapter.notifyItemInserted(homeViewModel.getCameraList().size() - 1);
            }
        }
    }

    private void updateNoCamerasText(boolean isEmpty) {
        if (isEmpty) {
            binding.textHome.setVisibility(View.VISIBLE);
        } else {
            binding.textHome.setVisibility(View.GONE);
        }
    }

    private void listenForIncomingCalls() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        incomingCallsRef = database.getReference("incoming_calls");

        incomingCallsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot callSnapshot : snapshot.getChildren()) {
                    String roomID = callSnapshot.child("roomID").getValue(String.class);
                    String callerDeviceID = callSnapshot.child("callerDeviceID").getValue(String.class);
                    String calleeDeviceID = callSnapshot.child("calleeDeviceID").getValue(String.class);

                    if (calleeDeviceID != null && calleeDeviceID.equals(getDeviceID())) {
                        showIncomingCallDialog(roomID, callerDeviceID, calleeDeviceID);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to listen for incoming calls", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showIncomingCallDialog(String roomID, String callerDeviceID, String calleeDeviceID) {
        new AlertDialog.Builder(getContext())
                .setTitle("Incoming Call")
                .setMessage("You have an incoming call from " + callerDeviceID)
                .setPositiveButton("Accept", (dialog, which) -> {
                    Intent intent = new Intent(getContext(), CallActivity.class);
                    intent.putExtra("ROOM_ID", roomID);
                    intent.putExtra("DEVICE_ID_1", callerDeviceID);
                    intent.putExtra("DEVICE_ID_2", calleeDeviceID);
                    startActivity(intent);
                })
                .setNegativeButton("Decline", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private String getDeviceID() {
        SharedPreferences preferences = requireActivity().getSharedPreferences("device_prefs", Context.MODE_PRIVATE);
        return preferences.getString("device_id", "UNKNOWN");
    }
}
