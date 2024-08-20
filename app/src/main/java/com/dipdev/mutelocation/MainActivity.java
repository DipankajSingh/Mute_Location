package com.dipdev.mutelocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_CODE = 100;
    private static final long UPDATE_INTERVAL = 60 * 1000; // 1 minutes

    private Integer count = 0;
    private Integer runnableRunCount = 0;
    private TextView logTextView;
    private TextView runnableTextView;
    private Button unMute;

    private AudioManager audioManager;
    private TextView longitudeTextView;
    private TextView latitudeTextView;
    private TextView addressTextView;
    FusedLocationProviderClient fusedLocationProviderClient;
    private Handler locationUpdateHandler;
    private ToggleButton startButton;
    private AppDatabase db ;

    public interface LocationCallback {
        void onLocationResult(Location location);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Button viewSavedLocationButton=findViewById(R.id.viewListButton);
        Button buttonTest = findViewById(R.id.buttonTest);
        longitudeTextView = findViewById(R.id.longituteTextView);
        latitudeTextView = findViewById(R.id.latituteTextView);
        addressTextView = findViewById(R.id.addressTextView);
        startButton = findViewById(R.id.startButton);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        db= AppDatabase.getDB(this);



        viewSavedLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Listing_Activity.class);
                startActivity(intent);
            }
        });

        logTextView = findViewById(R.id.logTextView);
        runnableTextView = findViewById(R.id.runnableRunCountTextView);
        findViewById(R.id.unMute).setOnClickListener(v -> {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationUpdateHandler = new Handler(Looper.getMainLooper());

        startButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Toast.makeText(MainActivity.this,"is checked: "+isChecked,Toast.LENGTH_SHORT).show();
            // Check if the app has permission to modify Do Not Disturb settings
            if (!notificationManager.isNotificationPolicyAccessGranted()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(intent);
            }

            if (isChecked) {
                scheduleLocationUpdates();
            } else {
                WorkManager.getInstance(this).cancelAllWorkByTag("LocationUpdates");
            }

            if (isChecked) {
                // Retrieve saved locations from the database
                List<SavedLocation> allLocations = getLocationsFromDatabase();

                // Get the last known location
                getLastKnownLocation(new LocationCallback() {
                    @Override
                    public void onLocationResult(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            // Check if the current location matches any saved location
                            for (SavedLocation savedLocation : allLocations) {
                                if (isLocationMatch(savedLocation, latitude, longitude)) {
                                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                                    break;
                                }
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Location is null", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                // Stop location updates and set the ringer mode to normal when switch is turned off
                locationUpdateHandler.removeCallbacksAndMessages(null);
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
        });

        buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLastKnownLocation(new LocationCallback() {
                    @Override
                    public void onLocationResult(Location location) {
                        if(location!=null){
                            db.SavedLocationDao().insert(location.getLatitude(),location.getLongitude(),addressTextView.getText().toString());
                            updateUIWithLocation(location);
                        }
                    }
                });
            }
        });
        startLocationUpdates();
    }

    private void scheduleLocationUpdates() {
        // Create a periodic work request to execute the task every 15 minutes
        PeriodicWorkRequest locationUpdateRequest =
                new PeriodicWorkRequest.Builder(LocationUpdateWorker.class, 15, TimeUnit.MINUTES)
                        .addTag("LocationUpdates")
                        .build();

        // Enqueue the work request
        WorkManager.getInstance(this).enqueue(locationUpdateRequest);
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


    private void startLocationUpdates() {
        locationUpdateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getLastKnownLocation(location -> updateUIWithLocation(location));

                if (startButton.isChecked()) {
                    // Retrieve saved locations from the database
                    List<SavedLocation> allLocations = getLocationsFromDatabase();

                    // Get the last known location
                    getLastKnownLocation(new LocationCallback() {
                        @Override
                        public void onLocationResult(Location location) {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                // Check if the current location matches any saved location
                                for (SavedLocation savedLocation : allLocations) {
                                    if (isLocationMatch(savedLocation, latitude, longitude)) {
                                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                                        break;
                                    }
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "Location is null", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                runnableRunCount++;
                runnableTextView.setText("Runnable Run Count: " + runnableRunCount);
                locationUpdateHandler.postDelayed(this, UPDATE_INTERVAL);
            }
        }, UPDATE_INTERVAL);
    }

    private void getLastKnownLocation(LocationCallback callback) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        public void onSuccess(Location location) {
                            // Handle the case where location is null
                            callback.onLocationResult(location);
                        }
                    });
        } else {
            askPermission();
        }
    }


    @SuppressLint("SetTextI18n")
    private void updateUIWithLocation(Location location) {
        try {
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            longitudeTextView.setText("Longitude: " + location.getLongitude());
            latitudeTextView.setText("Latitude: " + location.getLatitude());
            assert addresses != null;
            addressTextView.setText("Address: " + addresses.get(0).getAddressLine(0));
            count++;
           logTextView.setText("Location Updates: " + count);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Something went wrong, "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private List<SavedLocation> getLocationsFromDatabase() {
        return db.SavedLocationDao().getAll();
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {
        if (requestCode == REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLastKnownLocation(this::updateUIWithLocation);
            }else {
                Toast.makeText(MainActivity.this,"Please provide the required permission",Toast.LENGTH_SHORT).show();
            }
        }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (startButton.isChecked()){
            startLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationUpdateHandler.removeCallbacksAndMessages(null);
    }
}
