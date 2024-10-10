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

        // Inizializza la RecyclerView e imposta il suo layout
        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inizializza la lista che conterrà gli oggetti CameraItem
        cameraList = new ArrayList<>();

        // Configura l'adattatore della RecyclerView e assegna la lista delle telecamere
        cameraAdapter = new CameraAdapter(requireContext(), cameraList, new CameraAdapter.CameraItemListener() {
            @Override
            public void onCameraItemUpdated(CameraItem cameraItem) {
                // Quando un elemento della lista viene aggiornato, salva le modifiche nelle SharedPreferences
                saveCameraItemToPreferences(cameraItem);
            }

            @Override
            public void onCameraItemDeleted(CameraItem cameraItem) {
                // Quando un elemento viene eliminato, rimuovilo dalle SharedPreferences
                removeCameraIdFromPreferences(cameraItem.getCameraId());
                updateNoCamerasMessageVisibility(); // Aggiorna la visibilità del messaggio "No Cameras"
            }
        });
        recyclerView.setAdapter(cameraAdapter);

        // Carica gli elementi delle telecamere salvati dalle SharedPreferences
        loadCameraItems();

        // Verifica se ci sono argomenti passati a questo fragment
        Bundle arguments = getArguments();
        if (arguments != null) {
            // Ottieni il roomId, deviceId1 e deviceId2 dal Bundle
            String roomId = arguments.getString("ROOM_ID");
            String deviceId1 = arguments.getString("DEVICE_ID_1");
            String deviceId2 = arguments.getString("DEVICE_ID_2");

            // Se il roomId non è vuoto, verifica se la telecamera è già presente nella lista
            if (roomId != null && !roomId.isEmpty()) {
                boolean cameraExists = false;
                for (CameraItem item : cameraList) {
                    if (item.getRoomId().equals(roomId)) {
                        cameraExists = true;
                        break;
                    }
                }

                // Se la telecamera con roomId non esiste, aggiungila
                if (!cameraExists) {
                    cameraList.add(new CameraItem(roomId, "", roomId, deviceId1, deviceId2));
                    cameraAdapter.notifyItemInserted(cameraList.size() - 1);
                    // Salva la nuova telecamera nelle SharedPreferences
                    saveCameraItemToPreferences(new CameraItem(roomId, "", roomId, deviceId1, deviceId2));
                }
            }
        }

        // Aggiorna la visibilità del messaggio se non ci sono telecamere
        updateNoCamerasMessageVisibility();

        return root;
    }

    /**
     * Carica le telecamere salvate nelle SharedPreferences e le aggiunge alla lista delle telecamere.
     */
    private void loadCameraItems() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("CameraPreferences", Context.MODE_PRIVATE);
        Set<String> cameraIds = sharedPreferences.getStringSet("camera_ids", new HashSet<>());

        // Cicla attraverso gli ID delle telecamere salvati
        for (String cameraId : cameraIds) {
            String cameraName = sharedPreferences.getString(cameraId, "");
            String roomId = sharedPreferences.getString(cameraId + "_roomId", "");
            String deviceId1 = sharedPreferences.getString(cameraId + "_deviceId1", "");
            String deviceId2 = sharedPreferences.getString(cameraId + "_deviceId2", "");
            // Aggiungi l'oggetto CameraItem alla lista delle telecamere
            cameraList.add(new CameraItem(cameraId, cameraName, roomId, deviceId1, deviceId2));
        }
        cameraAdapter.notifyDataSetChanged(); // Notifica l'adattatore che i dati sono cambiati
    }

    /**
     * Salva un oggetto CameraItem nelle SharedPreferences.
     */
    private void saveCameraItemToPreferences(CameraItem cameraItem) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("CameraPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Ottieni l'insieme degli ID delle telecamere salvati
        Set<String> cameraIds = sharedPreferences.getStringSet("camera_ids", new HashSet<>());
        if (cameraIds == null) {
            cameraIds = new HashSet<>();
        }
        // Aggiungi l'ID della telecamera corrente
        cameraIds.add(cameraItem.getCameraId());

        // Salva i dettagli della telecamera
        editor.putStringSet("camera_ids", cameraIds);
        editor.putString(cameraItem.getCameraId(), cameraItem.getCameraName());
        editor.putString(cameraItem.getCameraId() + "_roomId", cameraItem.getRoomId());
        editor.putString(cameraItem.getCameraId() + "_deviceId1", cameraItem.getDeviceId1());
        editor.putString(cameraItem.getCameraId() + "_deviceId2", cameraItem.getDeviceId2());

        // Applica le modifiche
        editor.apply();
    }

    /**
     * Rimuove un oggetto CameraItem dalle SharedPreferences.
     */
    private void removeCameraIdFromPreferences(String cameraId) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("CameraPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Ottieni l'insieme degli ID delle telecamere salvati
        Set<String> cameraIds = sharedPreferences.getStringSet("camera_ids", new HashSet<>());
        if (cameraIds != null && cameraIds.contains(cameraId)) {
            cameraIds.remove(cameraId); // Rimuovi l'ID della telecamera eliminata
            editor.putStringSet("camera_ids", cameraIds);

            // Rimuovi i dettagli della telecamera
            editor.remove(cameraId);
            editor.remove(cameraId + "_roomId");
            editor.remove(cameraId + "_deviceId1");
            editor.remove(cameraId + "_deviceId2");

            // Applica le modifiche
            editor.apply();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Libera la memoria associata al binding quando la vista viene distrutta
    }

    /**
     * Mostra o nasconde il messaggio "No Cameras" in base alla presenza di telecamere nella lista.
     */
    private void updateNoCamerasMessageVisibility() {
        if (cameraList.isEmpty()) {
            binding.textHome.setVisibility(View.VISIBLE); // Mostra il messaggio
        } else {
            binding.textHome.setVisibility(View.GONE); // Nascondi il messaggio
        }
    }
}
