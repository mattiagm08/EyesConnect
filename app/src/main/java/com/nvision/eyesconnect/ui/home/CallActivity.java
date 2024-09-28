package com.nvision.eyesconnect.ui.home;

import android.os.Bundle;
import android.view.SurfaceView;
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
    private SurfaceView localVideoView;
    private SurfaceView remoteVideoView;
    private EglBase eglBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        // Inizializza le SurfaceView
        localVideoView = findViewById(R.id.localVideoView);
        remoteVideoView = findViewById(R.id.remoteVideoView);

        // Configura Firebase
        signalingRef = FirebaseDatabase.getInstance().getReference("signaling");

        // Configura WebRTC
        initializePeerConnectionFactory();
        initializePeerConnections();
        addMediaStreamToConnection();
        startSignaling();
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

        // Configurazione audio
        audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        audioTrack = peerConnectionFactory.createAudioTrack("audioTrack", audioSource);
        mediaStream.addTrack(audioTrack);

        peerConnection.addStream(mediaStream);
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
        peerConnection.createOffer(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(this, sessionDescription);
                signalingRef.child("deviceId").child("offer").setValue(sessionDescription);
            }

            @Override
            public void onSetSuccess() {}

            @Override
            public void onCreateFailure(String error) {}

            @Override
            public void onSetFailure(String error) {}
        }, new MediaConstraints());

        signalingRef.child("deviceId").child("answer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                SessionDescription answer = snapshot.getValue(SessionDescription.class);
                if (answer != null) {
                    peerConnection.setRemoteDescription(new SdpObserver() {
                        @Override
                        public void onCreateSuccess(SessionDescription sessionDescription) {}

                        @Override
                        public void onSetSuccess() {}

                        @Override
                        public void onCreateFailure(String s) {}

                        @Override
                        public void onSetFailure(String error) {}
                    }, answer);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private class CustomPeerConnectionObserver implements PeerConnection.Observer {
        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            signalingRef.child("deviceId").child("candidate").push().setValue(iceCandidate);
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {}

        @Override
        public void onDataChannel(DataChannel dataChannel) {}

        @Override
        public void onIceConnectionReceivingChange(boolean b) {}

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {}

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {}

        @Override
        public void onAddStream(MediaStream mediaStream) {
            // Gestisci lo stream remoto, ad esempio puoi integrarlo qui in `remoteVideoView` utilizzando un approccio adeguato
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
