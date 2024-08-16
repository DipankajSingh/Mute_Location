package com.dipdev.mutelocation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SavedLocationAdapter extends RecyclerView.Adapter<SavedLocationAdapter.ViewHolder> {

    private List<SavedLocation> savedLocations;

    public SavedLocationAdapter(List<SavedLocation> savedLocations) {
        this.savedLocations = savedLocations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavedLocation savedLocation = savedLocations.get(position);
        holder.latitudeTextView.setText("Latitude: " + savedLocation.getLatitude());
        holder.longitudeTextView.setText("Longitude: " + savedLocation.getLongitude());
        holder.addressTextView.setText("Address: " + savedLocation.getAddress());
    }

    @Override
    public int getItemCount() {
        return savedLocations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView latitudeTextView;
        public TextView longitudeTextView;
        public TextView addressTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            latitudeTextView = itemView.findViewById(R.id.latitudeTextView);
            longitudeTextView = itemView.findViewById(R.id.longitudeTextView);
            addressTextView = itemView.findViewById(R.id.addressTextView);
        }
    }
}
