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
                // Aggiorna la UI e le preferenze condivise (già gestito da CameraAdapter)
            }

            @Override
            public void onCameraItemDeleted(CameraItem cameraItem) {
                // Gestisci l'eliminazione della telecamera
                updateNoCamerasMessageVisibility();
            }
        });
        recyclerView.setAdapter(cameraAdapter);

        // Carica gli ID e i nomi delle telecamere dalle preferenze
        loadCameraItems();

        // Ottieni il roomId dal Bundle
        Bundle arguments = getArguments();
        if (arguments != null) {
            String roomId = arguments.getString("ROOM_ID");
            if (roomId != null && !roomId.isEmpty()) {
                // Aggiungi una nuova telecamera con l'ID scansionato se non esiste già
                boolean cameraExists = false;
                for (CameraItem item : cameraList) {
                    if (item.getCameraId().equals(roomId)) {
                        cameraExists = true;
                        break;
                    }
                }

                if (!cameraExists) {
                    cameraList.add(new CameraItem(roomId, ""));
                    cameraAdapter.notifyItemInserted(cameraList.size() - 1);
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
            cameraList.add(new CameraItem(cameraId, cameraName));
        }
        cameraAdapter.notifyDataSetChanged();
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
