package com.nvision.eyesconnect.ui.home;

public class CameraItem {
    private String cameraId;
    private String cameraName;
    private String roomId;    // Aggiungi il campo roomId
    private String deviceId1; // Aggiungi il campo deviceId1
    private String deviceId2; // Aggiungi il campo deviceId2

    public CameraItem(String cameraId, String cameraName, String roomId, String deviceId1, String deviceId2) {
        this.cameraId = cameraId;
        this.cameraName = cameraName;
        this.roomId = roomId;
        this.deviceId1 = deviceId1;
        this.deviceId2 = deviceId2;
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

    // Aggiungi il metodo getRoomId
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    // Aggiungi il metodo getDeviceId1
    public String getDeviceId1() {
        return deviceId1;
    }

    public void setDeviceId1(String deviceId1) {
        this.deviceId1 = deviceId1;
    }

    // Aggiungi il metodo getDeviceId2
    public String getDeviceId2() {
        return deviceId2;
    }

    public void setDeviceId2(String deviceId2) {
        this.deviceId2 = deviceId2;
    }
}
