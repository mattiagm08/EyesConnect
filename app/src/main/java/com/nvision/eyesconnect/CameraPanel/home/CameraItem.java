package com.nvision.eyesconnect.CameraPanel.home;

import java.util.Objects;

public class CameraItem {
    private String cameraName;
    private String roomID;
    private String deviceID1;
    private String deviceID2;

    public CameraItem(String cameraName, String roomID, String deviceID1, String deviceID2) {
        this.cameraName = cameraName;
        this.roomID = roomID;
        this.deviceID1 = deviceID1;
        this.deviceID2 = deviceID2;
    }

    public CameraItem() {
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getDeviceID1() {
        return deviceID1;
    }

    public void setDeviceID1(String deviceID1) {
        this.deviceID1 = deviceID1;
    }

    public String getDeviceID2() {
        return deviceID2;
    }

    public void setDeviceID2(String deviceID2) {
        this.deviceID2 = deviceID2;
    }

    @Override
    public String toString() {
        return "CameraItem{" +
                "cameraName='" + cameraName + '\'' +
                ", roomID='" + roomID + '\'' +
                ", deviceID1='" + deviceID1 + '\'' +
                ", deviceID2='" + deviceID2 + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CameraItem that = (CameraItem) o;
        return Objects.equals(cameraName, that.cameraName) &&
                Objects.equals(roomID, that.roomID) &&
                Objects.equals(deviceID1, that.deviceID1) &&
                Objects.equals(deviceID2, that.deviceID2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cameraName, roomID, deviceID1, deviceID2);
    }
}
