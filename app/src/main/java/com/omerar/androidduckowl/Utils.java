package com.omerar.androidduckowl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {
    final static String TAG = Utils.class.getSimpleName();

    public boolean isConnectedToDuckAP(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        if (info != null && info.isConnected()) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            wifiInfo.getSSID();
            String name = info.getExtraInfo();
            String ssid = wifiInfo.getSSID();

//            Log.e(TAG, "WiFi SSID: " + ssid);
            if (ssid == null) {
//                Toast.makeText(context, "Not connected to the correct Duck's Wifi! Please connect and try again", Toast.LENGTH_LONG).show();
                return false;
            }
            if (!ssid.contains("EMERGENCY")) {
//                Toast.makeText(context, "Not connected to the correct Duck's Wifi! Please connect and try again", Toast.LENGTH_LONG).show();
                return false;
            } else {
//                Log.e(TAG, "GREAT SUCCESS! CONNECTED TO THE DUCK WIFI!");
                return true;
            }
        } else if (info == null) {
//            Toast.makeText(context, "Not connected to the Duck's Wifi! Please connect and try again", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            if (isConnectedToDuckAP(context)) {
                return false;
            } else {
                return true;
            }

        }
        return false;
    }


    public void sendGetRequest(final Context context, final EmergencyRequest emergencyRequest) {
        String url = Constants.getDuck_AP_IP();

        StringRequest commonRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //HANDLE RESPONSE
//                Log.e(TAG, "Response == " + response);
                if (response.contains("EMERGENCY")) {
                    Toast.makeText(context,"MSG was sent successfully!",Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle your error types accordingly.For Timeout & No
                // connection error, you can show 'retry' button.
                // For AuthFailure, you can re login with user
                // credentials.
                // For ClientError, 400 & 401, Errors happening on
                // client side when sending api request.
                // In this case you can check how client is forming the
                // api and debug accordingly.
                // For ServerError 5xx, you can do retry or handle
                // accordingly.
                Log.e(TAG, error.toString());
                Toast.makeText(context,"Got an error from the HTTP Request! Is your phone on Airplane Mode?", Toast.LENGTH_LONG).show();

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> hashMap = emergencyRequest.getMap();
                return hashMap;
            }
        };
        MySingleton.getInstance(context).addToRequestQueue(commonRequest);
    }

    public void connectToDuckAP(Context context) {
        String networkSSID = " 🆘 DUCK EMERGENCY PORTAL";


        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain ssid in quotes
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.addNetwork(conf);
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();

                break;
            }
        }
    }

    public void getIOTPCredentials(Context context) {
        sendPOSTRequestCredentials(context);
    }

    private void sendPOSTRequestCredentials(final Context context) {
        String url = Constants.getDuckApiGetDeviceCredentials();

        JSONObject jsonBodyObj = new JSONObject();

        try{
            String macAddress = getMACAddress();
            if (macAddress.equals("")) {
                macAddress = "0000000";
            }
            macAddress = macAddress.replace(":","");
            jsonBodyObj.put("type", "android");
            jsonBodyObj.put("id", macAddress);
        }catch (JSONException e){
            e.printStackTrace();
        }
        final String requestBody = jsonBodyObj.toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
                Log.e(TAG, String.valueOf(response));
                try {
                    SharedPreferences sharedPref = context.getSharedPreferences(
                            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();

                    JSONObject credentials = (JSONObject) response.get("credentials");
                    String authToken = credentials.get("token").toString();
                    Constants.setAuthToken(authToken);
                    editor.putString(context.getString(R.string.authToken), authToken);

                    String deviceId = credentials.get("id").toString();
                    Constants.setDeviceID(deviceId);
                    editor.putString(context.getString(R.string.deviceID), deviceId);

                    String deviceType = credentials.get("type").toString();
                    Constants.setDeviceType(deviceType);
                    editor.putString(context.getString(R.string.deviceType), deviceType);

                    String organization = credentials.get("organization").toString();
                    Constants.setOrganization(organization);
                    editor.putString(context.getString(R.string.organization), organization);

                    editor.putBoolean(context.getString(R.string.mqtt_credentials_set), Boolean.TRUE);

                    editor.apply();
                    Log.e(TAG, "Organziation=" + organization + " , deviceType=" + deviceType + " , deviceID=" + deviceId + " ,authToken=" + authToken);
                            Intent intent = new Intent();
                            intent.setAction("MQTT_CREDENTIALS_RECIEVED");
//                            intent.putExtra("data","Notice me senpai!");
                            context.sendBroadcast(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override    public void onErrorResponse(VolleyError error) {
                if ((error != null) && (error.getMessage() != null)) {
                    Log.e(TAG, error.getMessage());
                }
            }
        }){
            @Override    public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                return headers;
            }


            @Override    public byte[] getBody() {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                            requestBody, "utf-8");
                    return null;
                }
            }


        };
                MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }


    public void sendPOSTRequestMSGStatus(final Context context, String description) {
        String url = Constants.getDuckApiGetMessagesStatus();

        JSONObject jsonBodyObj = new JSONObject();

        try{

            JSONArray msgs = new JSONArray();
            List<String> msgsList = Constants.getMessageIDs();
            for (int i=0; i<msgsList.size(); i++) {
                msgs.put(msgsList.get(i));
            }

            jsonBodyObj.put("message_ids", msgs);
            jsonBodyObj.put("name", description);
        }catch (JSONException e){
            e.printStackTrace();
        }
        final String requestBody = jsonBodyObj.toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url, null, new Response.Listener<JSONObject>(){
            @Override
            public void onResponse(JSONObject response) {
                Log.e(TAG, String.valueOf(response));

                Log.e(TAG, response.toString());
//                Toast.makeText(context, response.toString(),Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(context, ResultsActivity.class);
                    intent.putExtra("TEST", response.toString());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(intent);

            }
        }, new Response.ErrorListener() {
            @Override    public void onErrorResponse(VolleyError error) {
                if ((error != null) && (error.getMessage() != null)) {
                    Log.e(TAG, error.getMessage());
                }
            }
        }){
            @Override    public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                return headers;
            }


            @Override    public byte[] getBody() {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                            requestBody, "utf-8");
                    return null;
                }
            }


        };
        MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);

    }



    public String getMACAddress() {
        String address = "";
        try {
            String interfaceName = "wlan0";
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)){
                    continue;
                }

                byte[] mac = intf.getHardwareAddress();
                if (mac==null){
                    address = "";
                }

                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) {
                    buf.append(String.format("%02X:", aMac));
                }
                if (buf.length()>0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                address = buf.toString();
            }
        } catch (Exception ex) { } // for now eat exceptions
//        Log.e(TAG,"This device MAC address: " + address);

        return address;
    }

    public void sendGetRequestDuckMACAddress(final Context context) {
        String url = Constants.getDuck_MAC_AP_IP();

        StringRequest commonRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //HANDLE RESPONSE
                Log.e(TAG, "Response == " + response);
                if (response != null) {
                    Toast.makeText(context, "Sending a Duck Observation message to the queue!", Toast.LENGTH_SHORT).show();
                    String[] responseArray = response.split(",") ;
                    String duckMac =  responseArray[0];
                    String duckType = responseArray[1];
                    Log.e(TAG, "duckMac == " + duckMac + " , duckType == " + duckType);
                    Constants.setDuckMacAddress(duckMac);
                    Constants.setDuckType(duckType);
                    Intent intent = new Intent();
                    intent.setAction("DUCK_MACADRESS_UPDATED");
//                    intent.putExtra("data","Notice me senpai!");
                    context.sendBroadcast(intent);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle your error types accordingly.For Timeout & No
                // connection error, you can show 'retry' button.
                // For AuthFailure, you can re login with user
                // credentials.
                // For ClientError, 400 & 401, Errors happening on
                // client side when sending api request.
                // In this case you can check how client is forming the
                // api and debug accordingly.
                // For ServerError 5xx, you can do retry or handle
                // accordingly.
//                Log.e(TAG, error.toString());
                Toast.makeText(context," Not connected to a duck!", Toast.LENGTH_LONG).show();

            }
        });
        MySingleton.getInstance(context).addToRequestQueue(commonRequest);
    }




}
