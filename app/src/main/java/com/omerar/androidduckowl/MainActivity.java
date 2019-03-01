package com.omerar.androidduckowl;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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

import java.security.Permissions;

public class MainActivity extends AppCompatActivity {
    final String TAG = MainActivity.class.getSimpleName();

    MqttAndroidClient mqttAndroidClient;
    Utils utils;
    BroadcastReceiver mNetworkReceiver;


    final String publishMessage = "{'msg' : 'Hello World Test!'}";      //TODO: Change!
    String localClientId;
    MqttConnectOptions mqttConnectOptions;
    Boolean duckIsConnected = false;

    int counter = 0;
    Location lastKnownLocation;
    TextView connectionTextView;
    ImageView connectedImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        mNetworkReceiver = new NetworkBroadcastReciever();
        registerReceiver(mNetworkReceiver,new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
        setContentView(R.layout.activity_main);

        RequestQueue queue = MySingleton.getInstance(this.getApplicationContext()).
                getRequestQueue();

        localClientId = Constants.getClientId() + System.currentTimeMillis();
        utils = new Utils();

        initializeMQTT();

        getLocation();

        connectionTextView = findViewById(R.id.connection_status);
        connectedImage = findViewById(R.id.connected_image);
        checkConnectionStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkConnectionStatus();
    }

    private void checkConnectionStatus() {
        //TODO: Add listener when the wifi changes it should change automatically!
        if (utils.isConnectedToDuckAP(getApplicationContext())) {
            connectionTextView.setText(R.string.connected);
            connectionTextView.setTextColor(Color.GREEN);
            connectedImage.setImageDrawable(getResources().getDrawable(R.drawable.connected_duck));
        } else {
            connectionTextView.setText(R.string.disconnected);
            connectionTextView.setTextColor(Color.RED);
            connectedImage.setImageDrawable(getResources().getDrawable(R.drawable.disconnected_duck));
        }
    }

    void getLocation() {
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
//                makeUseOfNewLocation(location);
                lastKnownLocation = location;
                Log.e(TAG, "Location == " + location.toString());
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };


// Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            //TODO: OMER -> Not working!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            Log.e(TAG, "PROBLEM GPS!");
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
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
//                    publishMessage();   //TODO: Send a connected msg!

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
            String counterSTR = String.valueOf(counter);
            EmergencyRequest emergencyRequest = new EmergencyRequest(counterSTR,"User" + counter,
                                   counterSTR,counterSTR,counterSTR,counterSTR,
                                    counterSTR,counterSTR,counterSTR,counterSTR,counterSTR);
            utils.sendGetRequest(getApplicationContext(),emergencyRequest);
            counter++;
        }


    }

    public void sendManySOSMSGS(View view) {
        Log.e(TAG, "Trying to send SOS MSG!");
        boolean isConnected = utils.isConnectedToDuckAP(getApplicationContext());
        if (isConnected) {
            for (int i=0; i<10; i++) {
                String counterSTR = String.valueOf(counter);
                EmergencyRequest emergencyRequest = new EmergencyRequest(counterSTR,"User" + counter,
                        counterSTR,counterSTR,counterSTR,counterSTR,
                        counterSTR,counterSTR,counterSTR,counterSTR,counterSTR);
                utils.sendGetRequest(getApplicationContext(), emergencyRequest);
                counter++;
            }
        }

        publishMessage();
    }


    public class NetworkBroadcastReciever extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            try {
                checkConnectionStatus();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mNetworkReceiver);
    }
}