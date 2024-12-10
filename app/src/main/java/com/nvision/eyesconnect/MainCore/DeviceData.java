package com.nvision.eyesconnect.MainCore;

public class DeviceData {

    // Dichiarazione elementi Firebase (dentro i deviceID)
    private String answer;
    private String candidates;
    private String offer;

    // Costruttore vuoto richiesto da Firebase

    public DeviceData() {
        this.answer = "null";
        this.candidates = "null";
        this.offer = "null";
    }

    // Costruttore con parametri (opzionale)

    public DeviceData(String answer, String candidates, String offer) {
        this.answer = answer;
        this.candidates = candidates;
        this.offer = offer;
    }

    // Getter e Setter

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getCandidates() {
        return candidates;
    }

    public void setCandidates(String candidates) {
        this.candidates = candidates;
    }

    public String getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }
}
