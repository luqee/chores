package com.ntatma.tatua;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

public class FragmentCategories extends Fragment implements AdapterView.OnItemClickListener {

    public static String TAG = "FragmentCategories";

    Context mContext;
    Utils utils;
    List<Category> categories = new ArrayList<>();
    CategoriesAdapter adapter;
    RecyclerView recyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();
        utils = new Utils(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new CategoriesAdapter(mContext, categories, new CategoriesAdapter.CategoriesAdapterListener() {
            @Override
            public void onCategoryClicked(String name) {
                Log.d(TAG, "Item : " + name + " clicked");
            }
        });
//        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, categories);
        recyclerView.setAdapter(adapter);
//        getListView().setOnItemClickListener(this);
        getCategories();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), "Item: " + position, Toast.LENGTH_SHORT).show();
    }

    public void getCategories(){

        new AsyncTask<Void, Void, JSONObject>(){

            @Override
            protected JSONObject doInBackground(Void... params) {
                Log.d(TAG, "Fetching Categories to display");
                List<NameValuePair> nameValuePairs;
                JSONParser parser = new JSONParser();
                nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("name", utils.getFromPreferences(Utils.USER_NAME)));
                nameValuePairs.add(new BasicNameValuePair("number", utils.getFromPreferences(Utils.USER_NUMBER)));
                nameValuePairs.add(new BasicNameValuePair("registered_as", utils.getFromPreferences(Utils.LOGED_IN_AS)));
                Log.d(TAG, "create namevalue pairs");
                JSONObject jsonObject = parser.getJSONArray(utils.getCurrentIPAddress() +"tatua/api/v1.0/categories",null);
                return jsonObject;
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                try {
                    String response = jsonObject.getString("result");

                    if (response.equals("success")){
                        Log.d(TAG, "Successfully fetched categiries : ");

                        for (int i = 0 ; i<jsonObject.getJSONArray("categories").length(); i++) {
                            JSONObject categoryJSON = jsonObject.getJSONArray("categories").getJSONObject(i);
                            Category category = new Category();
                            category.setId(Integer.parseInt(categoryJSON.getString("id")));
                            category.setName(categoryJSON.getString("name"));
                            categories.add(category);
                        }
                        adapter.notifyDataSetChanged();
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
