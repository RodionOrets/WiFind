package com.diploma.rodionorets.wifind.data;

import android.provider.BaseColumns;

/**
 * Created by rodionorets on 16.05.17.
 */

public class WifiSpotsContract {

    public static final class WifiSpotsEntry implements BaseColumns {

        public static final String TABLE_NAME = "spots";

        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LNG = "lng";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_LINK_SPEED = "link_speed";

        public static final String COLUMN_USER_NUMBER = "user_number";

        public static final String COLUMN_PASSWORD = "password";

    }

}
