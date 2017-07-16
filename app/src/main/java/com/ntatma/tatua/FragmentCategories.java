package com.ntatma.tatua;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luqi on 7/8/17.
 */

public class FragmentCategories extends ListFragment implements AdapterView.OnItemClickListener {

    public static String TAG = "FragmentCategories";

    Context mContext;
    Utils utils;
    ArrayList<String> categories=new ArrayList<String>();
    ArrayAdapter<String> adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();
        utils = new Utils(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, categories);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
        getCategories();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), "Item: " + position, Toast.LENGTH_SHORT).show();
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
                JSONArray jsonArray = parser.getJSONArray(utils.getCurrentIPAddress() +"tatua/api/v1.0/categories",null);
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
