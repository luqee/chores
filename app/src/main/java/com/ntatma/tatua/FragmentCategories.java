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
import java.util.HashMap;
import java.util.List;

/**
 * Created by luqi on 7/8/17.
 */

public class FragmentCategories extends Fragment {

    private FragmentCategoriesListener fragmentCategoriesListener;

    public static String TAG = "FragmentCategories";

    Context mContext;
    Utils utils;
    List<Category> categories = new ArrayList<>();
    CategoriesAdapter adapter;
    RecyclerView recyclerView;

    public interface FragmentCategoriesListener {
        void onCategoryClick(String name);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();
        utils = new Utils(mContext);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fragmentCategoriesListener = (FragmentCategoriesListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentCategoriesListener = null;
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
                fragmentCategoriesListener.onCategoryClick(name);
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        getCategories();
    }

    public void getCategories(){

        new AsyncTask<Void, Void, JSONObject>(){

            @Override
            protected JSONObject doInBackground(Void... args) {
                HashMap<String, String> params = new HashMap<>();
                Log.d(TAG, "Fetching Categories to display");
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = parser.makeHttpRequest(utils.getCurrentIPAddress() +"tatua/api/v1.0/categories","GET", params);
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
                        Log.d(TAG, "Error in gwetting categories");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute(null, null, null);
    }


}
