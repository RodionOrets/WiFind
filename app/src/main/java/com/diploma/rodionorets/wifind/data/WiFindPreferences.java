package com.diploma.rodionorets.wifind.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.diploma.rodionorets.wifind.R;

public final class WiFindPreferences {

    public static String getPrefferedDistanceToWifi(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String distanceKey = context.getString(R.string.pref_distance_key);
        return sp.getString(distanceKey, context.getString(R.string.pref_distance_default));
    }

    public static String getPrefferedWifiLinkSpeed(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String linkSpeedKey = context.getString(R.string.pref_link_speed_key);
        return sp.getString(linkSpeedKey, context.getString(R.string.pref_link_speed_default));
    }

    public static String getPrefferedNumberOfUsers(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String userNumberKey = context.getString(R.string.pref_popularity_key);
        return sp.getString(userNumberKey, context.getString(R.string.pref_user_number_default));
    }

    public static boolean isWifiRequirePassword(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String passwordRequiredKey = context.getString(R.string.pref_password_required_key);
        return sp.getBoolean(passwordRequiredKey, false);
    }
}
