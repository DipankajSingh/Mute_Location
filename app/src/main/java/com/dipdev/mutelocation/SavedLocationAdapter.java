package com.dipdev.mutelocation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SavedLocationAdapter extends RecyclerView.Adapter<SavedLocationAdapter.ViewHolder> {

    private List<SavedLocation> savedLocations;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public SavedLocationAdapter(List<SavedLocation> savedLocations) {
        this.savedLocations = savedLocations;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView latitudeTextView, longitudeTextView, addressTextView;
        public Button deleteButton;

        public ViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            latitudeTextView = itemView.findViewById(R.id.latitudeTextView);
            longitudeTextView = itemView.findViewById(R.id.longitudeTextView);
            addressTextView = itemView.findViewById(R.id.addressTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_location, parent, false);
        return new ViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SavedLocation savedLocation = savedLocations.get(position);
        holder.latitudeTextView.setText("Latitude: " + savedLocation.getLatitude());
        holder.longitudeTextView.setText("Longitude: " + savedLocation.getLongitude());
        holder.addressTextView.setText(savedLocation.getAddress());
    }

    @Override
    public int getItemCount() {
        return savedLocations.size();
    }
}
