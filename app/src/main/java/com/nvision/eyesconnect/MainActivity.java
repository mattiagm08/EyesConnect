package com.nvision.eyesconnect;

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

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private String deviceID; // Il deviceID per questo dispositivo

    @SuppressLint({"SourceLockedOrientationActivity", "HardwareIds"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // Imposta l'orientamento a verticale
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ottenere l'Android ID per generare un deviceID univoco
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        imageView = findViewById(R.id.imageView);
        Button buttonQRCode = findViewById(R.id.buttonQRCode);
        Button buttonScanner = findViewById(R.id.buttonScanner);
        ImageView imageView2 = findViewById(R.id.imageView2);
        ImageView imageView3 = findViewById(R.id.imageView3);

        buttonQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference roomRef = database.getReference("rooms").push();
                String roomId = roomRef.getKey(); // Ottieni un identificatore univoco per la stanza

                try {
                    String qrCodeData = roomId + "," + deviceID; // Concatenazione roomID e deviceID
                    generateQRCode(qrCodeData); // Usa la stringa concatenata per generare il QR code

                    // Rende invisibile l'imageView3
                    imageView3.setVisibility(View.GONE);

                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        });

        buttonScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                startActivity(intent);
            }
        });

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PanelActivity.class);
                startActivity(intent);
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
}
