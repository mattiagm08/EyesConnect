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

    private FragmentHomeBinding binding; // Variabile per il binding del layout
    private HomeViewModel homeViewModel; // ViewModel per gestire i dati delle telecamere
    private CameraAdapter cameraAdapter; // Adapter per la RecyclerView delle telecamere
    private boolean isDataLoaded = false; // Flag per evitare caricamenti multipli dei dati
    private DatabaseReference incomingCallsRef; // Riferimento Firebase per ascoltare le chiamate in arrivo

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mantiene il fragment anche durante i cambiamenti di configurazione (come rotazione dello schermo)

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Ottieni una istanza del ViewModel per la gestione delle telecamere

        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        // Se i dati non sono già stati caricati, carica la lista delle telecamere

        if (!isDataLoaded) {
            loadCameraListFromPreferences(); // Carica le telecamere salvate dalle preferenze
            isDataLoaded = true;
        }

        // Imposta il binding del layout

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot(); // Ottieni la vista radice del layout

        // Crea un nuovo adapter per la RecyclerView che visualizza la lista delle telecamere

        cameraAdapter = new CameraAdapter(
                homeViewModel.getCameraList(), // Passa la lista delle telecamere dal ViewModel
                this::updateNoCamerasText // Passa il metodo per aggiornare il messaggio quando non ci sono telecamere
        );

        // Imposta la RecyclerView con un layout manager lineare

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(cameraAdapter); // Imposta l'adapter alla RecyclerView

        // Gestisce gli argomenti passati al fragment

        handleIncomingArguments(getArguments());

        // Aggiorna il testo che indica se non ci sono telecamere

        updateNoCamerasText(homeViewModel.getCameraList().isEmpty());

        return root; // Ritorna la vista radice
    }

    @Override
    public void onPause() {
        super.onPause();
        saveCameraListToPreferences(); // Salva la lista delle telecamere quando il fragment è in pausa
    }

    // Carica la lista delle telecamere dalle preferenze salvate

    private void loadCameraListFromPreferences() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("camera_data", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("camera_list", null); // Ottieni la lista in formato JSON
        if (json != null) {
            Type type = new TypeToken<List<CameraItem>>() {
            }.getType(); // Tipo di lista di oggetti CameraItem
            List<CameraItem> savedList = new Gson().fromJson(json, type); // Converte il JSON in una lista di CameraItem
            if (savedList != null) {
                homeViewModel.getCameraList().clear(); // Pulisce la lista corrente
                homeViewModel.getCameraList().addAll(savedList); // Aggiunge la lista salvata
            }
        }
    }

    // Salva la lista delle telecamere nelle preferenze

    private void saveCameraListToPreferences() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("camera_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = new Gson().toJson(homeViewModel.getCameraList()); // Converte la lista in formato JSON
        editor.putString("camera_list", json); // Salva la lista JSON
        editor.apply(); // Applica la modifica
    }

    // Gestisce gli argomenti ricevuti (ad esempio quando una nuova telecamera è aggiunta)

    private void handleIncomingArguments(Bundle arguments) {
        if (arguments != null) {
            String roomID = arguments.getString("ROOM_ID");
            String deviceID1 = arguments.getString("DEVICE_ID_1");
            String deviceID2 = arguments.getString("DEVICE_ID_2");

            // Verifica se la stanza è già presente nella lista

            boolean alreadyExists = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                alreadyExists = homeViewModel.getCameraList().stream()
                        .anyMatch(item -> item.getRoomID().equals(roomID)); // Usa il flusso per verificare se la stanza esiste
            }

            // Se la stanza non esiste già, crea un nuovo oggetto CameraItem e aggiungilo alla lista

            if (!alreadyExists) {
                CameraItem camera = new CameraItem();
                camera.setRoomID(roomID);
                camera.setDeviceID1(deviceID1);
                camera.setDeviceID2(deviceID2);
                camera.setCameraName("Camera's Name: "); // Imposta un nome di default per la telecamera

                homeViewModel.addCamera(camera); // Aggiungi la telecamera al ViewModel
                cameraAdapter.notifyItemInserted(homeViewModel.getCameraList().size() - 1); // Notifica l'adapter dell'inserimento
            }
        }
    }

    // Aggiorna il testo che indica se non ci sono telecamere

    private void updateNoCamerasText(boolean isEmpty) {
        if (isEmpty) {
            binding.textHome.setVisibility(View.VISIBLE); // Mostra il testo se la lista è vuota
        } else {
            binding.textHome.setVisibility(View.GONE); // Nasconde il testo se ci sono telecamere
        }
    }

    // Ottieni l'ID del dispositivo dalle preferenze

    private String getDeviceID() {
        SharedPreferences preferences = requireActivity().getSharedPreferences("device_prefs", Context.MODE_PRIVATE);
        return preferences.getString("device_id", "UNKNOWN"); // Ritorna l'ID del dispositivo, o "UNKNOWN" se non trovato
    }
}
