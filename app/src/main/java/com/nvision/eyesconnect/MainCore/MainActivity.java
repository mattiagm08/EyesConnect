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
import com.nvision.eyesconnect.R;
import com.nvision.eyesconnect.ScannerQRCode.ScanActivity;

/** @noinspection ALL*/
public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private String deviceID1; // Il deviceID per questo dispositivo

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

        // Ottenere l'Android ID per generare un deviceID univoco
        deviceID1 = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

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

                // Crea un riferimento al database Firebase per creare una nuova stanza (room)
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference roomRef = database.getReference("rooms").push();

                String roomID = roomRef.getKey(); // Ottieni un identificatore univoco per la stanza

                try {
                    // Crea il contenuto del QR code con roomID e deviceID
                    String qrCodeData = roomID + "," + deviceID1;
                    generateQRCode(qrCodeData); // Genera il QR code con i dati concatenati

                    // Rende invisibile l'imageView3 una volta generato il QR code
                    imageView3.setVisibility(View.GONE);

                } catch (WriterException e) {
                    e.printStackTrace(); // Gestisce eventuali errori nella generazione del QR code
                }
            }
        });

        // Listener per il bottone che apre l'attività di scansione QR code
        buttonScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                startActivity(intent); // Avvia l'attività di scansione
            }
        });

        // Listener per navigare al pannello
        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PanelActivity.class);
                startActivity(intent); // Avvia l'attività del pannello
            }
        });
    }

    // Metodo per generare un QR code
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
