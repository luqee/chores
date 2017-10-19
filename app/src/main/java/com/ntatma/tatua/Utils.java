package com.ntatma.tatua;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;


public class Utils {
    static Context context;
    public static final String TAG = "Utils";

    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String USER_LATITUDE = "UserLatitude";
    public static final String USER_LONGITUDE = "UserLongitude";

    public static final String IS_USER_REGISTRED = "IsUserRegistered";
    public static final String IS_NUM_VERIFIED = "IsNumberVerified";
    public static final String USER_NAME = "UserName";
    public static final String USER_NUMBER = "UserNumber";
    public static final String USER_ID = "UserId";
    public static final String USER_STATUS = "UserStatus";
    public static final String PROVIDER_NUMBER = "ProvNumber";
    public static final String PROVIDER_NAME = "ProvName";
    public static final String TRANSACTION_ID = "transactId";




    public static final String NexmoAppId = "300e8d3c-d27a-47b6-8016-6870a0d79efc";
    public static final String NexmoSharedSecretKey = "dba21cce6cc2223";

    private static final String PROPERTY_APP_VERSION = "appVersion";


    public Utils(Context context) {
        Utils.context = context;
    }

    public SharedPreferences getAriffPreferences() {
        return context.getSharedPreferences("TatuaPref", Context.MODE_PRIVATE);
    }

    public void savePreferences(String key, String value) {
        final SharedPreferences prefs = getAriffPreferences();
        Log.i(TAG, key + " : " + value);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getFromPreferences(String key) {
        final SharedPreferences prefs = getAriffPreferences();
        String value = prefs.getString(key, "");
        if (value.isEmpty()) {
            Log.i(TAG, key + " not found.");
            return "";
        }
        return value;
    }

    String getRegistrationId() {
        final SharedPreferences prefs = getAriffPreferences();
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
                Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    static int getAppVersion() {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public void storeRegistrationId(String regId) {
        final SharedPreferences prefs = getAriffPreferences();
        int appVersion = Utils.getAppVersion();
        Log.i(TAG, "Saving regId on app version " + appVersion);
        Log.i(TAG, "Reg ID : " + regId);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    public String getCurrentIPAddress() {
        return "http://flask-tatua-api.herokuapp.com/";
    }

    public void showToast(final String txt) {
        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, txt, Toast.LENGTH_LONG).show();
            }
        });
    }

    // convert timestamp to a day's name (e.g., Monday, Tuesday, ...)
    private static String convertTimeStampToDay(long timeStamp){
        Calendar calendar = Calendar.getInstance(); // create Calendar
        calendar.setTimeInMillis(timeStamp * 1000); // set time
        TimeZone tz = TimeZone.getDefault(); // get device's time zone
        // adjust time for device's time zone
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
        // SimpleDateFormat that returns the day's name
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE");
        return dateFormatter.format(calendar.getTime());
    }
}
