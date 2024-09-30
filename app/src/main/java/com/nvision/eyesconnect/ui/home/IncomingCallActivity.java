package com.nvision.eyesconnect.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.webrtc.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nvision.eyesconnect.R;

import java.util.ArrayList;

public class IncomingCallActivity extends AppCompatActivity {

    private PeerConnection peerConnection;
    private PeerConnectionFactory peerConnectionFactory;
    private DatabaseReference signalingRef;
    private String roomId;
    private String deviceId;
    private static final String TAG = "IncomingCallActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        roomId = getIntent().getStringExtra("roomId");
        deviceId = getIntent().getStringExtra("deviceId");

        signalingRef = FirebaseDatabase.getInstance().getReference("signaling").child("rooms").child(roomId);

        initializePeerConnectionFactory();
        initializePeerConnections();
        listenForOffer();
    }

    private void initializePeerConnectionFactory() {
        PeerConnectionFactory.InitializationOptions initializationOptions =
                PeerConnectionFactory.InitializationOptions.builder(this)
                        .setEnableInternalTracer(true)
                        .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);
        peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory();
    }

    private void initializePeerConnections() {
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(new ArrayList<>());
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, new CustomPeerConnectionObserver());

        if (peerConnection != null) {
            Toast.makeText(this, "Peer connection created", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to create peer connection", Toast.LENGTH_LONG).show();
        }
    }

    private void listenForOffer() {
        signalingRef.child(deviceId).child("offer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SessionDescription offer = snapshot.getValue(SessionDescription.class);
                    if (offer != null) {
                        peerConnection.setRemoteDescription(new SdpObserver() {
                            @Override
                            public void onCreateSuccess(SessionDescription sessionDescription) {}

                            @Override
                            public void onSetSuccess() {
                                Log.d(TAG, "Remote description set successfully.");
                                answerCall();
                            }

                            @Override
                            public void onCreateFailure(String error) {
                                Log.e(TAG, "Failed to create remote description: " + error);
                            }

                            @Override
                            public void onSetFailure(String error) {
                                Log.e(TAG, "Failed to set remote description: " + error);
                            }
                        }, offer);
                    } else {
                        Log.e(TAG, "Received offer is null.");
                    }
                } else {
                    Log.e(TAG, "No offer received.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Firebase listener cancelled: " + error.getMessage());
            }
        });
    }

    private void answerCall() {
        peerConnection.createAnswer(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.d(TAG, "Answer created successfully.");
                peerConnection.setLocalDescription(this, sessionDescription);

                signalingRef.child(deviceId).child("answer").setValue(sessionDescription).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Answer sent to Firebase: " + sessionDescription);
                    } else {
                        Log.e(TAG, "Failed to send answer to Firebase.");
                    }
                });
            }

            @Override
            public void onSetSuccess() {
                Log.d(TAG, "Local description set successfully");
            }

            @Override
            public void onCreateFailure(String error) {
                Log.e(TAG, "Failed to create answer: " + error);
            }

            @Override
            public void onSetFailure(String error) {
                Log.e(TAG, "Failed to set local description: " + error);
            }
        }, new MediaConstraints());
    }

    private class CustomPeerConnectionObserver implements PeerConnection.Observer {
        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            signalingRef.child(deviceId).child("candidates").push().setValue(iceCandidate);
            Toast.makeText(IncomingCallActivity.this, "ICE Candidate sent", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "ICE Candidate sent: " + iceCandidate);
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {}

        @Override
        public void onDataChannel(DataChannel dataChannel) {}

        @Override
        public void onIceConnectionReceivingChange(boolean b) {}

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            Toast.makeText(IncomingCallActivity.this, "ICE Connection State: " + iceConnectionState, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "ICE Connection State: " + iceConnectionState);
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {}

        @Override
        public void onAddStream(MediaStream mediaStream) {
            Toast.makeText(IncomingCallActivity.this, "Remote stream added", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Remote stream added");
        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {}

        @Override
        public void onRemoveStream(MediaStream mediaStream) {}

        @Override
        public void onRenegotiationNeeded() {}

        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (peerConnection != null) {
            peerConnection.close();
        }
        if (peerConnectionFactory != null) {
            peerConnectionFactory.dispose();
        }
    }
}
