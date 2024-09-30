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

public class CallActivity extends AppCompatActivity {

    private PeerConnection peerConnection;
    private PeerConnectionFactory peerConnectionFactory;
    private VideoTrack videoTrack;
    private AudioTrack audioTrack;
    private DatabaseReference signalingRef;
    private VideoSource videoSource;
    private AudioSource audioSource;
    private EglBase eglBase;
    private String roomId;
    private String device1Id;
    private String device2Id;
    private static final String TAG = "CallActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        // Recupero l'ID della stanza e i device ID passati da ScanActivity o HomeFragment
        roomId = getIntent().getStringExtra("roomId");
        device1Id = getIntent().getStringExtra("device1Id");
        device2Id = getIntent().getStringExtra("device2Id");

        // Configura Firebase
        signalingRef = FirebaseDatabase.getInstance().getReference("signaling").child("rooms").child(roomId);

        // Configura WebRTC
        initializePeerConnectionFactory();
        initializePeerConnections();
        addMediaStreamToConnection();

        // Inizio segnalazione
        startSignaling();
    }

    private void initializePeerConnectionFactory() {
        PeerConnectionFactory.InitializationOptions initializationOptions =
                PeerConnectionFactory.InitializationOptions.builder(this)
                        .setEnableInternalTracer(true)
                        .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);
        peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory();

        Toast.makeText(this, "PeerConnectionFactory initialized", Toast.LENGTH_SHORT).show();
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

    private void addMediaStreamToConnection() {
        MediaStream mediaStream = peerConnectionFactory.createLocalMediaStream("mediaStream");

        // Configurazione video
        VideoCapturer videoCapturer = createVideoCapturer();
        eglBase = EglBase.create();
        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
        videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(surfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());
        videoCapturer.startCapture(1280, 720, 30);
        videoTrack = peerConnectionFactory.createVideoTrack("videoTrack", videoSource);
        mediaStream.addTrack(videoTrack);

        // Configurazione audio
        audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        audioTrack = peerConnectionFactory.createAudioTrack("audioTrack", audioSource);
        mediaStream.addTrack(audioTrack);

        peerConnection.addStream(mediaStream);

        Toast.makeText(this, "Media stream added to connection", Toast.LENGTH_SHORT).show();
    }

    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        if (Camera2Enumerator.isSupported(this)) {
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
        }
        return videoCapturer;
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
    }

    private void startSignaling() {
        // Creazione dell'offerta
        peerConnection.createOffer(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.d(TAG, "Offer created successfully.");
                peerConnection.setLocalDescription(this, sessionDescription);

                signalingRef.child(device1Id).child("offer").setValue(sessionDescription).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Offer sent to Firebase: " + sessionDescription);
                    } else {
                        Log.e(TAG, "Failed to send offer to Firebase.");
                    }
                });
            }

            @Override
            public void onSetSuccess() {
                Log.d(TAG, "Local description set successfully");
            }

            @Override
            public void onCreateFailure(String error) {
                Log.e(TAG, "Failed to create offer: " + error);
            }

            @Override
            public void onSetFailure(String error) {
                Log.e(TAG, "Failed to set local description: " + error);
            }
        }, new MediaConstraints());

        // Ascolto la risposta dal Firebase
        signalingRef.child(device2Id).child("answer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SessionDescription answer = snapshot.getValue(SessionDescription.class);
                    if (answer != null) {
                        peerConnection.setRemoteDescription(new SdpObserver() {
                            @Override
                            public void onCreateSuccess(SessionDescription sessionDescription) {}

                            @Override
                            public void onSetSuccess() {
                                Log.d(TAG, "Remote description set successfully.");
                            }

                            @Override
                            public void onCreateFailure(String error) {
                                Log.e(TAG, "Failed to create remote description: " + error);
                            }

                            @Override
                            public void onSetFailure(String error) {
                                Log.e(TAG, "Failed to set remote description: " + error);
                            }
                        }, answer);
                    } else {
                        Log.e(TAG, "Received answer is null.");
                    }
                } else {
                    Log.e(TAG, "No answer received.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Firebase listener cancelled: " + error.getMessage());
            }
        });
    }

    private class CustomPeerConnectionObserver implements PeerConnection.Observer {
        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            signalingRef.child(device1Id).child("candidates").push().setValue(iceCandidate);
            Toast.makeText(CallActivity.this, "ICE Candidate sent", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(CallActivity.this, "ICE Connection State: " + iceConnectionState, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "ICE Connection State: " + iceConnectionState);
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {}

        @Override
        public void onAddStream(MediaStream mediaStream) {
            Toast.makeText(CallActivity.this, "Remote stream added", Toast.LENGTH_SHORT).show();
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
        if (eglBase != null) {
            eglBase.release();
        }
    }
}
