package com.omerar.androidduckowl;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.RequestQueue;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {
    final String TAG = MainActivity.class.getSimpleName();

    MqttAndroidClient mqttAndroidClient;
    Utils utils;

    final String publishMessage = "{'msg' : 'Hello World Test!'}";
    String localClientId;
    MqttConnectOptions mqttConnectOptions;
    Boolean duckIsConnected = false;

                            int counter = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RequestQueue queue = MySingleton.getInstance(this.getApplicationContext()).
                getRequestQueue();

        localClientId = Constants.getClientId() + System.currentTimeMillis();

        utils = new Utils();

        //TODO: OMER -> define the MQTT as a service in the app so it would be accessible from all the activities!
        initializeMQTT();

    }

    void initializeMQTT() {
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), Constants.getServerUri(), Constants.getClientId());

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    Log.e(TAG,"Reconnected to : " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
//                    subscribeToTopic();
                } else {
                    Log.e(TAG,"Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.e(TAG,"The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.e(TAG,"Incoming message: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);      //TODO: OMER -> SHOULD BE TRUE$@^$U@()TU!@(T!PT(J!PG$APODJGVPAOJDGPA(DJGAP(DGJDAPOG
        mqttConnectOptions.setUserName(Constants.getIotDeviceUsername());
        char[] password = Constants.getAuthToken().toCharArray();
        mqttConnectOptions.setPassword(password);
        connectMQTT(mqttConnectOptions);


    }


    void connectMQTT(MqttConnectOptions mqttConnectOptions) {
        try {
            Log.e(TAG,"Connecting to " + Constants.getServerUri());
            mqttAndroidClient.connect(mqttConnectOptions, getApplicationContext(), new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    Log.e(TAG, " Great Success Connecting! :)");
//                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG,"Failed to connect to: " + Constants.getServerUri());
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }

    }

    public void subscribeToTopic(){
        //TODO: Currently not working!
        try {
            mqttAndroidClient.subscribe(Constants.getSubscriptionTopic(), 1, getApplicationContext(), new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.e(TAG,"Subscribed!");
//                    publishMessage();   //TODO: OMER -> Remove from here.

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG,"Failed to subscribe: " + exception.toString());
                }
            });

            // THIS DOES NOT WORK!
            mqttAndroidClient.subscribe(Constants.getSubscriptionTopic(), 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // message Arrived!
                    System.out.println("Message: " + topic + " : " + new String(message.getPayload()));
                }
            });

        } catch (MqttException ex){
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    public void publishMessage(){

        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(publishMessage.getBytes());
            message.setQos(1);
            mqttAndroidClient.publish(Constants.getSubscriptionTopic(), message);
            Log.e(TAG, " Trying to publish: " + message);
            if(!mqttAndroidClient.isConnected()){
                Log.e(TAG,mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void sendSOSAutomaticMessage(View view) {
        Log.e(TAG, "Trying to send SOS MSG!");
        boolean isConnected = utils.isConnectedToDuckAP(getApplicationContext());
        if (isConnected) {
//            EmergencyRequest emergencyRequest = new EmergencyRequest("Test" + counter, "test" + counter);
            String counterSTR = String.valueOf(counter);
            EmergencyRequest emergencyRequest = new EmergencyRequest(counterSTR,"Test" + counter,  counterSTR,counterSTR,counterSTR,counterSTR,counterSTR,counterSTR,counterSTR,counterSTR,counterSTR);
            utils.sendGetRequest(getApplicationContext(),emergencyRequest);
            counter++;
        }


    }

    public void sendManySOSMSGS(View view) {
        Log.e(TAG, "Trying to send SOS MSG!");
        boolean isConnected = utils.isConnectedToDuckAP(getApplicationContext());
        if (isConnected) {
            for (int i=0; i<10; i++) {
                EmergencyRequest emergencyRequest = new EmergencyRequest("Test" + i, "test" + i);
                utils.sendGetRequest(getApplicationContext(), emergencyRequest);
            }
        }

        publishMessage();
    }
}