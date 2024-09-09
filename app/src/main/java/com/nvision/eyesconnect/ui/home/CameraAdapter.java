package com.nvision.eyesconnect.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.nvision.eyesconnect.R;
import java.util.List;

public class CameraAdapter extends RecyclerView.Adapter<CameraAdapter.CameraViewHolder> {

    private List<CameraItem> cameraList;

    public CameraAdapter(List<CameraItem> cameraList) {
        this.cameraList = cameraList;
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
        holder.cameraIdTextView.setText("Camera ID: " + cameraItem.getCameraId());
        holder.cameraNameEditText.setText(cameraItem.getCameraName());

        // Imposta l'azione per salvare il nome modificato
        holder.saveImageView.setOnClickListener(v -> {
            cameraItem.setCameraName(holder.cameraNameEditText.getText().toString());
            // Qui puoi gestire il salvataggio del nome della telecamera in un database o simile
        });

        // Imposta l'azione per eliminare la telecamera
        holder.deleteImageView.setOnClickListener(v -> {
            cameraList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cameraList.size());
        });

        // Imposta l'azione per avviare la connessione
        holder.playImageView.setOnClickListener(v -> {
            // Qui puoi gestire l'avvio della connessione con la telecamera
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
}
