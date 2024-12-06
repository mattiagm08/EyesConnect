package com.nvision.eyesconnect.CameraPanel.home.Connections;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;
import com.nvision.eyesconnect.R;

import org.webrtc.*;

import java.util.ArrayList;

public class CallActivity extends AppCompatActivity {

    private EglBase rootEglBase;
    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection peerConnection;
    private SurfaceViewRenderer localVideoView;
    private SurfaceViewRenderer remoteVideoView;

    private String roomID;
    private String deviceID1;
    private String deviceID2;

    private DatabaseReference roomRef;
    private CameraVideoCapturer videoCapturer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        // Retrieve data passed via Intent
        roomID = getIntent().getStringExtra("ROOM_ID");
        deviceID1 = getIntent().getStringExtra("DEVICE_ID_1");
        deviceID2 = getIntent().getStringExtra("DEVICE_ID_2");

        // Initialize views
        localVideoView = findViewById(R.id.local_video_view);
        remoteVideoView = findViewById(R.id.remote_video_view);
        Button endCallButton = findViewById(R.id.end_call_button);

        // Initialize WebRTC connection
        setupConnection();

        // Set listener for the end call button
        endCallButton.setOnClickListener(v -> {
            try {
                endCall();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setupConnection() {
        // Initialize PeerConnectionFactory
        PeerConnectionFactory.InitializationOptions options =
                PeerConnectionFactory.InitializationOptions.builder(this)
                        .setEnableInternalTracer(true)
                        .createInitializationOptions();
        PeerConnectionFactory.initialize(options);

        peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory();

        rootEglBase = EglBase.create();
        localVideoView.init(rootEglBase.getEglBaseContext(), null);
        remoteVideoView.init(rootEglBase.getEglBaseContext(), null);

        localVideoView.setMirror(true);
        remoteVideoView.setMirror(false);

        VideoTrack localVideoTrack = createLocalVideoTrack();
        if (localVideoTrack != null) {
            localVideoTrack.addSink(localVideoView);
        }
        AudioTrack localAudioTrack = createLocalAudioTrack();

        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());

        peerConnection = peerConnectionFactory.createPeerConnection(iceServers, new PeerConnectionObserver());

        if (localVideoTrack != null) {
            MediaStream mediaStream = peerConnectionFactory.createLocalMediaStream("ARDAMS");
            mediaStream.addTrack(localVideoTrack);
            mediaStream.addTrack(localAudioTrack);
            peerConnection.addStream(mediaStream);
        }

        initializeSignaling();
    }

    private VideoTrack createLocalVideoTrack() {
        Camera2Enumerator camera2Enumerator = new Camera2Enumerator(this);
        String[] deviceNames = camera2Enumerator.getDeviceNames();
        videoCapturer = null;

        for (String deviceName : deviceNames) {
            if (camera2Enumerator.isFrontFacing(deviceName)) {
                videoCapturer = camera2Enumerator.createCapturer(deviceName, null);
                break;
            }
        }

        if (videoCapturer == null) {
            Toast.makeText(this, "No front camera found", Toast.LENGTH_SHORT).show();
            return null;
        }

        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext()),
                this, videoSource.getCapturerObserver());
        videoCapturer.startCapture(1280, 720, 30);

        return peerConnectionFactory.createVideoTrack("ARDAMSv0", videoSource);
    }

    private AudioTrack createLocalAudioTrack() {
        return peerConnectionFactory.createAudioTrack("ARDAMSa0", peerConnectionFactory.createAudioSource(new MediaConstraints()));
    }

    private void initializeSignaling() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        roomRef = database.getReference("signaling/rooms/" + roomID);

        // Check for existing offer
        roomRef.child(deviceID1).child("offer").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    createOffer();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(CallActivity.this, "Failed to check offer", Toast.LENGTH_SHORT).show();
            }
        });

        // Listen for answer from device2
        roomRef.child(deviceID2).child("answer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SessionDescription answer = snapshot.getValue(SessionDescription.class);
                    if (answer != null) {
                        peerConnection.setRemoteDescription(new SimpleSdpObserver(), answer);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });

        // Listen for ICE candidates from device2
        roomRef.child(deviceID2).child("candidates").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                IceCandidate iceCandidate = snapshot.getValue(IceCandidate.class);
                if (iceCandidate != null) {
                    peerConnection.addIceCandidate(iceCandidate);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(CallActivity.this, "Failed to read candidates", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createOffer() {
        MediaConstraints constraints = new MediaConstraints();
        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sdp);
                roomRef.child(deviceID1).child("offer").setValue(sdp);
            }
        }, constraints);
    }

    private void endCall() throws InterruptedException {
        if (peerConnection != null) {
            peerConnection.close();
        }
        if (videoCapturer != null) {
            videoCapturer.stopCapture();
            videoCapturer.dispose();
        }
        if (rootEglBase != null) {
            rootEglBase.release();
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            endCall();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
