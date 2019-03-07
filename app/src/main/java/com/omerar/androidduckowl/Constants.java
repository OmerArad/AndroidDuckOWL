package com.omerar.androidduckowl;

public class Constants {



    // Omer's IoTP //
//    private static final String organization = "qr7t4e";
//    private static final String deviceID = "test2";
//    private static final String authToken = "fC64mwjdLqqVFxquLk";
//    private static final String deviceType  = "android2";
//    private static final String IOT_DEVICE_USERNAME  = "use-token-auth";
//    private static final String publishTopic = "iot-2/evt/test/fmt/json";
//    private static final String clientID = "d:" + organization + ":" + deviceType + ":" + deviceID;


    // OWL IOTP //
//    private static  String organization = "zoad0c";
//    private static  String deviceID = "omer-android-app";
//    private static  String authToken = "kU7sJww216BA+RZ)If";
//    private static  String deviceType  = "android";



    private static String organization = "";
    private static String deviceID = "";
    private static String authToken = "";
    private static String deviceType  = "";



    private static final String IOT_DEVICE_USERNAME  = "use-token-auth";
    private static final String publishTopic = "iot-2/evt/androidDebug/fmt/json";


    private static final String subscriptionTopic = "iot-2/cmd/+/fmt/json";
    private static String clientID = "";
//    Organization ID: zoad0c
//    Device Type: android
//    Device ID: omer-android-app
//    Authentication Method: use-token-auth
//    Authentication Token: kU7sJww216BA+RZ)If

    private static String serverUri = "";
    private static final String duck_AP_IP = "http://192.168.1.1:80/";




    public static String getServerUri() {
        String uri = "ssl://" + organization + ".messaging.internetofthings.ibmcloud.com:8883";
        return uri;
    }

    public static String getClientId() {
        String client = "d:" + organization + ":" + deviceType + ":" + deviceID;
        return client;
    }
    public static String getPublishTopic() {
        return publishTopic;
    }
    public static String getDuck_AP_IP() {
        return duck_AP_IP;
    }
    public static String getDeviceID() {
        return deviceID;
    }

    public static String getAuthToken() {
        return authToken;
    }

    public static String getIotDeviceUsername() {
        return IOT_DEVICE_USERNAME;
    }

    public static String getDeviceType() {
        return deviceType;
    }

    public static void setOrganization(String organization) {
        Constants.organization = organization;
    }

    public static void setDeviceID(String deviceID) {
        Constants.deviceID = deviceID;
    }

    public static void setAuthToken(String authToken) {
        Constants.authToken = authToken;
    }

    public static void setDeviceType(String deviceType) {
        Constants.deviceType = deviceType;
    }

    public static String getOrganization() {
        return organization;
    }


    public static String getSubscriptionTopic() {
        return subscriptionTopic;
    }
}
