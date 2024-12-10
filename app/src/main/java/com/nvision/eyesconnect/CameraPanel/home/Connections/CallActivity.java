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

    // Variabili per WebRTC

    private EglBase rootEglBase;
    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection peerConnection;
    private SurfaceViewRenderer localVideoView;
    private SurfaceViewRenderer remoteVideoView;

    // Variabili per gestire i dettagli della stanza e dei dispositivi

    private String roomID;
    private String deviceID1;
    private String deviceID2;

    // Riferimento alla stanza nel database Firebase

    private DatabaseReference roomRef;
    private CameraVideoCapturer videoCapturer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        // Recupera i dati passati tramite Intent

        roomID = getIntent().getStringExtra("ROOM_ID");
        deviceID1 = getIntent().getStringExtra("DEVICE_ID_1");
        deviceID2 = getIntent().getStringExtra("DEVICE_ID_2");

        // Inizializza le view per i video

        localVideoView = findViewById(R.id.local_video_view);
        remoteVideoView = findViewById(R.id.remote_video_view);
        Button endCallButton = findViewById(R.id.end_call_button);

        // Inizializza la connessione WebRTC

        setupConnection();

        // Aggiungi listener per il pulsante di fine chiamata

        endCallButton.setOnClickListener(v -> {
            try {
                endCall();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setupConnection() {

        // Inizializza PeerConnectionFactory

        PeerConnectionFactory.InitializationOptions options =
                PeerConnectionFactory.InitializationOptions.builder(this)
                        .setEnableInternalTracer(true)
                        .createInitializationOptions();
        PeerConnectionFactory.initialize(options);

        peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory();

        // Crea il contesto EGL per il rendering video

        rootEglBase = EglBase.create();
        localVideoView.init(rootEglBase.getEglBaseContext(), null);
        remoteVideoView.init(rootEglBase.getEglBaseContext(), null);

        // Configura la visualizzazione video locale e remota

        localVideoView.setMirror(true);
        remoteVideoView.setMirror(false);

        // Crea e aggiungi il track video locale

        VideoTrack localVideoTrack = createLocalVideoTrack();
        if (localVideoTrack != null) {
            localVideoTrack.addSink(localVideoView);
        }
        AudioTrack localAudioTrack = createLocalAudioTrack();

        // Configura i server ICE per la connessione WebRTC

        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());

        // Crea la connessione peer

        peerConnection = peerConnectionFactory.createPeerConnection(iceServers, new PeerConnectionObserver());

        // Aggiungi il flusso video e audio alla connessione

        if (localVideoTrack != null) {
            MediaStream mediaStream = peerConnectionFactory.createLocalMediaStream("ARDAMS");
            mediaStream.addTrack(localVideoTrack);
            mediaStream.addTrack(localAudioTrack);
            peerConnection.addStream(mediaStream);
        }

        // Inizializza la segnalazione per la comunicazione tramite Firebase

        initializeSignaling();
    }

    private VideoTrack createLocalVideoTrack() {

        // Crea un enumeratore per le telecamere disponibili

        Camera2Enumerator camera2Enumerator = new Camera2Enumerator(this);
        String[] deviceNames = camera2Enumerator.getDeviceNames();
        videoCapturer = null;

        // Cerca una telecamera frontale

        for (String deviceName : deviceNames) {
            if (camera2Enumerator.isFrontFacing(deviceName)) {
                videoCapturer = camera2Enumerator.createCapturer(deviceName, null);
                break;
            }
        }

        // Verifica che sia stata trovata una telecamera frontale

        if (videoCapturer == null) {
            Toast.makeText(this, "No front camera found", Toast.LENGTH_SHORT).show();
            return null;
        }

        // Crea una sorgente video e inizializza il capturer

        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext()),
                this, videoSource.getCapturerObserver());
        videoCapturer.startCapture(1280, 720, 30);

        // Crea un track video e restituiscilo

        return peerConnectionFactory.createVideoTrack("ARDAMSv0", videoSource);
    }

    private AudioTrack createLocalAudioTrack() {

        // Crea e restituisce il track audio

        return peerConnectionFactory.createAudioTrack("ARDAMSa0", peerConnectionFactory.createAudioSource(new MediaConstraints()));
    }

    private void initializeSignaling() {

        // Inizializza il riferimento al nodo della stanza su Firebase

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        roomRef = database.getReference("signaling/rooms/" + roomID);

        // Verifica se esiste già un'offerta per device2

        roomRef.child("device2").child("offer").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {

                    // Se l'offerta esiste già, aggiorna il valore

                    updateOffer();
                } else {

                    // Altrimenti, crea una nuova offerta

                    createOffer();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(CallActivity.this, "Failed to check offer", Toast.LENGTH_SHORT).show();
            }
        });

        // Ascolta la risposta di device2

        roomRef.child("device2").child("answer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    // Se viene ricevuta una risposta, imposta la descrizione remota

                    SessionDescription answer = snapshot.getValue(SessionDescription.class);
                    if (answer != null) {
                        peerConnection.setRemoteDescription(new SimpleSdpObserver(), answer);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });

        // Ascolta i candidati ICE da device2

        roomRef.child("device2").child("candidates").addChildEventListener(new ChildEventListener() {
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

        // Crea un'offerta per la connessione WebRTC

        MediaConstraints constraints = new MediaConstraints();
        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sdp);

                // Scrivi l'offerta sotto device2 nel nodo della stanza

                roomRef.child("device2").child("offer").setValue(sdp);
            }
        }, constraints);
    }

    private void updateOffer() {

        // Modifica l'offerta sotto device1, se esiste già

        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sdp);
                roomRef.child("device2").child("offer").setValue(sdp); // Aggiorna il nodo con la nuova offerta
            }
        }, new MediaConstraints());
    }

    private void endCall() throws InterruptedException {

        // Termina la chiamata chiudendo la connessione WebRTC e liberando le risorse

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

            // Termina la chiamata quando l'attività viene distrutta

            endCall();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
