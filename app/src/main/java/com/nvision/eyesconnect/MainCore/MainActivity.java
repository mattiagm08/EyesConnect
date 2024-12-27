package com.nvision.eyesconnect.MainCore;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.nvision.eyesconnect.CameraPanel.PanelActivity;
import com.nvision.eyesconnect.CameraPanel.home.HomeFragment;
import com.nvision.eyesconnect.R;
import com.nvision.eyesconnect.ScannerQRCode.ScanActivity;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView; // Visualizzatore QR Code
    private String deviceID1; // Il deviceID per questo dispositivo
    private String myIDRoom; // Stanza del dispositivo madre
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "EyesConnectPrefs";
    private static final String ROOM_ID_KEY = "roomID";

    @SuppressLint({"SourceLockedOrientationActivity", "HardwareIds"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Imposta l'orientamento dello schermo a verticale
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Attiva l'interfaccia a schermo intero (edge-to-edge)
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Gestisce il padding delle viste per evitare sovrapposizione con le barre di sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ottenere l'Android ID per generare un device ID (1) univoco
        deviceID1 = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Inizializza le viste per i vari elementi nel layout
        imageView = findViewById(R.id.imageView);
        Button buttonQRCode = findViewById(R.id.buttonQRCode);
        Button buttonScanner = findViewById(R.id.buttonScanner);
        ImageView imageView2 = findViewById(R.id.imageView2);
        ImageView imageView3 = findViewById(R.id.imageView3);

        // Inizializza SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        myIDRoom = sharedPreferences.getString(ROOM_ID_KEY, null);

        // Listener per il bottone che genera un QR code
        buttonQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myIDRoom != null) {
                    // Controlla se la stanza esiste su Firebase
                    checkRoomExists(myIDRoom, exists -> {
                        if (exists) {
                            // Mostra il QR code esistente
                            String qrCodeData = myIDRoom + "," + deviceID1;
                            try {
                                generateQRCode(qrCodeData);
                                imageView3.setVisibility(View.GONE);
                            } catch (WriterException e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, "Error generating QR code", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Crea una nuova stanza se non esiste
                            createNewRoom();
                        }
                    });
                } else {
                    // Crea una nuova stanza se myIDRoom è nullo
                    createNewRoom();
                }
            }
        });

        // Listener per il bottone che apre l'attività di scansione QR code
        buttonScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                startActivity(intent);
            }
        });

        // Listener per navigare al pannello
        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PanelActivity.class);
                startActivity(intent);
            }
        });
    }

    private void createNewRoom() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference roomRef = database.getReference("signaling/rooms").push();

        String roomID = roomRef.getKey();
        if (roomID == null) {
            Toast.makeText(MainActivity.this, "Error generating room ID", Toast.LENGTH_SHORT).show();
            return;
        }

        myIDRoom = roomID;
        sharedPreferences.edit().putString(ROOM_ID_KEY, myIDRoom).apply();

        // Inizializza i nodi per device1 e device2
        roomRef.child("device1").setValue(new DeviceData());
        roomRef.child("device2").setValue(new DeviceData());

        try {
            ImageView imageView3 = findViewById(R.id.imageView3);

            String qrCodeData = roomID + "," + deviceID1;
            generateQRCode(qrCodeData);
            imageView3.setVisibility(View.GONE);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Error generating QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkRoomExists(String roomID, RoomExistsCallback callback) {
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("signaling/rooms").child(roomID);
        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                callback.onResult(snapshot.exists());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error checking room existence", Toast.LENGTH_SHORT).show();
                callback.onResult(false);
            }
        });
    }

    private void generateQRCode(String text) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 200, 200);

        Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565);
        for (int x = 0; x < 200; x++) {
            for (int y = 0; y < 200; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);
            }
        }

        imageView.setImageBitmap(bitmap);
        imageView.setVisibility(View.VISIBLE);
    }

    private interface RoomExistsCallback {
        void onResult(boolean exists);
    }
}
