package com.omerar.androidduckowl;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class Utils {
    final static String TAG = Utils.class.getSimpleName();

    public static boolean isConnectedToDuckAP(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            String ssid = info.getExtraInfo();
            Log.e(TAG, "WiFi SSID: " + ssid);
            if (!ssid.contains("EMERGENCY")) {
                Toast.makeText(context, "Not connected to the correct Duck's Wifi! Please connect and try again", Toast.LENGTH_LONG).show();
                return true;
            } else {
                Log.e(TAG, "GREAT SUCCESS! CONNECTED TO THE DUCK WIFI!");
            }
        } else if (info == null) {
            Toast.makeText(context, "Not connected to the Duck's Wifi! Please connect and try again", Toast.LENGTH_LONG).show();
        }
        return false;
    }


}
