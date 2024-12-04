package com.nvision.eyesconnect.CameraPanel.home;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nvision.eyesconnect.R;

import org.webrtc.AudioTrack;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

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
        endCallButton.setOnClickListener(v -> endCall());
    }

    private void setupConnection() {
        // Initialize PeerConnectionFactory
        PeerConnectionFactory.InitializationOptions options =
                PeerConnectionFactory.InitializationOptions.builder(this)
                        .setEnableInternalTracer(true)
                        .createInitializationOptions();
        PeerConnectionFactory.initialize(options);

        // Create PeerConnectionFactory instance
        peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory();

        // Create EGL context for rendering
        rootEglBase = EglBase.create();
        localVideoView.init(rootEglBase.getEglBaseContext(), null);
        remoteVideoView.init(rootEglBase.getEglBaseContext(), null);

        // Configure local video and audio tracks
        VideoTrack localVideoTrack = createLocalVideoTrack();
        if (localVideoTrack != null) {
            localVideoTrack.addSink(localVideoView);
        }
        AudioTrack localAudioTrack = createLocalAudioTrack();

        // Initialize Firebase signaling
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        roomRef = database.getReference("signaling/rooms/" + roomID);

        // Create PeerConnection
        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());

        peerConnection = peerConnectionFactory.createPeerConnection(
                iceServers,
                new PeerConnection.Observer() {
                    @Override
                    public void onIceCandidate(IceCandidate candidate) {
                        roomRef.child(deviceID1).child("candidates").push().setValue(candidate);
                    }

                    @Override
                    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

                    }

                    @Override
                    public void onAddStream(MediaStream stream) {
                        if (!stream.videoTracks.isEmpty()) {
                            stream.videoTracks.get(0).addSink(remoteVideoView);
                        }
                    }

                    @Override
                    public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
                        runOnUiThread(() -> {
                            if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
                                Toast.makeText(CallActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    // Other methods are left empty for simplicity
                    @Override public void onSignalingChange(PeerConnection.SignalingState signalingState) {}
                    @Override public void onIceConnectionChange(PeerConnection.IceConnectionState state) {}
                    @Override public void onIceConnectionReceivingChange(boolean b) {}
                    @Override public void onIceGatheringChange(PeerConnection.IceGatheringState state) {}
                    @Override public void onRemoveStream(MediaStream stream) {}
                    @Override public void onDataChannel(DataChannel dataChannel) {}
                    @Override public void onRenegotiationNeeded() {}

                    @Override
                    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

                    }
                });

        startSignaling();
    }

    private VideoTrack createLocalVideoTrack() {
        Camera2Enumerator camera2Enumerator = new Camera2Enumerator(this);
        String[] deviceNames = camera2Enumerator.getDeviceNames();
        CameraVideoCapturer videoCapturer = null;

        // Select front-facing camera
        for (String deviceName : deviceNames) {
            if (camera2Enumerator.isFrontFacing(deviceName)) {
                videoCapturer = camera2Enumerator.createCapturer(deviceName, null);
                break;
            }
        }

        if (videoCapturer == null) {
            Toast.makeText(this, "No camera found", Toast.LENGTH_SHORT).show();
            return null;
        }

        // Create video source and track
        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(
                SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext()),
                this,
                videoSource.getCapturerObserver()
        );
        videoCapturer.startCapture(1280, 720, 30);

        return peerConnectionFactory.createVideoTrack("ARDAMSv0", videoSource);
    }

    private AudioTrack createLocalAudioTrack() {
        return peerConnectionFactory.createAudioTrack("ARDAMSa0", peerConnectionFactory.createAudioSource(null));
    }

    private void startSignaling() {
        roomRef.child(deviceID1).child("offer").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Create offer
                    MediaConstraints constraints = new MediaConstraints();
                    peerConnection.createOffer(new SimpleSdpObserver() {
                        @Override
                        public void onCreateSuccess(SessionDescription sdp) {
                            peerConnection.setLocalDescription(new SimpleSdpObserver(), sdp);
                            roomRef.child(deviceID1).child("offer").setValue(sdp);
                        }
                    }, constraints);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(CallActivity.this, "Failed to retrieve offer", Toast.LENGTH_SHORT).show();
            }
        });

        // Listen for answer and candidates
        roomRef.child(deviceID2).child("answer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SessionDescription answer = snapshot.getValue(SessionDescription.class);
                    peerConnection.setRemoteDescription(new SimpleSdpObserver(), answer);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });

        roomRef.child(deviceID2).child("candidates").addChildEventListener(new CandidateEventListener(peerConnection));
    }

    private void endCall() {
        if (peerConnection != null) {
            peerConnection.close();
        }
        finish();
    }

    // SimpleSdpObserver class to handle SDP events
    private static class SimpleSdpObserver implements org.webrtc.SdpObserver {
        @Override
        public void onCreateSuccess(SessionDescription sdp) {}
        @Override
        public void onSetSuccess() {}
        @Override
        public void onCreateFailure(String error) {}
        @Override
        public void onSetFailure(String error) {}
    }

    // CandidateEventListener class to handle ICE candidates
    private static class CandidateEventListener implements com.google.firebase.database.ChildEventListener {
        private final PeerConnection peerConnection;

        public CandidateEventListener(PeerConnection peerConnection) {
            this.peerConnection = peerConnection;
        }

        @Override
        public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
            IceCandidate candidate = snapshot.getValue(IceCandidate.class);
            if (candidate != null) {
                peerConnection.addIceCandidate(candidate);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot snapshot, String previousChildName) {}
        @Override
        public void onChildRemoved(DataSnapshot snapshot) {}
        @Override
        public void onChildMoved(DataSnapshot snapshot, String previousChildName) {}
        @Override
        public void onCancelled(DatabaseError error) {}
    }
}
