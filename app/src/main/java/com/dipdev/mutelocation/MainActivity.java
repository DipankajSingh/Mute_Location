package com.dipdev.mutelocation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

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
            if (!notificationManager.isNotificationPolicyAccessGranted()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(intent);
            }
            if (isChecked) {
                startLocationUpdates();
            }
            if (!isChecked) {
                locationUpdateHandler.removeCallbacksAndMessages(null);
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
        });
        buttonTest.setOnClickListener(v -> getLastKnownLocation());
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        locationUpdateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getLastKnownLocation();
                runnableRunCount++;
                runnableTextView.setText("Runnable Run Count: " + runnableRunCount);
                locationUpdateHandler.postDelayed(this, UPDATE_INTERVAL);
            }
        }, UPDATE_INTERVAL);
    }

    private void getLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @SuppressLint("SetTextI18n")
                        public void onSuccess(Location location) {
                            if (location != null) {
                                updateUIWithLocation(location);
                            }
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
            longitudeTextView.setText("Longitude: " + String.valueOf(location.getLongitude()));
            latitudeTextView.setText("Latitude: " + String.valueOf(location.getLatitude()));
            assert addresses != null;
            addressTextView.setText("Address: " + addresses.get(0).getAddressLine(0));
            count++;

            saveLocation(location);
            updateRingerMode();
            getLocationsFromDatabase();
           logTextView.setText("Location Updates: " + count);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Something went wrong, "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void getLocationsFromDatabase() {
       List<SavedLocation> addresses = db.SavedLocationDao().getAll();
       Log.d("addresses", addresses.toString());
    }

    private void saveLocation(Location location) {
        db.SavedLocationDao().insert(location.getLatitude(), location.getLongitude(), addressTextView.getText().toString());
    }

    private void updateRingerMode() {

        logTextView.setText("Location Updates: " + count+" "+"Ringer Mode:");
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }


    private void askPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {
        if (requestCode == REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLastKnownLocation();
            }else {
                Toast.makeText(MainActivity.this,"Please provide the required permission",Toast.LENGTH_SHORT).show();
            }
        }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationUpdateHandler.removeCallbacksAndMessages(null);
    }
}
