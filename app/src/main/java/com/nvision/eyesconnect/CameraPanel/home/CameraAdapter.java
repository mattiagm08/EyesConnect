package com.nvision.eyesconnect.CameraPanel.home;

import android.annotation.SuppressLint;
import android.content.Context;
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

import com.nvision.eyesconnect.R;

import java.util.List;

public class CameraAdapter extends RecyclerView.Adapter<CameraAdapter.CameraViewHolder> {

    private final List<CameraItem> cameraList;

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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CameraViewHolder holder, int position) {
        CameraItem camera = cameraList.get(position);

        holder.cameraName.setText(camera.getCameraName());
        holder.roomID.setText("Room ID: " + camera.getRoomID());
        holder.deviceIDs.setText("Device 1: " + camera.getDeviceID1() + "\nDevice 2: " + camera.getDeviceID2());

        holder.cameraName.setOnClickListener(v -> {
            showEditNameDialog(holder.itemView.getContext(), camera, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return cameraList.size();
    }

    private void showEditNameDialog(Context context, CameraItem camera, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Insert Camera's Name:");

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(camera.getCameraName());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                camera.setCameraName(newName);
                notifyItemChanged(position);
                Toast.makeText(context, "Name updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    public static class CameraViewHolder extends RecyclerView.ViewHolder {
        TextView cameraName, roomID, deviceIDs;
        ImageView deleteIcon, playIcon;

        public CameraViewHolder(@NonNull View itemView) {
            super(itemView);
            cameraName = itemView.findViewById(R.id.camera_name);
            roomID = itemView.findViewById(R.id.room_id);
            deviceIDs = itemView.findViewById(R.id.device_ids);
        }
    }
}
