package com.nvision.eyesconnect.CameraPanel.home;

import java.util.Objects;

public class CameraItem {

    // Variabili di istanza per memorizzare le informazioni della telecamera

    private String cameraName; // Nome della telecamera
    private String roomID;     // ID della stanza
    private String deviceID1;  // ID del dispositivo 1
    private String deviceID2;  // ID del dispositivo 2

    // Costruttore per inizializzare un oggetto CameraItem con i dati forniti

    public CameraItem(String cameraName, String roomID, String deviceID1, String deviceID2) {
        this.cameraName = cameraName;
        this.roomID = roomID;
        this.deviceID1 = deviceID1;
        this.deviceID2 = deviceID2;
    }

    // Costruttore senza parametri, utile per la creazione di oggetti vuoti

    public CameraItem() {
    }

    // Getter per ottenere il nome della telecamera

    public String getCameraName() {
        return cameraName;
    }

    // Setter per impostare il nome della telecamera

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    // Getter per ottenere l'ID della stanza

    public String getRoomID() {
        return roomID;
    }

    // Setter per impostare l'ID della stanza

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    // Getter per ottenere l'ID del dispositivo 1

    public String getDeviceID1() {
        return deviceID1;
    }

    // Setter per impostare l'ID del dispositivo 1

    public void setDeviceID1(String deviceID1) {
        this.deviceID1 = deviceID1;
    }

    // Getter per ottenere l'ID del dispositivo 2

    public String getDeviceID2() {
        return deviceID2;
    }

    // Setter per impostare l'ID del dispositivo 2

    public void setDeviceID2(String deviceID2) {
        this.deviceID2 = deviceID2;
    }

    // Metodo per rappresentare l'oggetto CameraItem come una stringa

    @Override
    public String toString() {
        return "CameraItem{" +
                "cameraName='" + cameraName + '\'' +
                ", roomID='" + roomID + '\'' +
                ", deviceID1='" + deviceID1 + '\'' +
                ", deviceID2='" + deviceID2 + '\'' +
                '}';
    }

    // Override del metodo equals per confrontare due oggetti CameraItem

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Se sono lo stesso oggetto, ritorna true
        if (o == null || getClass() != o.getClass()) return false; // Se l'oggetto è null o di classe diversa, ritorna false
        CameraItem that = (CameraItem) o; // Cast dell'oggetto in CameraItem

        // Confronta tutti i campi per vedere se sono uguali

        return Objects.equals(cameraName, that.cameraName) &&
                Objects.equals(roomID, that.roomID) &&
                Objects.equals(deviceID1, that.deviceID1) &&
                Objects.equals(deviceID2, that.deviceID2);
    }

    // Override del metodo hashCode per generare un valore di hash univoco per l'oggetto

    @Override
    public int hashCode() {
        return Objects.hash(cameraName, roomID, deviceID1, deviceID2); // Calcola il valore hash basato sui campi
    }
}
