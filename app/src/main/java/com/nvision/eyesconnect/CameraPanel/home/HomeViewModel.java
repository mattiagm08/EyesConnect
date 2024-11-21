package com.nvision.eyesconnect.CameraPanel.home;

import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {
    private final List<CameraItem> cameraList = new ArrayList<>();

    public List<CameraItem> getCameraList() {
        return cameraList;
    }

    public void addCamera(CameraItem camera) {
        // Evita duplicati verificando se l'elemento è già presente
        if (!cameraList.contains(camera)) {
            cameraList.add(camera);
        }
    }
}
