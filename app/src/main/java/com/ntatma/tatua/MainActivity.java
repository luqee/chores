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
        getCategories();
    }

    public void getCategories(){

        new AsyncTask<Void, Void, JSONArray>(){

            @Override
            protected JSONArray doInBackground(Void... params) {
                Log.d(TAG, "Fetching Categories to display");
                List<NameValuePair> nameValuePairs;
                JSONParser parser = new JSONParser();
                nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("name", utils.getFromPreferences(Utils.USER_NAME)));
                nameValuePairs.add(new BasicNameValuePair("number", utils.getFromPreferences(Utils.USER_NUMBER)));
                nameValuePairs.add(new BasicNameValuePair("registered_as", utils.getFromPreferences(Utils.LOGED_IN_AS)));
                Log.d(TAG, "create namevalue pairs");
                JSONArray jsonArray = parser.getJSONArray(utils.getCurrentIPAddress() +"tatua/api/v1.0/categories",nameValuePairs);
                return jsonArray;
            }

            @Override
            protected void onPostExecute(JSONArray jsonArray) {
                try {
                    String response = jsonArray.getString(Integer.parseInt("result"));

                    if (response.equals("success")){
                        Log.d(TAG, "Successfully fetched categiries");
                        //update the categories fragment
                    }else if (response.equals("error")){
                        Log.d(TAG, "Error in registration");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute(null, null, null);
    }

}
