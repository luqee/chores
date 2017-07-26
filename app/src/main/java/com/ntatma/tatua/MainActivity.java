package com.ntatma.tatua;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
                    GoogleApiClient.OnConnectionFailedListener, FragmentCategories.FragmentCategoriesListener{

    public static String TAG = "MainActivity";
    Utils utils;
    Context mContext;
    Location mCurrentLocation;

    GoogleApiClient googleApiClient;
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;
    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        utils = new Utils(mContext);
        setContentView(R.layout.activity_main);
        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        Log.d(TAG, "After setting content view and GoogleApiClient, Now setting Fragment categories.");
        Fragment fragmentCategories = new FragmentCategories();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_content_frame, fragmentCategories, FragmentCategories.TAG);
        fragmentTransaction.addToBackStack(FragmentCategories.TAG);
        fragmentTransaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (mCurrentLocation != null) {
            String latt = (String.valueOf(mCurrentLocation.getLatitude()));
            String longtd = (String.valueOf(mCurrentLocation.getLongitude()));
            Log.d(TAG, "lattlong : " + mCurrentLocation.toString());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (connectionResult.hasResolution()) {
            try {
                mResolvingError = true;
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                googleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(connectionResult.getErrorCode());
            mResolvingError = true;
        }
    }

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    @Override
    public void onCategoryClick(String name) {
        getProviders(name);
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MainActivity)getActivity()).onDialogDismissed();
        }
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!googleApiClient.isConnecting() &&
                        !googleApiClient.isConnected()) {
                    googleApiClient.connect();
                }
            }
        }
    }

    public void getProviders(String catName){
        new AsyncTask<String, Void, JSONObject>(){

            @Override
            protected JSONObject doInBackground(String... args) {
                Log.d(TAG, "Preparing to get providers");
                HashMap<String, String> params = new HashMap<>();
                JSONParser parser = new JSONParser();
                params.put("registered_as", args[0]);
                JSONObject jsonObject = parser.makeHttpRequest(utils.getCurrentIPAddress() +"tatua/api/v1.0/providers/", "GET", params);
                return jsonObject;
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                List<LatLng> latLngList = new ArrayList<>();
                LatLng latLng;
                try {
                    JSONArray providersArray = jsonObject.getJSONArray("providers");
                    for (int i= 0 ; i<providersArray.length() ; i++){

                        double latitude = providersArray.getJSONObject(i).getDouble("lat");
                        double longitude = providersArray.getJSONObject(i).getDouble("long");
                        latLng = new LatLng(latitude, longitude);
                        latLngList.add(latLng);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "Successfully fetched providers.\n" );
                getSupportFragmentManager().popBackStack();
                Fragment fragmentProviders = new FragmentProviders();
                Bundle bundle = new Bundle();
                bundle.putSerializable("providers", (Serializable) latLngList);
                bundle.putDouble("tstLat", mCurrentLocation.getLatitude());
                bundle.putDouble("tstLong", mCurrentLocation.getLongitude());
                fragmentProviders.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.main_content_frame, fragmentProviders, FragmentProviders.TAG);
                fragmentTransaction.addToBackStack(FragmentProviders.TAG);
                fragmentTransaction.commit();
            }
        }.execute(catName);
    }
}
