package com.nvision.eyesconnect.ui.home;

public class CameraItem {
    private String cameraId;
    private String cameraName;

    public CameraItem(String cameraId, String cameraName) {
        this.cameraId = cameraId;
        this.cameraName = cameraName;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }
}
