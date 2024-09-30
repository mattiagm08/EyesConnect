package com.nvision.eyesconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nvision.eyesconnect.databinding.ActivityPanelBinding;

public class PanelActivity extends AppCompatActivity {

    private ActivityPanelBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPanelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_panel);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Recupera i tre ID dall'Intent
        String roomId = getIntent().getStringExtra("ROOM_ID");
        String deviceID1 = getIntent().getStringExtra("DEVICE_ID_1");
        String deviceID2 = getIntent().getStringExtra("DEVICE_ID_2");

        if (roomId != null && deviceID1 != null && deviceID2 != null) {
            // Passa i tre ID al HomeFragment
            Bundle bundle = new Bundle();
            bundle.putString("ROOM_ID", roomId);
            bundle.putString("DEVICE_ID_1", deviceID1);
            bundle.putString("DEVICE_ID_2", deviceID2);
            navController.navigate(R.id.navigation_home, bundle);
        }

        // Aggiungi OnClickListener per l'ImageView
        ImageView imageView4 = findViewById(R.id.imageView4);
        imageView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PanelActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Opzionale: chiude PanelActivity
            }
        });
    }
}
