package com.dipdev.mutelocation;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Listing_Activity extends AppCompatActivity {
    private List<SavedLocation> savedLocations;
    private SavedLocationAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);


        // Enable the back button in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        // Initialize RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerViewSavedLocations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up the adapter
        AppDatabase db = AppDatabase.getDB(this);
        List<SavedLocation> savedLocations = db.SavedLocationDao().getAll();

        // Create and set the adapter
        SavedLocationAdapter adapter = new SavedLocationAdapter(savedLocations);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new SavedLocationAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                SavedLocation locationToDelete = savedLocations.get(position);

                // Delete the item from the database in a background thread
                new Thread(() -> {
                    db.SavedLocationDao().delete(locationToDelete.getLocationID());

                    // Remove the item from the list and update the UI on the main thread
                    runOnUiThread(() -> {
                        savedLocations.remove(position);
                        adapter.notifyItemRemoved(position);
                    });
                }).start();
            }
        });


    }
    // Handle the action bar back button press
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Close the activity when the back button is pressed
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
