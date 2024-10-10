package com.nvision.eyesconnect.ui.home;

public class CameraItem {
    private String cameraId;   // ID univoco della telecamera
    private String cameraName; // Nome della telecamera
    private String roomId;     // ID della stanza a cui appartiene la telecamera
    private String deviceId1;  // ID del primo dispositivo (probabilmente il dispositivo che trasmette)
    private String deviceId2;  // ID del secondo dispositivo (probabilmente il dispositivo che riceve)

    // Costruttore per inizializzare un oggetto CameraItem
    public CameraItem(String cameraId, String cameraName, String roomId, String deviceId1, String deviceId2) {
        this.cameraId = cameraId;       // Assegna l'ID della telecamera
        this.cameraName = cameraName;   // Assegna il nome della telecamera
        this.roomId = roomId;           // Assegna l'ID della stanza
        this.deviceId1 = deviceId1;     // Assegna l'ID del primo dispositivo
        this.deviceId2 = deviceId2;     // Assegna l'ID del secondo dispositivo
    }

    // Metodo per ottenere l'ID della telecamera
    public String getCameraId() {
        return cameraId;
    }

    // Metodo per impostare un nuovo ID per la telecamera
    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    // Metodo per ottenere il nome della telecamera
    public String getCameraName() {
        return cameraName;
    }

    // Metodo per impostare un nuovo nome per la telecamera
    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    // Metodo per ottenere l'ID della stanza
    public String getRoomId() {
        return roomId;
    }

    // Metodo per impostare un nuovo ID per la stanza
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    // Metodo per ottenere l'ID del primo dispositivo
    public String getDeviceId1() {
        return deviceId1;
    }

    // Metodo per impostare un nuovo ID per il primo dispositivo
    public void setDeviceId1(String deviceId1) {
        this.deviceId1 = deviceId1;
    }

    // Metodo per ottenere l'ID del secondo dispositivo
    public String getDeviceId2() {
        return deviceId2;
    }

    // Metodo per impostare un nuovo ID per il secondo dispositivo
    public void setDeviceId2(String deviceId2) {
        this.deviceId2 = deviceId2;
    }
}
