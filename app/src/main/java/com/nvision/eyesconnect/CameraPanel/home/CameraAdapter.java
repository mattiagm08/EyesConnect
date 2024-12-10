package com.nvision.eyesconnect.CameraPanel.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.nvision.eyesconnect.CameraPanel.home.Connections.CallActivity;
import com.nvision.eyesconnect.R;
import java.util.List;

public class CameraAdapter extends RecyclerView.Adapter<CameraAdapter.CameraViewHolder> {

    // Lista delle telecamere e il listener per i cambiamenti nella lista

    private final List<CameraItem> cameraList;
    private final OnCameraListChangeListener listChangeListener;

    // Interfaccia per notificare i cambiamenti nella lista di telecamere

    public interface OnCameraListChangeListener {
        void onCameraListChanged(boolean isEmpty);
    }

    // Costruttore per inizializzare la lista di telecamere e il listener

    public CameraAdapter(List<CameraItem> cameraList, OnCameraListChangeListener listChangeListener) {
        this.cameraList = cameraList;
        this.listChangeListener = listChangeListener;
    }

    // Crea un nuovo ViewHolder per un elemento della lista

    @NonNull
    @Override
    public CameraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Inflazione del layout dell'elemento della lista

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_camera, parent, false);
        return new CameraViewHolder(view);
    }

    // Associa i dati di un elemento della lista al ViewHolder

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CameraViewHolder holder, int position) {

        // Recupera l'oggetto CameraItem alla posizione corrente

        CameraItem camera = cameraList.get(position);

        // Imposta il nome della telecamera e altre informazioni

        holder.cameraName.setText(camera.getCameraName());
        holder.roomID.setText("Room ID: " + camera.getRoomID());
        holder.deviceIDs.setText("Device 1: " + camera.getDeviceID1() + "\nDevice 2: " + camera.getDeviceID2());

        // Gestisce il clic sul nome della telecamera per modificarlo

        holder.cameraName.setOnClickListener(v -> {
            showEditNameDialog(holder.itemView.getContext(), camera, holder.getAdapterPosition());
        });

        // Gestisce il clic sull'icona di eliminazione della telecamera

        holder.deleteIcon.setOnClickListener(v -> {
            removeCamera(holder.getAdapterPosition());
        });

        // Gestisce il clic sull'icona di avvio della connessione RTC

        holder.playIcon.setOnClickListener(v -> {
            String roomID = camera.getRoomID();
            String deviceID1 = camera.getDeviceID1();
            String deviceID2 = camera.getDeviceID2();

            // Passa i dati alla CallActivity per avviare la connessione RTC

            Intent intent = new Intent(v.getContext(), CallActivity.class);
            intent.putExtra("ROOM_ID", roomID);
            intent.putExtra("DEVICE_ID_1", deviceID1);
            intent.putExtra("DEVICE_ID_2", deviceID2);
            v.getContext().startActivity(intent);
        });
    }

    // Restituisce il numero di elementi nella lista

    @Override
    public int getItemCount() {
        return cameraList.size();
    }

    // Mostra un dialogo per modificare il nome della telecamera

    private void showEditNameDialog(Context context, CameraItem camera, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Insert Camera's Name:");

        // Campo di input per inserire il nuovo nome della telecamera

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(camera.getCameraName());  // Imposta il nome attuale
        builder.setView(input);

        // Gestisce il clic sul pulsante "Salva"

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {

                // Se il nuovo nome non è vuoto, lo salva

                camera.setCameraName(newName);
                notifyItemChanged(position);  // Notifica che l'elemento è cambiato
                Toast.makeText(context, "Name updated successfully", Toast.LENGTH_SHORT).show();
            } else {

                // Altrimenti mostra un messaggio di errore

                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // Gestisce il clic sul pulsante "Annulla"

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Rimuove la telecamera dalla lista

    private void removeCamera(int position) {
        if (position >= 0 && position < cameraList.size()) {

            // Rimuove l'elemento dalla lista

            cameraList.remove(position);
            notifyItemRemoved(position);  // Notifica che l'elemento è stato rimosso
            notifyItemRangeChanged(position, cameraList.size());  // Rende aggiornati gli altri elementi

            // Notifica al listener se la lista è vuota

            if (listChangeListener != null) {
                listChangeListener.onCameraListChanged(cameraList.isEmpty());
            }
        }
    }

    // ViewHolder che gestisce gli elementi visivi per ogni elemento della lista

    public static class CameraViewHolder extends RecyclerView.ViewHolder {
        TextView cameraName, roomID, deviceIDs;
        ImageView deleteIcon, playIcon;

        public CameraViewHolder(@NonNull View itemView) {
            super(itemView);

            // Associa i componenti del layout agli oggetti

            cameraName = itemView.findViewById(R.id.camera_name);
            roomID = itemView.findViewById(R.id.room_id);
            deviceIDs = itemView.findViewById(R.id.device_ids);
            deleteIcon = itemView.findViewById(R.id.imageView5); // Icona di eliminazione
            playIcon = itemView.findViewById(R.id.imageView6);   // Icona di avvio
        }
    }
}
