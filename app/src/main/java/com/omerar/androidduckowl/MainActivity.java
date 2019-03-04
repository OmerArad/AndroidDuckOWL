package com.omerar.androidduckowl;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.google.gson.Gson;

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
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    MqttAndroidClient mqttAndroidClient;
    Utils utils;
    BroadcastReceiver mNetworkReceiver;

    String localClientId;
    MqttConnectOptions mqttConnectOptions;
    Boolean duckIsConnected = false;

    int counter = 0;
    Location lastKnownLocation;
    TextView connectionTextView;
    ImageView connectedImage;
    TextView msgDebug;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        utils = new Utils();

        utils.connectToDuckAP(getApplicationContext());



        mNetworkReceiver = new NetworkBroadcastReceiver();
        registerReceiver(mNetworkReceiver,new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
        setContentView(R.layout.activity_main);

        RequestQueue queue = MySingleton.getInstance(this.getApplicationContext()).
                getRequestQueue();

        localClientId = Constants.getClientId() + System.currentTimeMillis();


        initializeMQTT();

        getLocation();

        connectionTextView = findViewById(R.id.connection_status);
        connectedImage = findViewById(R.id.connected_image);
        msgDebug = findViewById(R.id.msg_debug);
        checkConnectionStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkConnectionStatus();
    }

    private void checkConnectionStatus() {
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
                Log.d(TAG, "Location == " + location.toString());
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
            checkLocationPermission();
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
        mqttConnectOptions.setCleanSession(true);
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
//                    publishTestMessage();     //Debug only.
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

    public void publishMessage(EmergencyRequest emergencyRequest){

        try {
            MqttMessage message = new MqttMessage();
            Gson gson = new Gson();
            String json = gson.toJson(emergencyRequest);
            message.setPayload(json.getBytes());
            message.setQos(1);
            mqttAndroidClient.publish(Constants.getSubscriptionTopic(), message);
            Log.e(TAG, " Trying to publish: " + message);
            if(!mqttAndroidClient.isConnected()){
//                Log.e(TAG,mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
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
            if (lastKnownLocation == null) {
                Log.e(TAG, "GPS ERROR@");
            }
            if (lastKnownLocation == null) {
                Location loc = new Location("dummyprovider");
                loc.setLatitude(0);
                loc.setLongitude(0);
                lastKnownLocation = loc;
            }
            emergencyRequest.setGpsLocation(lastKnownLocation.getLatitude() + "," + lastKnownLocation.getLongitude());
            utils.sendGetRequest(getApplicationContext(),emergencyRequest);
            msgDebug.setText("Send Emergency Request ID: #" + counterSTR);
            counter++;
            publishMessage(emergencyRequest);
        }


    }

    public void sendManySOSMSGS(View view) {
        Log.e(TAG, "Trying to send SOS MSG!");
        boolean isConnected = utils.isConnectedToDuckAP(getApplicationContext());
        if (isConnected) {
            for (int i=0; i<10; i++) {
                sendSOSAutomaticMessage(view);
            }
        }
    }


    public class NetworkBroadcastReceiver extends BroadcastReceiver
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

//    @Override
//    protected void onStop() {
//        super.onStop();
//        unregisterReceiver(mNetworkReceiver);
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNetworkReceiver);
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Requestions GPS Permission")
                        .setMessage("Please approve")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
//                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }

    void publishTestMessage() {
        String counterSTR = "99";
        EmergencyRequest emergencyRequest = new EmergencyRequest(counterSTR,"User" + counter,
                counterSTR,counterSTR,counterSTR,counterSTR,
                counterSTR,counterSTR,counterSTR,counterSTR,counterSTR);
        publishMessage(emergencyRequest);
    }

}