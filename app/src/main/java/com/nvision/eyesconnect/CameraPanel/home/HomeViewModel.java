package com.nvision.eyesconnect.CameraPanel.home;

import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    // Lista che contiene gli oggetti CameraItem

    private final List<CameraItem> cameraList = new ArrayList<>();

    // Metodo per ottenere la lista delle telecamere

    public List<CameraItem> getCameraList() {
        return cameraList;
    }

    // Metodo per aggiungere una telecamera alla lista

    public void addCamera(CameraItem camera) {

        // Evita di aggiungere duplicati verificando se l'elemento è già presente nella lista

        if (!cameraList.contains(camera)) {
            cameraList.add(camera);
        }
    }
}
