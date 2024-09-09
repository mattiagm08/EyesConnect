package com.nvision.eyesconnect.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.nvision.eyesconnect.databinding.FragmentHomeBinding;
import com.nvision.eyesconnect.ui.home.CameraAdapter;
import com.nvision.eyesconnect.ui.home.CameraItem;
import java.util.ArrayList;
import java.util.List;

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

        // Ottieni il roomId dal Bundle
        Bundle arguments = getArguments();
        if (arguments != null) {
            String roomId = arguments.getString("ROOM_ID");
            if (roomId != null) {
                // Aggiungi una nuova telecamera con l'ID scansionato
                cameraList.add(new CameraItem(roomId, ""));
            }
        }

        // Imposta l'adattatore per la RecyclerView
        cameraAdapter = new CameraAdapter(cameraList);
        recyclerView.setAdapter(cameraAdapter);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
