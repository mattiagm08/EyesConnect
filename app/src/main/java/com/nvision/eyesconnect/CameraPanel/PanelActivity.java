package com.nvision.eyesconnect.CameraPanel;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.nvision.eyesconnect.MainCore.MainActivity;
import com.nvision.eyesconnect.R;
import com.nvision.eyesconnect.databinding.ActivityPanelBinding;

public class PanelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inizializza il binding e imposta il contenuto della vista

        com.nvision.eyesconnect.databinding.ActivityPanelBinding binding = ActivityPanelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Imposta la toolbar

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configura la navigazione di fondo e il controller di navigazione

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_panel);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Recupera i tre ID dall'Intent

        String roomID = getIntent().getStringExtra("ROOM_ID");
        String deviceID1 = getIntent().getStringExtra("DEVICE_ID_1");
        String deviceID2 = getIntent().getStringExtra("DEVICE_ID_2");

        // Verifica che gli ID siano stati passati correttamente

        if (roomID != null && deviceID1 != null && deviceID2 != null) {
            Bundle bundle = new Bundle();
            bundle.putString("ROOM_ID", roomID);
            bundle.putString("DEVICE_ID_1", deviceID1);
            bundle.putString("DEVICE_ID_2", deviceID2);

            navController.navigate(R.id.navigation_home, bundle);
        }

        // Mostra i valori degli ID (Testing and Debug)

        /*
        Toast.makeText(this, "Room ID: " + roomID, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "DeviceID 1: " + deviceID1, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "DeviceID 2: " + deviceID2, Toast.LENGTH_SHORT).show();
        */

        // Aggiungi OnClickListener per l'ImageView con ID "imageView4"

        ImageView imageView4 = findViewById(R.id.imageView4);
        imageView4.setOnClickListener(v -> {

            // Crea un Intent per avviare MainActivity

            Intent intent = new Intent(PanelActivity.this, MainActivity.class);
            startActivity(intent);

            // Termina PanelActivity dopo aver avviato MainActivity (opzionale)

            finish();
        });
    }
}
