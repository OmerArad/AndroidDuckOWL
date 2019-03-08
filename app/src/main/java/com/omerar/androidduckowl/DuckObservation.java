package com.omerar.androidduckowl;

public class DuckObservation {
//    "observation": {
//        "timestamp": "2019-04-12 13:34",
//                "deviceType": "papa-duck",
//                "device_id":"dereks-duck",
//                "latitude": "53.2324",
//                "longitude": "-1.34343"
//    }

    private String deviceType;
    private String deviceID;
    private String latitude;
    private String longitude;
    private String timestamp;

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String device_id) {
        this.deviceID = device_id;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

}
