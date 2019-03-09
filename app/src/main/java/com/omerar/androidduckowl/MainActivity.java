package com.omerar.androidduckowl;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    final String TAG = MainActivity.class.getSimpleName();
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    SharedPreferences sharedPref;
    MqttAndroidClient mqttAndroidClient;
    Utils utils;
    BroadcastReceiver mNetworkReceiver;

    String localClientId;
    MqttConnectOptions mqttConnectOptions;


    int counter = 0;
    Location lastKnownLocation;
    TextView duckConnectionTextView;
    TextView mqttConnectionTextView;
    TextView locationTextView;
    ImageView connectedImage;
    TextView msgDebug;
    Button checkDBBUtton;
    Button tagDuckButton;
    String m_Text = "";


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
        Context context = getApplicationContext();
        checkDBBUtton = findViewById(R.id.checkDB);
        tagDuckButton = findViewById(R.id.tagDuck);
        locationTextView = findViewById(R.id.location_textview);

        sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);


        mNetworkReceiver = new NetworkBroadcastReceiver();
        registerReceiver(mNetworkReceiver,new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
        registerReceiver(mNetworkReceiver,new IntentFilter("MQTT_CREDENTIALS_RECIEVED"));
        registerReceiver(mNetworkReceiver,new IntentFilter("DUCK_MACADRESS_UPDATED"));
        setContentView(R.layout.activity_main);

        RequestQueue queue = MySingleton.getInstance(this.getApplicationContext()).
                getRequestQueue();

        localClientId = Constants.getClientId() + System.currentTimeMillis();

        getLocation();

        duckConnectionTextView = findViewById(R.id.duck_connection_status);
        mqttConnectionTextView = findViewById(R.id.mqtt_connection_status);
        connectedImage = findViewById(R.id.connected_image);
        msgDebug = findViewById(R.id.msg_debug);
        checkDuckConnectionStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkDuckConnectionStatus();
    }

    private void checkDuckConnectionStatus() {
        if (utils.isConnectedToDuckAP(getApplicationContext())) {
            duckConnectionTextView.setText(R.string.duck_connected);
            duckConnectionTextView.setTextColor(Color.GREEN);
            connectedImage.setImageDrawable(getResources().getDrawable(R.drawable.connected_duck));
        } else {
            duckConnectionTextView.setText(R.string.duck_disconnected);
            duckConnectionTextView.setTextColor(Color.RED);
            connectedImage.setImageDrawable(getResources().getDrawable(R.drawable.disconnected_duck));
        }

        if ((mqttAndroidClient != null) && (mqttAndroidClient.isConnected())) {
            mqttConnectionTextView.setText(R.string.mqtt_connected);
            mqttConnectionTextView.setTextColor(Color.GREEN);
        } else {
            mqttConnectionTextView.setText(R.string.mqtt_disconnected);
            mqttConnectionTextView.setTextColor(Color.RED);
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
                if (Constants.getDuckLastLocation() != null) {
                    float distance = lastKnownLocation.distanceTo(Constants.getDuckLastLocation());
                    String distanceString = "Distance: " + String.valueOf(distance);
                    try {
                        TextView textView = findViewById(R.id.location_textview);
                        textView.setText(distanceString);
                        Log.e(TAG, "Trying to change distance: " + distanceString);


                    } catch (Exception exception) {
                        Log.e(TAG, exception.toString());
                    }

                }

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };



// Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                    checkDuckConnectionStatus();
                } else {
                    Log.e(TAG,"Connected to: " + serverURI);
                    Toast.makeText(getApplicationContext(),"MQTT Connected Successfuly!", Toast.LENGTH_SHORT).show();
                    checkDuckConnectionStatus();
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.e(TAG,"The Connection was lost.");
                checkDuckConnectionStatus();
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
                    Log.e(TAG, " MQTT Connected Successfuly! :)");
                    Toast.makeText(getApplicationContext(),"MQTT Connected Successfuly!", Toast.LENGTH_SHORT).show();
                    checkDuckConnectionStatus();
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
                    Toast.makeText(getApplicationContext(),"MQTT Connection Failed!", Toast.LENGTH_SHORT).show();
                    checkDuckConnectionStatus();
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }

    }

    public void subscribeToTopic(){
        //TODO: Currently not working!
        try {

            mqttAndroidClient.subscribe(Constants.getSubscriptionTopic(), 0, getApplicationContext(), new IMqttActionListener() {
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

//            // THIS DOES NOT WORK!
            mqttAndroidClient.subscribe(Constants.getSubscriptionTopic(), 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // message Arrived!
                    Log.e(TAG, "Message: " + topic + " : " + new String(message.getPayload()));
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
            mqttAndroidClient.publish(Constants.getPublishTopic(), message);
            Log.e(TAG, " Trying to publish: " + message);
            if(!mqttAndroidClient.isConnected()){
//                Log.e(TAG,mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void publishDuckObservation(DuckObservation duckObservation){

        try {
            MqttMessage message = new MqttMessage();
            Gson gson = new Gson();
            String json = gson.toJson(duckObservation);
            message.setPayload(json.getBytes());
            message.setQos(1);
            mqttAndroidClient.publish(Constants.getPublishTopicDuckObservation(), message);
            Log.e(TAG, " Trying to publish Duck Observation: " + message);
            if(!mqttAndroidClient.isConnected()){
//                Log.e(TAG,mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void sendSOSAutomaticMessage(View view) {
        boolean isConnected = utils.isConnectedToDuckAP(getApplicationContext());
        if (isConnected) {
            String counterSTR = String.valueOf(counter);
            String uniqueId = UUID.randomUUID().toString().substring(0,8);

            EmergencyRequest emergencyRequest = new EmergencyRequest(uniqueId,  uniqueId,
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
            msgDebug.setText("Send Emergency Request ID: " + uniqueId +  " , #" + counterSTR);
            counter++;
            publishMessage(emergencyRequest);
            Log.e(TAG, "Trying to send SOS MSG!");
            Constants.addMessageID(uniqueId);
        } else {
            Toast.makeText(getApplicationContext(),"Please connect to a duck first!", Toast.LENGTH_SHORT).show();
        }

    }

    public void sendManySOSMSGS(View view) {
        Log.e(TAG, "Trying to send SOS MSG!");
        boolean isConnected = utils.isConnectedToDuckAP(getApplicationContext());
        if (isConnected) {
            for (int i=0; i<10; i++) {
                sendSOSAutomaticMessage(view);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        } else {
            Toast.makeText(getApplicationContext(),"Please connect to a duck first!", Toast.LENGTH_SHORT).show();
        }
    }

    public void checkDB(View view) {
        if (utils.isConnectedToInternet(getApplicationContext())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Write a description for the test!");


            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    m_Text = input.getText().toString();
                    utils.sendPOSTRequestMSGStatus(getApplicationContext(), m_Text);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();



        } else {
            Toast.makeText(getApplicationContext(),"Please connect the internet first!", Toast.LENGTH_SHORT).show();
        }
    }

    public void tagDuck(View view) {
        if (utils.isConnectedToDuckAP(getApplicationContext())) {
            Constants.setDuckLastLocation(lastKnownLocation);
            utils.sendGetRequestDuckMACAddress(getApplicationContext());
        } else {
            Toast.makeText(getApplicationContext(),"Please connect to a duck first!", Toast.LENGTH_SHORT).show();
        }

    }


    public class NetworkBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            try {
                checkDuckConnectionStatus();

                utils.getMACAddress();

                if (utils.isConnectedToDuckAP(context)) {
                    //        utils.connectToDuckAP(getApplicationContext());       //Should not always be created!
                    checkDBBUtton.setClickable(false);
                    tagDuckButton.setClickable(true);

                }
                if (utils.isConnectedToInternet(context)) {
                    checkDBBUtton.setClickable(true);
                    tagDuckButton.setClickable(false);
                }

                Boolean mqtt_credentials_set = sharedPref.getBoolean(getString(R.string.mqtt_credentials_set), Boolean.FALSE);
                    if ((intent.getAction() != null) && intent.getAction().equals("MQTT_CREDENTIALS_RECIEVED") && (mqttAndroidClient == null)) {
                        Log.e(TAG, "11111111111111");
                        // This case is where the API Call returned the MQTT credentials. Now it should
                        // initialize the MQTT server and will connect to it.
                        Log.d(TAG, "MQTT Credentials set");
                        initializeMQTT();
                        return;
                    }
                if ((intent.getAction() != null) && intent.getAction().equals("DUCK_MACADRESS_UPDATED") && (mqttAndroidClient != null)) {
                    Log.e(TAG, "444444444444");
                    Log.e(TAG, "Trying to send the new GPS Coords for the Duck Observation!");
                    // This case is when we tagged a duck and we want to send the phone's GPS coordinates
                    // With the duck's Mac Address to the db.
                    msgDebug.setText("Duck MacAddress == " + Constants.getDuckMacAddress());
                    DuckObservation duckObservation = new DuckObservation();
                    duckObservation.setDeviceID(Constants.getDuckMacAddress());
                    duckObservation.setDeviceType("ducklink");
                    if (lastKnownLocation != null) {
                        double latitude = lastKnownLocation.getLatitude();
                        double longitude = lastKnownLocation.getLongitude();
                        if (latitude == 0) {
                            Toast.makeText(context, "No GPS Location yet!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        duckObservation.setLatitude(String.valueOf(latitude));
                        duckObservation.setLongitude(String.valueOf(longitude));
                    }
                    Long tsLong = System.currentTimeMillis();
                    String ts = tsLong.toString();
                    duckObservation.setTimestamp(ts);
                    Log.e(TAG, duckObservation.toString());
                    publishDuckObservation(duckObservation);
                    return;
                }
                    if ((mqttAndroidClient == null) && (mqtt_credentials_set)) {
                        // This case is the general case where the app already has the credentials stored
                        // for it and will just try to connect to the MQTT server.
                        Log.e(TAG, "22222222222222");
                        Constants.setOrganization(sharedPref.getString(getString(R.string.organization), ""));
                        Constants.setDeviceType(sharedPref.getString(getString(R.string.deviceType), ""));
                        Constants.setDeviceID(sharedPref.getString(getString(R.string.deviceID), ""));
                        Constants.setAuthToken(sharedPref.getString(getString(R.string.authToken), ""));
                        Log.e(TAG, "Organziation=" + Constants.getOrganization() + " , deviceType=" + Constants.getDeviceType() +
                                " , deviceID=" + Constants.getDeviceID() + " ,authToken=" + Constants.getAuthToken());
                        initializeMQTT();
                    } else if (!mqtt_credentials_set){
                        Log.e(TAG, "333333333333333");
                        // This is the first time the app opens. Need to go to the API and ask for
                        // the IOTP credentials for this specific Android device.
                        utils.getIOTPCredentials(getApplicationContext());
                    }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }



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



}