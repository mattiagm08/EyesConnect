package com.nvision.eyesconnect.ScannerQRCode;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.google.zxing.ResultPoint;
import com.nvision.eyesconnect.CameraPanel.PanelActivity;
import com.nvision.eyesconnect.R;
import java.io.IOException;
import java.util.List;

public class ScanActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private CaptureManager capture;

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

        DecoratedBarcodeView barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);
        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();

        barcodeScannerView.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                handleScannedData(result.getText());
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                // Non necessario per il tuo caso d'uso
            }
        });

        // Pulsante per selezionare l'immagine dalla galleria
        Button selectFromGalleryButton = findViewById(R.id.button_select_from_gallery);
        selectFromGalleryButton.setOnClickListener(v -> openImagePicker());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                String scannedData = decodeQRCodeFromBitmap(bitmap);
                if (scannedData != null) {
                    handleScannedData(scannedData);
                } else {
                    Toast.makeText(this, "No QR code found in the image", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String decodeQRCodeFromBitmap(Bitmap bitmap) {
        int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        RGBLuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Reader reader = new QRCodeReader();
            return reader.decode(binaryBitmap).getText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void handleScannedData(String scannedData) {
        String[] parts = scannedData.split(",");
        if (parts.length >= 2) {
            String roomID = parts[0];   // roomID
            String deviceID2 = parts[1]; // deviceID2
            String deviceID1 = getAndroidDeviceID();

            Intent intent = createPanelActivityIntent(roomID, deviceID1, deviceID2);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid QR code data", Toast.LENGTH_SHORT).show();
        }
    }

    private Intent createPanelActivityIntent(String roomID, String deviceID1, String deviceID2) {
        Intent intent = new Intent(this, PanelActivity.class);
        intent.putExtra("ROOM_ID", roomID);
        intent.putExtra("DEVICE_ID_1", deviceID1);
        intent.putExtra("DEVICE_ID_2", deviceID2);
        return intent;
    }

    @SuppressLint("HardwareIds")
    private String getAndroidDeviceID() {
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
}