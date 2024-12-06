package com.nvision.eyesconnect.CameraPanel.home.Connections;

import org.webrtc.IceCandidate;

public interface CandidateEventListener {
    void onIceCandidateReceived(IceCandidate iceCandidate);
}
