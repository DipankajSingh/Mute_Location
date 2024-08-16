package com.dipdev.mutelocation;


import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SavedLocationDao {
    @Query("SELECT * FROM savedlocation")
    List<SavedLocation> getAll();

    @Query("INSERT INTO savedlocation (Latitude, Longitude, Address) VALUES (:latitude, :longitude, :address)")
    void insert(double latitude, double longitude, String address);

    @Query("DELETE FROM savedlocation WHERE LocationID = :locationID")
    void delete(int locationID);

    @Query("UPDATE savedlocation SET Latitude = :latitude, Longitude = :longitude, Address = :address WHERE LocationID = :locationID")
    void update(int locationID, double latitude, double longitude, String address);

    @Query("SELECT * FROM savedlocation WHERE LocationID = :locationID")
    SavedLocation get(int locationID);

    @Query("SELECT * FROM savedlocation WHERE Latitude = :latitude AND Longitude = :longitude")
    SavedLocation getByCoordinates(double latitude, double longitude);

    @Query("SELECT * FROM savedlocation WHERE Address = :address")
    SavedLocation getByAddress(String address);

    @Query("SELECT COUNT(*) FROM savedlocation")
    int getCount();

    @Query("SELECT * FROM savedlocation ORDER BY LocationID DESC LIMIT 1")
    SavedLocation getLast();

    @Query("SELECT * FROM savedlocation ORDER BY LocationID ASC LIMIT 1")
    SavedLocation getFirst();

    @Query("SELECT * FROM savedlocation ORDER BY LocationID DESC LIMIT 10")
    List<SavedLocation> getLast10();

    @Query("SELECT * FROM savedlocation ORDER BY LocationID ASC LIMIT 10")
    List<SavedLocation> getFirst10();
}
