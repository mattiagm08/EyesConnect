package com.nvision.eyesconnect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.google.zxing.ResultPoint;

import java.util.List;

public class ScanActivity extends AppCompatActivity {

    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // Imposta l'orientamento a verticale
        setContentView(R.layout.activity_scan);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);
        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();

        barcodeScannerView.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                String scannedData = result.getText(); // Ottieni i dati dal QR code (roomID,deviceID2)

                // Estrai il roomID e il deviceID2 dal QR code, supponiamo siano separati da una virgola
                String[] parts = scannedData.split(",");
                String roomId = parts[0];   // roomID
                String deviceID2 = parts[1]; // deviceID2

                // Ottieni l'ID del dispositivo che scannerizza (deviceID1)
                String deviceID1 = getAndroidDeviceID();

                // Reindirizza a PanelActivity passando roomID, deviceID1 e deviceID2
                Intent intent = new Intent(ScanActivity.this, PanelActivity.class);
                intent.putExtra("ROOM_ID", roomId);
                intent.putExtra("DEVICE_ID_1", deviceID1);
                intent.putExtra("DEVICE_ID_2", deviceID2);
                startActivity(intent);
                finish(); // Termina l'activity corrente
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                // Non necessario per il tuo caso d'uso
            }
        });
    }

    private String getAndroidDeviceID() {
        // Ottieni l'ID univoco del dispositivo che scannerizza
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ScanActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
