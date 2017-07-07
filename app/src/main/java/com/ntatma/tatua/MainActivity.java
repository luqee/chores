package com.ntatma.tatua;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> categories=new ArrayList<String>();

    ArrayAdapter<String> adapter;
    private ListView mListView;
    Utils utils;
    Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        if (mListView == null){
            mListView = (ListView) findViewById(R.id.lst_cats);
        }
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, categories);
        mListView.setAdapter(adapter);
        fetchCats();
    }

    public void fetchCats(){
        new AsyncTask<Void, Void, JSONObject>(){

            @Override
            protected JSONObject doInBackground(Void... params) {
                List<NameValuePair> nameValuePairs;
                JSONParser parser = new JSONParser();
                nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("name", utils.getFromPreferences(Utils.USER_NAME)));
                nameValuePairs.add(new BasicNameValuePair("number", utils.getFromPreferences(Utils.USER_NUMBER)));
                nameValuePairs.add(new BasicNameValuePair("registered_as", utils.getFromPreferences(Utils.PROPERTY_TOKEN_ID)));
                JSONObject jsonObject = parser.getJSONFromUrl(utils.getCurrentIPAddress() +"/tatua/api/v1.0/categories",nameValuePairs);
                return jsonObject;
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                try {
                    String response = jsonObject.getString("result");

                    if (response.equals("Success")){

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute(null, null, null);
    }

}
