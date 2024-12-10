package com.nvision.eyesconnect.MainCore;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.nvision.eyesconnect.CameraPanel.PanelActivity;
import com.nvision.eyesconnect.CameraPanel.home.HomeFragment;
import com.nvision.eyesconnect.R;
import com.nvision.eyesconnect.ScannerQRCode.ScanActivity;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView; // Definizione delle immagini
    private String deviceID1; // Il deviceID per questo dispositivo
    private List<String> myIDRooms; // Lista delle stanze generate dal dispositivo

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

        // Inizializza la lista per le stanze generate

        myIDRooms = new ArrayList<>();

        // Inizializza le viste per i vari elementi nel layout

        imageView = findViewById(R.id.imageView); // Visualizzatore QR Code
        Button buttonQRCode = findViewById(R.id.buttonQRCode); // Bottone per generare QR code
        Button buttonScanner = findViewById(R.id.buttonScanner); // Bottone per aprire lo scanner
        ImageView imageView2 = findViewById(R.id.imageView2); // Icona per navigare al pannello
        ImageView imageView3 = findViewById(R.id.imageView3); // Televisore QR Code

        // Listener per il bottone che genera un QR code

        buttonQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Crea un riferimento al database Firebase

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference roomRef = database.getReference("signaling/rooms").push();

                // Ottieni un ID unico per la stanza

                String roomID = roomRef.getKey();
                if (roomID == null) {
                    Toast.makeText(MainActivity.this, "Error generating room ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Aggiungi il roomID alla lista

                myIDRooms.add(roomID);

                // Inizializza i nodi per device1 e device2
                roomRef.child("device1").setValue(new DeviceData());
                roomRef.child("device2").setValue(new DeviceData());

                try {
                    // Crea il contenuto del QR code con roomID e deviceID

                    String qrCodeData = roomID + "," + deviceID1;

                    // Genera il QR code con i dati concatenati

                    generateQRCode(qrCodeData);

                    // Nascondi monitor dopo aver generato il QR Code

                    imageView3.setVisibility(View.GONE);

                } catch (WriterException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Error generating QR code", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Listener per il bottone che apre l'attività di scansione QR code

        buttonScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);

                // Avvia l'attività di scansione

                startActivity(intent);
            }
        });

        // Listener per navigare al pannello

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PanelActivity.class);

                // Avvia l'attività del pannello

                startActivity(intent);
            }
        });

        // Passa la lista myIDRooms all'HomeFragment

        Bundle bundle = new Bundle();
        bundle.putStringArrayList("roomIDs", (ArrayList<String>) myIDRooms);
        HomeFragment homeFragment = new HomeFragment();
        homeFragment.setArguments(bundle);
    }

    // Metodo per generare il QR code

    private void generateQRCode(String text) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        // Genera una matrice per il QR code con dimensioni 200x200
        BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 200, 200);

        // Crea un bitmap che rappresenta il QR code
        Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565);
        for (int x = 0; x < 200; x++) {
            for (int y = 0; y < 200; y++) {
                // Imposta i pixel in base alla matrice del QR code (nero o bianco)
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);
            }
        }

        // Mostra il QR code nell'ImageView

        imageView.setImageBitmap(bitmap);
        imageView.setVisibility(View.VISIBLE); // Rende visibile il QR code generato
    }
}
