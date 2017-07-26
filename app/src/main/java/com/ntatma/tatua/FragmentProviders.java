package com.ntatma.tatua;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * Created by luqi on 7/16/17.
 */

public class FragmentProviders extends Fragment implements OnMapReadyCallback{

    public static String TAG = "FragmentProviders";

    private GoogleMap mGoogleMap;
    private List<LatLng> availableProviders;
    double lat, longt;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_providers, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_provider);
        mapFragment.getMapAsync(this);
        availableProviders = (List<LatLng>) getArguments().getSerializable("providers");
        lat = getArguments().getDouble("tstlat");
        longt = getArguments().getDouble("tstLong");
        Log.d(TAG, "Fetched some values : \n"+availableProviders.toString());

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
        LatLng ll = new LatLng(availableProviders.get(0).latitude, availableProviders.get(0).longitude);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 15);
        googleMap.moveCamera(update);
        for (int i =0 ; i< availableProviders.size() ; i++){
            Log.d(TAG, "Adding a marker");
            googleMap.addMarker(new MarkerOptions().title("prov")
                    .position(new LatLng(availableProviders.get(i).latitude, availableProviders.get(i).longitude)));
        }

    }
}
