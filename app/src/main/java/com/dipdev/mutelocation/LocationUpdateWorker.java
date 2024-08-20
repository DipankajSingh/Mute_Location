package com.dipdev.mutelocation;


import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.util.Log;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.List;

public class LocationUpdateWorker extends Worker {

    private final AudioManager audioManager;

    public LocationUpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Implement your location update logic here
        updateLocation();

        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }

    private void updateLocation() {
        Log.d("LocationUpdateWorker", "Attempting to update location...");

        // Retrieve saved locations from the database
        AppDatabase db = AppDatabase.getDB(getApplicationContext());
        List<SavedLocation> savedLocations = db.SavedLocationDao().getAll();

        // Get current location
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            // Check if the current location matches any saved location
                            for (SavedLocation savedLocation : savedLocations) {
                                if (isLocationMatch(savedLocation, latitude, longitude)) {
                                    // Mute the phone if a matching location is found
                                    if (audioManager != null) {
                                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                                        Log.d("LocationUpdateWorker", "Ringer mode set to silent based on location match.");
                                    } else {
                                        Log.e("LocationUpdateWorker", "AudioManager is null");
                                    }
                                    return; // Exit after muting the phone
                                }
                            }

                            // If no matching location found, log and do not change ringer mode
                            Log.d("LocationUpdateWorker", "No matching location found. Ringer mode unchanged.");
                        } else {
                            Log.e("LocationUpdateWorker", "Location is null.");
                        }
                    });
        } else {
            Log.e("LocationUpdateWorker", "Location permission not granted.");
        }
    }

    private boolean isLocationMatch(SavedLocation savedLocation, double latitude, double longitude) {
        // Create a Location object for the saved location
        Location savedLoc = new Location("savedLocation");
        savedLoc.setLatitude(savedLocation.getLatitude());
        savedLoc.setLongitude(savedLocation.getLongitude());

        // Create a Location object for the current location
        Location currentLoc = new Location("currentLocation");
        currentLoc.setLatitude(latitude);
        currentLoc.setLongitude(longitude);

        // Calculate the distance between the two locations in meters
        float distanceInMeters = savedLoc.distanceTo(currentLoc);

        // Return true if the distance is within 50 meters, false otherwise
        return distanceInMeters <= 50;
    }


}