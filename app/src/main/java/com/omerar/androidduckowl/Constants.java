package com.omerar.androidduckowl;

public class Constants {





    // Omer's IoTP //
//    private static final String organization = "qr7t4e";
//    private static final String deviceID = "test2";
//    private static final String authToken = "fC64mwjdLqqVFxquLk";
//    private static final String deviceType  = "android2";
//    private static final String IOT_DEVICE_USERNAME  = "use-token-auth";
//    private static final String subscriptionTopic = "iot-2/evt/test/fmt/json";
//    private static final String clientID = "d:" + organization + ":" + deviceType + ":" + deviceID;


    private static final String organization = "zoad0c";
    private static final String deviceID = "omer-android-app";
    private static final String authToken = "kU7sJww216BA+RZ)If";
    private static final String deviceType  = "android";
    private static final String IOT_DEVICE_USERNAME  = "use-token-auth";
    private static final String subscriptionTopic = "iot-2/evt/androidDebug/fmt/json";
    private static final String clientID = "d:" + organization + ":" + deviceType + ":" + deviceID;
//    Organization ID: zoad0c
//    Device Type: android
//    Device ID: omer-android-app
//    Authentication Method: use-token-auth
//    Authentication Token: kU7sJww216BA+RZ)If

    private static final String serverUri = "ssl://" + organization + ".messaging.internetofthings.ibmcloud.com:8883";
    private static final String duck_AP_IP = "http://192.168.1.1:80/";





    public static String getServerUri() {
        return serverUri;
    }
    public static String getClientId() {
        return clientID;
    }
    public static String getSubscriptionTopic() {
        return subscriptionTopic;
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
}
