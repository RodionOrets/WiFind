package com.diploma.rodionorets.wifind;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiInfo;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.diploma.rodionorets.wifind.data.WifiSpotsContract;
import com.diploma.rodionorets.wifind.utils.WifiUtils;


public class AddNewWifiSpotActivity extends AppCompatActivity {

    private static final String TAG = AddNewWifiSpotActivity.class.getSimpleName();
    public static boolean NEW_SPOT_HAS_BEEN_ADDED = false;
    private final int SPOT_IS_NULL = 1;
    private final int WIFI_ALREADY_EXIST = 2;
    private final int ADD_SUCCESSFULL = 3;

    EditText mEditTextCurrentWifiName;
    EditText mEditTextCurrentWifiLinkSpeed;
    EditText mEditTextUserNumber;
    EditText mEditTextWifiPassword;
    Button mDoneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_wifi_spot);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEditTextCurrentWifiName
                = (EditText) findViewById(R.id.et_wifi_ssid);
        mEditTextCurrentWifiLinkSpeed
                = (EditText) findViewById(R.id.et_wifi_link_speed);
        mEditTextUserNumber
                = (EditText) findViewById(R.id.et_wifi_users_number);
        mEditTextWifiPassword
                = (EditText) findViewById(R.id.et_wifi_password);
        mDoneButton
                = (Button) findViewById(R.id.button_add_spot);

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues spot = newSpot();
                if (addNewSpot(spot) == ADD_SUCCESSFULL) {
                    NEW_SPOT_HAS_BEEN_ADDED = true;
                    NavUtils.navigateUpFromSameTask(AddNewWifiSpotActivity.this);
                }
            }
        });

        setSSIDAndLinkSpeed();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }


    private void setSSIDAndLinkSpeed() {
        final Float linkSpeed = WifiUtils.getCurrentWifiLinkSpeed(this);
        final String linkSpeedString = String.valueOf(linkSpeed) + " " + WifiInfo.LINK_SPEED_UNITS;

        mEditTextCurrentWifiName.setText(WifiUtils.getCurrentWifiSSID(this));
        mEditTextCurrentWifiName.setEnabled(false);

        mEditTextCurrentWifiLinkSpeed.setText(linkSpeedString);
        mEditTextCurrentWifiLinkSpeed.setEnabled(false);
    }


    private int addNewSpot(ContentValues spot) {
        if (spot == null) {
            Toast.makeText(this, this.getString(R.string.error_simple), Toast.LENGTH_SHORT).show();
            return SPOT_IS_NULL;
        }

        if (wifiAlreadyExist()) {
            Toast.makeText(this, this.getString(R.string.error_wifi_exist), Toast.LENGTH_SHORT).show();
            return WIFI_ALREADY_EXIST;
        }

        SQLiteDatabase db = MainActivity.dbHelper.getWritableDatabase();
        db.insert(WifiSpotsContract.WifiSpotsEntry.TABLE_NAME, null, spot);
        return ADD_SUCCESSFULL;
    }


    private boolean wifiAlreadyExist() {
        String query = "SELECT * FROM " + WifiSpotsContract.WifiSpotsEntry.TABLE_NAME +
                " WHERE name = '" + WifiUtils.getCurrentWifiSSID(this) + "'";
        Cursor cursor = MainActivity.database.rawQuery(query, null);
        Log.d(TAG, "cursor rows: " + String.valueOf(cursor.getCount()));
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }


    private ContentValues newSpot() {
        if (mEditTextUserNumber.getText().toString().isEmpty()) {
            Toast.makeText(this, this.getString(R.string.error_no_users), Toast.LENGTH_SHORT).show();
            return null;
        }

        double latitude = MainActivity.latitude;
        double longitude = MainActivity.longitude;

        String ssid = WifiUtils.getCurrentWifiSSID(this);
        float linkSpeed = WifiUtils.getCurrentWifiLinkSpeed(this);
        int userNumber = Integer.parseInt(mEditTextUserNumber.getText().toString());

        if(userNumber <= 0) {
            Toast.makeText(this, this.getString(R.string.error_invalid_number_of_users), Toast.LENGTH_SHORT).show();
            return null;
        }

        String password = "";
        if(mEditTextWifiPassword.getText().toString().isEmpty()) {
            Log.d(TAG, "Wifi field is empty");
            password = "undefined";
        } else {
            password = mEditTextWifiPassword.getText().toString();
        }

        Log.d(TAG, "latlng: " + String.valueOf(latitude) + " " + String.valueOf(longitude));
        Log.d(TAG, "ssid: " + ssid);
        Log.d(TAG, "users: " + String.valueOf(userNumber));
        Log.d(TAG, "password: " + password);

        ContentValues spot = new ContentValues();
        spot.put(WifiSpotsContract.WifiSpotsEntry.COLUMN_LAT, latitude);
        spot.put(WifiSpotsContract.WifiSpotsEntry.COLUMN_LNG, longitude);
        spot.put(WifiSpotsContract.WifiSpotsEntry.COLUMN_NAME, ssid);
        spot.put(WifiSpotsContract.WifiSpotsEntry.COLUMN_LINK_SPEED, linkSpeed);
        spot.put(WifiSpotsContract.WifiSpotsEntry.COLUMN_USER_NUMBER, userNumber);
        spot.put(WifiSpotsContract.WifiSpotsEntry.COLUMN_PASSWORD, password);
        return spot;
    }
}

