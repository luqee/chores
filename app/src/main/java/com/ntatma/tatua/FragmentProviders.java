package com.ntatma.tatua;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by luqi on 7/16/17.
 */

public class FragmentProviders extends Fragment implements OnMapReadyCallback{

    public static String TAG = "FragmentProviders";

    private FragmentProvidersListener fragmentProvidersListener;
    private GoogleMap mGoogleMap;
    private List<LatLng> availableProviders;
    double lat, longt;
    Button btnMakeRequest;
    String mProviderCategory;
    Utils mUtils;

    public interface FragmentProvidersListener{
        void onrequestProviderClick(String cat);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fragmentProvidersListener = (FragmentProvidersListener)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentProvidersListener = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root =  inflater.inflate(R.layout.fragment_providers, container, false);
        btnMakeRequest = (Button) root.findViewById(R.id.buttonRequest);
        availableProviders = (List<LatLng>) getArguments().getSerializable("providers");
        mUtils = new Utils(getActivity());
        mProviderCategory = getArguments().getString("provCategory");
        btnMakeRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentProvidersListener.onrequestProviderClick(mProviderCategory);
            }
        });
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_provider);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        displayMap();
    }

    public void displayMap(){
        LatLng ll = new LatLng(Double.parseDouble(mUtils.getFromPreferences(Utils.USER_LATITUDE)), Double.parseDouble(mUtils.getFromPreferences(Utils.USER_LONGITUDE)));
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 15);
        mGoogleMap.moveCamera(update);
        mGoogleMap.addMarker(new MarkerOptions().title("Me")
                .position(ll));
        for (int i =0 ; i< availableProviders.size() ; i++){
            Log.d(TAG, "Adding a marker");
            mGoogleMap.addMarker(new MarkerOptions().title(mProviderCategory)
                    .position(new LatLng(availableProviders.get(i).latitude, availableProviders.get(i).longitude)));
        }
    }

    public void requestProvider(){
        new AsyncTask<String, Void, JSONObject>(){

            @Override
            protected JSONObject doInBackground(String... params) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("userId", mUtils.getFromPreferences(Utils.USER_NUMBER));
                hashMap.put("provCat", mProviderCategory);
                JSONParser parser = new JSONParser();
                return parser.makeHttpRequest(mUtils.getCurrentIPAddress()+"tatua/api/v1.0/users/request/", "GET", hashMap);
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
                //// TODO: 8/25/17 show engaged to 'provider' page
                try {
                    JSONObject userJSON = jsonObject.getJSONObject("activated_user");
                    String activeProviderName = userJSON.getString("name");
                    String activeProviderNumber = userJSON.getString("number");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute(mProviderCategory);
    }
}
