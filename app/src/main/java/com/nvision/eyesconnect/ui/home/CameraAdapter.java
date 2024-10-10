package com.nvision.eyesconnect.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nvision.eyesconnect.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CameraAdapter extends RecyclerView.Adapter<CameraAdapter.CameraViewHolder> {

    private final List<CameraItem> cameraList; // Lista di oggetti CameraItem
    private final CameraItemListener itemListener; // Interfaccia per gestire gli eventi di aggiornamento e cancellazione
    private final Context context; // Contesto dell'applicazione

    // Costruttore per inizializzare il CameraAdapter
    public CameraAdapter(Context context, List<CameraItem> cameraList, CameraItemListener itemListener) {
        this.context = context;
        this.cameraList = cameraList;
        this.itemListener = itemListener;
    }

    @NonNull
    @Override
    public CameraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflaziona il layout per ciascun item della lista
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_camera, parent, false);
        return new CameraViewHolder(view); // Restituisce un nuovo ViewHolder
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CameraViewHolder holder, int position) {
        // Ottieni l'oggetto CameraItem corrente
        CameraItem cameraItem = cameraList.get(position);
        holder.cameraIdTextView.setText("ID: " + cameraItem.getCameraId()); // Mostra l'ID della telecamera

        // Visualizza il nome della telecamera
        holder.cameraNameEditText.setText(cameraItem.getCameraName());

        // Listener per il pulsante di salvataggio
        holder.saveImageView.setOnClickListener(v -> {
            String updatedText = holder.cameraNameEditText.getText().toString();
            // Rimuovi il prefisso prima di salvare
            String updatedName = updatedText.replace("Camera's Name: ", "").trim();
            cameraItem.setCameraName(updatedName); // Aggiorna il nome della telecamera
            itemListener.onCameraItemUpdated(cameraItem); // Notifica l'aggiornamento
            saveCameraItemToPreferences(cameraItem); // Salva l'item nelle preferenze
        });

        // Listener per il pulsante di eliminazione
        holder.deleteImageView.setOnClickListener(v -> {
            // Rimuovi l'ID dalle preferenze
            removeCameraIdFromPreferences(cameraItem.getCameraId());

            // Rimuovi l'item dalla lista e aggiorna l'interfaccia utente
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                cameraList.remove(currentPosition); // Rimuovi l'elemento dalla lista
                notifyItemRemoved(currentPosition); // Notifica la rimozione dell'elemento
                notifyItemRangeChanged(currentPosition, cameraList.size()); // Aggiorna la posizione degli elementi
                itemListener.onCameraItemDeleted(cameraItem); // Notifica la cancellazione
            }
        });

        // Listener per il pulsante di avvio della trasmissione
        holder.playImageView.setOnClickListener(v -> {
            // Avvia CallActivity passando l'ID della telecamera e gli altri ID
            Intent intent = new Intent(context, CallActivity.class);
            intent.putExtra("cameraId", cameraItem.getCameraId());
            intent.putExtra("roomId", cameraItem.getRoomId()); // Passa il roomId
            intent.putExtra("deviceId1", cameraItem.getDeviceId1()); // Passa il deviceId1
            intent.putExtra("deviceId2", cameraItem.getDeviceId2()); // Passa il deviceId2
            context.startActivity(intent); // Avvia l'attività
        });
    }

    @Override
    public int getItemCount() {
        return cameraList.size(); // Restituisce il numero totale di elementi nella lista
    }

    public static class CameraViewHolder extends RecyclerView.ViewHolder {
        TextView cameraIdTextView; // TextView per mostrare l'ID della telecamera
        EditText cameraNameEditText; // EditText per modificare il nome della telecamera
        ImageView deleteImageView, saveImageView, playImageView; // ImageView per i pulsanti di eliminazione, salvataggio e riproduzione

        public CameraViewHolder(@NonNull View itemView) {
            super(itemView);
            cameraIdTextView = itemView.findViewById(R.id.id_camera_text);
            cameraNameEditText = itemView.findViewById(R.id.name_camera_text);
            deleteImageView = itemView.findViewById(R.id.deleteImageView);
            saveImageView = itemView.findViewById(R.id.saveImageView);
            playImageView = itemView.findViewById(R.id.playImageView);
        }
    }

    // Metodo per rimuovere l'ID della telecamera dalle SharedPreferences
    @SuppressLint("MutatingSharedPrefs")
    private void removeCameraIdFromPreferences(String cameraId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("CameraPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Assume di memorizzare gli ID delle telecamere in un set
        Set<String> cameraIds = sharedPreferences.getStringSet("camera_ids", new HashSet<>());
        if (cameraIds != null && cameraIds.contains(cameraId)) {
            cameraIds.remove(cameraId); // Rimuovi l'ID dal set
            editor.putStringSet("camera_ids", cameraIds); // Aggiorna le SharedPreferences
            editor.apply();
        }
    }

    // Metodo per salvare un CameraItem nelle SharedPreferences
    @SuppressLint("MutatingSharedPrefs")
    private void saveCameraItemToPreferences(CameraItem cameraItem) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("CameraPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Assume di memorizzare gli ID delle telecamere in un set
        Set<String> cameraIds = sharedPreferences.getStringSet("camera_ids", new HashSet<>());
        if (cameraIds == null) {
            cameraIds = new HashSet<>();
        }
        cameraIds.add(cameraItem.getCameraId()); // Aggiungi l'ID della telecamera al set
        editor.putStringSet("camera_ids", cameraIds); // Aggiorna le SharedPreferences
        editor.putString(cameraItem.getCameraId(), cameraItem.getCameraName()); // Salva il nome associato all'ID
        editor.apply();
    }

    // Interfaccia per gestire gli eventi relativi agli oggetti CameraItem
    public interface CameraItemListener {
        void onCameraItemUpdated(CameraItem cameraItem); // Metodo per gestire l'aggiornamento di un item
        void onCameraItemDeleted(CameraItem cameraItem); // Metodo per gestire la cancellazione di un item
    }
}
