package com.dipdev.mutelocation;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class SavedLocation {
    @PrimaryKey(autoGenerate = true)
    public int LocationID;

    @ColumnInfo
    public double Latitude;

    @ColumnInfo
    public double Longitude;

    @ColumnInfo
    public String Address;

    public SavedLocation(int LocationID, double Latitude, double Longitude, String Address) {
        this.LocationID = LocationID;
        this.Latitude = Latitude;
        this.Longitude = Longitude;
        this.Address = Address;
    }

    @Ignore
    public SavedLocation() {
    }

    public int getLocationID() {
        return LocationID;
    }

    public void setLocationID(int locationID) {
        LocationID = locationID;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }
}
