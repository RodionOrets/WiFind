package com.diploma.rodionorets.wifind;

import android.*;
import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.diploma.rodionorets.wifind.data.WiFindPreferences;
import com.diploma.rodionorets.wifind.data.WifiSpotsContract;
import com.diploma.rodionorets.wifind.data.WifiSpotsContract.WifiSpotsEntry;
import com.diploma.rodionorets.wifind.data.WifiSpotsDBHelper;
import com.diploma.rodionorets.wifind.utils.DistanceCalculator;
import com.diploma.rodionorets.wifind.utils.WifiUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.LinkedList;

import static com.diploma.rodionorets.wifind.data.WifiSpotsContract.WifiSpotsEntry.COLUMN_NAME;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        SharedPreferences.OnSharedPreferenceChangeListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    public static SQLiteDatabase database;
    public static WifiSpotsDBHelper dbHelper;

    FloatingActionButton addSpotButton;

    private boolean PREFERENCES_HAVE_BEEN_UPDATED = false;
    private static final String TAG = MainActivity.class.getSimpleName();

    private LinkedList<Marker> markers;

    private GoogleApiClient googleApiClient;
    private Location location;
    private LocationRequest locationRequest;
    public static double latitude;
    public static double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addSpotButton = (FloatingActionButton) findViewById(R.id.button_add_spot);
        addSpotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewWifiSpot();
            }
        });

        markers = new LinkedList<Marker>();
        dbHelper = new WifiSpotsDBHelper(this);
        database = dbHelper.getReadableDatabase();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
        if (PREFERENCES_HAVE_BEEN_UPDATED || AddNewWifiSpotActivity.NEW_SPOT_HAS_BEEN_ADDED) {
            Log.d(TAG, "onStart: preferences were updated");
            clearMap();
            fillMapWithWifiSpots();
            PREFERENCES_HAVE_BEEN_UPDATED = false;
            AddNewWifiSpotActivity.NEW_SPOT_HAS_BEEN_ADDED = false;
        }

        Log.d(TAG, "dist: " + WiFindPreferences.getPrefferedDistanceToWifi(this));
        Log.d(TAG, "link: " + WiFindPreferences.getPrefferedWifiLinkSpeed(this));
        Log.d(TAG, "user: " + WiFindPreferences.getPrefferedNumberOfUsers(this));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mMap == null) {
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        }
    }



    @Override
    protected void onPause(){
        super.onPause();
        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "in onMapReady()");
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new WiFindInfoWindowAdapter());
        enableMyLocation();
        fillMapWithWifiSpots();
    }


    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, this.getString(R.string.permission_error), Toast.LENGTH_SHORT)
                    .show();
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }


    private void fillMapWithWifiSpots() {
        Log.d(TAG, "in fillMapWithWifiSpots()");
        String selectionQuery = buildSelectionQuery();
        Cursor spots = getSpots(selectionQuery);
        LinkedList<ContentValues> wifiSpots
                = getPrefferedWifiSpots(spots);
        drawMarkers(wifiSpots);
    }


    private Cursor getSpots(String selectionQuery) {
        Cursor queryResult = database.rawQuery(selectionQuery, null);
        Log.d(TAG, "cursor rows: " + String.valueOf(queryResult.getCount()));
        return queryResult;
    }


    private LinkedList<ContentValues> getPrefferedWifiSpots(Cursor cursor) {
        LinkedList<ContentValues> spots = new LinkedList<ContentValues>();

        try {
            while(cursor.moveToNext()) {
                double lat
                        = cursor.getDouble(cursor.getColumnIndex(WifiSpotsEntry.COLUMN_LAT));
                double lng
                        = cursor.getDouble(cursor.getColumnIndex(WifiSpotsEntry.COLUMN_LNG));
                String name
                        = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                double linkSpeed
                        = cursor.getDouble(cursor.getColumnIndex(WifiSpotsEntry.COLUMN_LINK_SPEED));
                int userNumber
                        = cursor.getInt(cursor.getColumnIndex(WifiSpotsEntry.COLUMN_USER_NUMBER));
                String password
                        = cursor.getString(cursor.getColumnIndex(WifiSpotsEntry.COLUMN_PASSWORD));

                String prefferedDistance = WiFindPreferences.getPrefferedDistanceToWifi(this);

                ContentValues cv = new ContentValues();
                if(prefferedDistance.equals("any") || prefferedDistance.equals("def")) {
                    cv.put(WifiSpotsEntry.COLUMN_LAT, lat);
                    cv.put(WifiSpotsEntry.COLUMN_LNG, lng);
                    cv.put(WifiSpotsEntry.COLUMN_NAME, name);
                    cv.put(WifiSpotsEntry.COLUMN_LINK_SPEED, linkSpeed);
                    cv.put(WifiSpotsEntry.COLUMN_USER_NUMBER, userNumber);
                    cv.put(WifiSpotsEntry.COLUMN_PASSWORD, password);
                    spots.add(cv);
                } else {
                    double distance = Double.parseDouble(prefferedDistance);
                    if(DistanceCalculator.checkDistance(latitude, longitude, lat,lng, distance)) {
                        cv.put(WifiSpotsEntry.COLUMN_LAT, lat);
                        cv.put(WifiSpotsEntry.COLUMN_LNG, lng);
                        cv.put(WifiSpotsEntry.COLUMN_NAME, name);
                        cv.put(WifiSpotsEntry.COLUMN_LINK_SPEED, linkSpeed);
                        cv.put(WifiSpotsEntry.COLUMN_USER_NUMBER, userNumber);
                        cv.put(WifiSpotsEntry.COLUMN_PASSWORD, password);
                        spots.add(cv);
                    }
                }
            }
        } finally {
            cursor.close();
        }

        return spots;
    }


    private void drawMarkers(LinkedList<ContentValues> spots) {
        if (spots == null || spots.isEmpty()) {
            Toast.makeText(this, this.getString(R.string.no_spots_error), Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        for (ContentValues cv : spots) {
            double lat = cv.getAsDouble(WifiSpotsEntry.COLUMN_LAT);
            double lng = cv.getAsDouble(WifiSpotsEntry.COLUMN_LNG);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lng)));
            marker.setTitle(buildMarkerTitle(cv));
        }
    }


    private String buildMarkerTitle(ContentValues cv) {
        String title = "Wifi name: " +
                cv.getAsString(COLUMN_NAME) +
                "; \nLink speed: " +
                cv.getAsDouble(WifiSpotsEntry.COLUMN_LINK_SPEED) +
                " mb/s;\nNumber of users: " +
                cv.getAsInteger(WifiSpotsEntry.COLUMN_USER_NUMBER) + "\n";

        if (!cv.getAsString(WifiSpotsEntry.COLUMN_PASSWORD).equals("undefined")) {
            title = title +
                    "Password: " +
                    cv.getAsString(WifiSpotsEntry.COLUMN_PASSWORD);
        }

        return title;
    }


    private void addNewWifiSpot() {
        if (WifiUtils.getCurrentWifiSSID(this) != null) {
            startActivity(new Intent(this, AddNewWifiSpotActivity.class));
        } else {
            Toast.makeText(this, "Available only when connected to WiFi", Toast.LENGTH_SHORT).show();
        }
    }


    private String buildSelectionQuery() {
        StringBuilder selectionQuery = new StringBuilder();
        selectionQuery
                .append("SELECT * FROM " + WifiSpotsEntry.TABLE_NAME);

        String linkSpeedPreference = WiFindPreferences.getPrefferedWifiLinkSpeed(this);
        String userNumberPreference = WiFindPreferences.getPrefferedNumberOfUsers(this);
        boolean showPasswordRequiredSpots = WiFindPreferences.isWifiRequirePassword(this);

        if((!linkSpeedPreference.equals("any") && !linkSpeedPreference.equals("def")) ||
                (!userNumberPreference.equals("any") && !userNumberPreference.equals("def")) ||
                !showPasswordRequiredSpots) {
            selectionQuery.append(" WHERE ");
        }

        if(!linkSpeedPreference.equals("any") && !linkSpeedPreference.equals("def")) {
            double linkSpeed = Double.parseDouble(linkSpeedPreference);
            selectionQuery
                    .append(" link_speed<=")
                    .append(String.valueOf(linkSpeed));

            if(!userNumberPreference.equals("any") || !userNumberPreference.equals("def") || !showPasswordRequiredSpots) {
                selectionQuery.append(" AND ");
            }
        }

        if(!userNumberPreference.equals("any") && !userNumberPreference.equals("def")) {
            int userNumber = Integer.parseInt(userNumberPreference);
            selectionQuery
                    .append("user_number<=")
                    .append(String.valueOf(userNumber));

            if(!showPasswordRequiredSpots) {
                selectionQuery.append(" AND ");
            }
        }

        if(!showPasswordRequiredSpots) {
            selectionQuery.append(" password=='undefined'");
        }

        Log.d(TAG, "Selection query: " + selectionQuery.toString());
        return selectionQuery.toString();
    }


    private void clearMap() {
        mMap.clear();
        markers.clear();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_map_settings) {
            Intent mapSettingsActivityIntent = new Intent(this, MapSettingsActivity.class);
            try {
                startActivity(mapSettingsActivityIntent);
            } catch (Exception ex) {
                Toast.makeText(this, "Excception", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        PREFERENCES_HAVE_BEEN_UPDATED = true;
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if(googleApiClient.isConnected()) {
            Log.d(TAG, "googleApiClient is connected");
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }


    class WiFindInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View markerInfoView;

        WiFindInfoWindowAdapter() {
            markerInfoView = getLayoutInflater().inflate(R.layout.custom_info_window, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            TextView marketTextView = (TextView) markerInfoView.findViewById(R.id.tv_spot_info);
            marketTextView.setText(marker.getTitle());
            return markerInfoView;
        }
    }
}
