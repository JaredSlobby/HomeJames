package com.example.homejameswil;

public class DriverLocation
{

    String UID;
    double latitude;
    double longitude;
    String status;

    public DriverLocation() {

    }

    public DriverLocation(String uid, double latitude, double longitude, String status) {
        this.UID = uid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }
    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
