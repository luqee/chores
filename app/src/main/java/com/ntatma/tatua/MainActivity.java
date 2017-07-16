package com.ntatma.tatua;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "MainActivity";
    Utils utils;
    Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        utils = new Utils(mContext);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "After setting content view, Now setting Fragment categories.");
        Fragment fragmentCategories = new FragmentCategories();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.content_frame, fragmentCategories, FragmentCategories.TAG);
        fragmentTransaction.addToBackStack(FragmentCategories.TAG);
        fragmentTransaction.commit();
    }
}
