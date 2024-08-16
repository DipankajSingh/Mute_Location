package com.dipdev.mutelocation;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Listing_Activity extends AppCompatActivity {

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

        SavedLocationAdapter adapter = new SavedLocationAdapter(savedLocations);
        recyclerView.setAdapter(adapter);
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
