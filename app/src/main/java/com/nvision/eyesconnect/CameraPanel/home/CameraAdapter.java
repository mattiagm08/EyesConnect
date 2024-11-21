package com.nvision.eyesconnect.CameraPanel.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

    @Override
    public void onBindViewHolder(@NonNull CameraViewHolder holder, int position) {
        CameraItem camera = cameraList.get(position);
        holder.cameraName.setText(camera.cameraName);
        holder.roomID.setText("Room ID: " + camera.roomID);
        holder.deviceIDs.setText("Device 1: " + camera.deviceID1 + "\nDevice 2: " + camera.deviceID2);
    }

    @Override
    public int getItemCount() {
        return cameraList.size();
    }

    public static class CameraViewHolder extends RecyclerView.ViewHolder {
        TextView cameraName, roomID, deviceIDs;

        public CameraViewHolder(@NonNull View itemView) {
            super(itemView);
            cameraName = itemView.findViewById(R.id.camera_name);
            roomID = itemView.findViewById(R.id.room_id);
            deviceIDs = itemView.findViewById(R.id.device_ids);
        }
    }
}
