package com.omerar.androidduckowl;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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

            Log.e(TAG, "WiFi SSID: " + ssid);
            if (ssid == null) {
                Toast.makeText(context, "Not connected to the correct Duck's Wifi! Please connect and try again", Toast.LENGTH_LONG).show();
                return false;
            }
            if (!ssid.contains("EMERGENCY")) {
                Toast.makeText(context, "Not connected to the correct Duck's Wifi! Please connect and try again", Toast.LENGTH_LONG).show();
                return false;
            } else {
                Log.e(TAG, "GREAT SUCCESS! CONNECTED TO THE DUCK WIFI!");
                return true;
            }
        } else if (info == null) {
            Toast.makeText(context, "Not connected to the Duck's Wifi! Please connect and try again", Toast.LENGTH_LONG).show();
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
//                Log.e(TAG, error.toString());
                Toast.makeText(context,"Please Put your phone on Airplane mode!", Toast.LENGTH_LONG).show();

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> hashMap = emergencyRequest.getMap();
                return hashMap;
            }

        };


//        commonRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 1, 2));
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


}
