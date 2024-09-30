package com.nvision.eyesconnect.ui.home;

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

    private List<CameraItem> cameraList;
    private CameraItemListener itemListener;
    private Context context;

    public CameraAdapter(Context context, List<CameraItem> cameraList, CameraItemListener itemListener) {
        this.context = context;
        this.cameraList = cameraList;
        this.itemListener = itemListener;
    }

    @NonNull
    @Override
    public CameraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_camera, parent, false);
        return new CameraViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CameraViewHolder holder, int position) {
        CameraItem cameraItem = cameraList.get(position);
        holder.cameraIdTextView.setText("ID: " + cameraItem.getCameraId());

        // Visualizza il nome con il prefisso
        holder.cameraNameEditText.setText(cameraItem.getCameraName());

        holder.saveImageView.setOnClickListener(v -> {
            String updatedText = holder.cameraNameEditText.getText().toString();
            // Rimuovi il prefisso prima di salvare
            String updatedName = updatedText.replace("Camera's Name: ", "").trim();
            cameraItem.setCameraName(updatedName);
            itemListener.onCameraItemUpdated(cameraItem);
            saveCameraItemToPreferences(cameraItem); // Salva l'item nelle preferenze
        });

        holder.deleteImageView.setOnClickListener(v -> {
            // Rimuovi l'ID dalle preferenze
            removeCameraIdFromPreferences(cameraItem.getCameraId());

            // Rimuovi l'item dalla lista e aggiorna l'interfaccia utente
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                cameraList.remove(currentPosition);
                notifyItemRemoved(currentPosition);
                notifyItemRangeChanged(currentPosition, cameraList.size());
                itemListener.onCameraItemDeleted(cameraItem);
            }
        });

        holder.playImageView.setOnClickListener(v -> {
            // Avvia CallActivity passando l'ID della telecamera e gli altri ID
            Intent intent = new Intent(context, CallActivity.class);
            intent.putExtra("cameraId", cameraItem.getCameraId());
            intent.putExtra("roomId", cameraItem.getRoomId());  // Passa il roomId
            intent.putExtra("deviceId1", cameraItem.getDeviceId1());  // Passa il deviceId1
            intent.putExtra("deviceId2", cameraItem.getDeviceId2());  // Passa il deviceId2
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cameraList.size();
    }

    public static class CameraViewHolder extends RecyclerView.ViewHolder {
        TextView cameraIdTextView;
        EditText cameraNameEditText;
        ImageView deleteImageView, saveImageView, playImageView;

        public CameraViewHolder(@NonNull View itemView) {
            super(itemView);
            cameraIdTextView = itemView.findViewById(R.id.id_camera_text);
            cameraNameEditText = itemView.findViewById(R.id.name_camera_text);
            deleteImageView = itemView.findViewById(R.id.deleteImageView);
            saveImageView = itemView.findViewById(R.id.saveImageView);
            playImageView = itemView.findViewById(R.id.playImageView);
        }
    }

    private void removeCameraIdFromPreferences(String cameraId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("CameraPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Assume you store camera IDs in a set
        Set<String> cameraIds = sharedPreferences.getStringSet("camera_ids", new HashSet<>());
        if (cameraIds != null && cameraIds.contains(cameraId)) {
            cameraIds.remove(cameraId);
            editor.putStringSet("camera_ids", cameraIds);
            editor.apply();
        }
    }

    private void saveCameraItemToPreferences(CameraItem cameraItem) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("CameraPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Assume you store camera IDs and names in a map
        Set<String> cameraIds = sharedPreferences.getStringSet("camera_ids", new HashSet<>());
        if (cameraIds == null) {
            cameraIds = new HashSet<>();
        }
        cameraIds.add(cameraItem.getCameraId());
        editor.putStringSet("camera_ids", cameraIds);
        editor.putString(cameraItem.getCameraId(), cameraItem.getCameraName()); // Salva il nome associato all'ID
        editor.apply();
    }

    public interface CameraItemListener {
        void onCameraItemUpdated(CameraItem cameraItem);
        void onCameraItemDeleted(CameraItem cameraItem);
    }
}
