package com.diploma.rodionorets.wifind;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

public class MapSettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MapSettingsActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        onCreatePreferences();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    private void onCreatePreferences() {
        Log.d(TAG, "in onCreatePreferences()");
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        android.preference.PreferenceScreen preferenceScreen = getPreferenceScreen();
        int count = preferenceScreen.getPreferenceCount();

        for (int i = 0; i < count; i++) {
            android.preference.Preference p = preferenceScreen.getPreference(i);
            if (!(p instanceof android.preference.CheckBoxPreference)) {
                String value = sharedPreferences.getString(p.getKey(), "");
                setPreferenceSummary(p, value);
            }
        }
    }

    private void setPreferenceSummary(android.preference.Preference preference, String value) {
        Log.d(TAG, "in setPreferenceSummary()");
        if (preference instanceof android.preference.ListPreference) {
            android.preference.ListPreference listPreference
                    = (android.preference.ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            preference.setSummary(value);
        }
    }

    @Override
    public void onStop() {
        Log.d(TAG, "in onStop()");
        super.onStop();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "in onSharedPreferenceChanged()");
        android.preference.Preference preference = findPreference(key);
        if(preference != null) {
            if(!(preference instanceof android.preference.CheckBoxPreference)) {
                String value = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceSummary(preference, value);
            }
        }
    }
}
