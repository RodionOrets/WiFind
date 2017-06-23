package com.diploma.rodionorets.wifind.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

/**
 * Created by rodionorets on 17.05.17.
 */

public class WifiUtils {

    public static String getCurrentWifiSSID(Context context) {
        String ssid = null;

        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null) {
            if (networkInfo.isConnected()) {
                final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null && !TextUtils.isEmpty(wifiInfo.getSSID()) && !wifiInfo.getSSID().equals("<unknown ssid>")) {
                    ssid = wifiInfo.getSSID();
                }
            }
        }

        return ssid;
    }

    public static float getCurrentWifiLinkSpeed(Context context) {
        float linkSpeed = 0.0f;

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if(wifiInfo != null) {
            linkSpeed = wifiInfo.getLinkSpeed();
        }

        return linkSpeed;
    }

}
