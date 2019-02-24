package com.omerar.androidduckowl;

public class Constants {


    public static final String serverUri = "tcp://iot.eclipse.org:1883";
    public static final String clientId = "ExampleAndroidClient";
    public static final String subscriptionTopic = "exampleAndroidTopic";



    public static final String duck_AP_IP = "http://192.168.1.1:80/";





    public static String getServerUri() {
        return serverUri;
    }
    public static String getClientId() {
        return clientId;
    }
    public static String getSubscriptionTopic() {
        return subscriptionTopic;
    }
    public static String getDuck_AP_IP() {
        return duck_AP_IP;
    }
}
