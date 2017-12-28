package com.luqi.chores;

import android.*;
import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.nexmo.sdk.NexmoClient;
import com.nexmo.sdk.core.client.ClientBuilderException;
import com.nexmo.sdk.verify.client.VerifyClient;
import com.nexmo.sdk.verify.event.UserObject;
import com.nexmo.sdk.verify.event.VerifyClientListener;
import com.nexmo.sdk.verify.event.VerifyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class SplashActivity  extends AppCompatActivity
        implements LocationListener,FragmentVerify.FragmentVerifyListener
        ,FragmentCategories.FragmentCategoriesListener,
        FragmentProviders.FragmentProvidersListener,
        FragmentVerifyCode.FragmentVerifyCodeListener, FragmentUserDetails.FragmentUserDetailsListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static String TAG = "SplashActivity";
    Context mContext;
    Utils utils;

    private NexmoClient nexmoClient;
    private VerifyClient verifyClient;
    private ProgressBar progressBarSplash;


    private static final String REQUESTING_LOCATION_UPDATES_KEY = "getting_location_updates";
    private static final String LOCATION_KEY = "location";
    private static final String LAST_UPDATED_TIME_STRING_KEY = "last_update_time";

    Location mCurrentLocation;
    Boolean mRequestingLocationUpdates = Boolean.TRUE;
    String mLastUpdateTime;

    GoogleApiClient googleApiClient;
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;
    private static final String STATE_RESOLVING_ERROR = "resolving_error";


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "On start" );
        if (!mResolvingError) {
            Log.d(TAG, "On start calling googleApiClient.connect()" );
            googleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (googleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);
        outState.putParcelable(LOCATION_KEY, mCurrentLocation);
        outState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        Log.d(TAG, "Entering onCreate method of Splash Activity" );
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();
        utils = new Utils(mContext);
        setContentView(R.layout.activity_splash);
        progressBarSplash =(ProgressBar) findViewById(R.id.progress_splash);
        updateValuesFromBundle(savedInstanceState);
        buildGoogleApiClient();
        createView();
    }

    public void createView(){
        if (utils.getFromPreferences(Utils.IS_NUM_VERIFIED) == ""){
            progressBarSplash.setVisibility(View.VISIBLE);
            Log.d(TAG, "Creating nexmo client");
            try {
                nexmoClient = new NexmoClient.NexmoClientBuilder()
                        .context(mContext)
                        .applicationId(Utils.NexmoAppId)
                        .sharedSecretKey(Utils.NexmoSharedSecretKey)
                        .build();
            }catch (ClientBuilderException ex){
                ex.printStackTrace();
            }

            Log.d(TAG, "SplashActivity#.putVerifyView().. starting verify fragment");
            Fragment fragmentVerify = new FragmentVerify();
            progressBarSplash.setVisibility(View.INVISIBLE);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_frame ,fragmentVerify, FragmentVerify.TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .addToBackStack(FragmentVerify.TAG)
                    .commit();
        }else if (utils.getFromPreferences(Utils.IS_USER_REGISTRED) == ""){
            Fragment userDetailsFragment = new FragmentUserDetails();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_frame, userDetailsFragment, FragmentUserDetails.TAG)
                    .addToBackStack(FragmentUserDetails.TAG)
                    .commit();
        }else {
            Fragment fragmentCategories = new FragmentCategories();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.content_frame, fragmentCategories, FragmentCategories.TAG);
            fragmentTransaction.addToBackStack(FragmentCategories.TAG);
            fragmentTransaction.commit();
        }
    }
    private synchronized void buildGoogleApiClient() {
        Log.d(TAG, "building google api client");
        googleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and
            // make sure that the Start Updates and Stop Updates buttons are
            // correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
//                setButtonsEnabledState();
            }

            // Update the value of mCurrentLocation from the Bundle and update the
            // UI to show the correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocationis not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(
                        LAST_UPDATED_TIME_STRING_KEY);
            }
//            updateUI();
        }
    }

    @Override
    public void onBtnVerifyClicked(String number) {
        getSupportFragmentManager().popBackStack();
        //show progrees bar
        progressBarSplash.setVisibility(View.VISIBLE);
        Log.d(TAG, "Initialize Verify Client");
        verifyClient = new VerifyClient(nexmoClient);
        verifyClient.addVerifyListener(new VerifyClientListener() {
            @Override
            public void onVerifyInProgress(VerifyClient verifyClient, UserObject user) {
                Log.d(TAG, "onVerifyInProgress for number: " + user.getPhoneNumber());
                //hide progress bar
                progressBarSplash.setVisibility(View.INVISIBLE);
                Fragment verifyCodeFragment = new FragmentVerifyCode();
                Log.d(TAG, "Launching fragment");
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.content_frame, verifyCodeFragment, FragmentVerifyCode.TAG)
                        .commit();
            }

            @Override
            public void onUserVerified(VerifyClient verifyClient, UserObject user) {
                Log.d(TAG, "onUserVerified for number: " + user.getPhoneNumber());
                getSupportFragmentManager().popBackStack();
                //hide progress
                progressBarSplash.setVisibility(View.INVISIBLE);
                utils.savePreferences(Utils.USER_NUMBER, user.getPhoneNumber());
                utils.savePreferences(Utils.IS_NUM_VERIFIED, "True");
                Log.d(TAG, "SplashActivity#putUserDetailsView().. starting UserDetails fragment");

                Fragment userDetailsFragment = new FragmentUserDetails();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.content_frame, userDetailsFragment, FragmentUserDetails.TAG)
                        .commit();
            }

            @Override
            public void onError(VerifyClient verifyClient, VerifyError errorCode, UserObject user) {
                Log.d(TAG, "onError: " + errorCode + " for number: " + user.getPhoneNumber());
            }

            @Override
            public void onException(IOException exception) {

            }
        });

        Log.d(TAG, "Calling VerifyClient#getVerifiedUser");
        verifyClient.getVerifiedUser("KE", number);
    }

    @Override
    public void onBtnSendCodeClick(String code) {
        getSupportFragmentManager().popBackStack();
        //show progress bar
        progressBarSplash.setVisibility(View.VISIBLE);
        verifyClient.checkPinCode(code);
    }

    @Override
    public void onBtnRegisterClicked(String uname) {
        Log.d(TAG, "Button Register Clicked");
        utils.savePreferences(Utils.USER_NAME, uname);
        getSupportFragmentManager().popBackStack();
        //send details to server
        progressBarSplash.setVisibility(View.VISIBLE);
        registerWithServer();
    }

    public void registerWithServer(){

        new AsyncTask<Void, Void, JSONObject>(){

            @Override
            protected JSONObject doInBackground(Void... params) {
                Log.d(TAG, "Starting registration");
                HashMap<String, String> nameValuePairs = new HashMap<>();
                JSONParser parser = new JSONParser();
                nameValuePairs.put("username", utils.getFromPreferences(Utils.USER_NAME));
                nameValuePairs.put("number", utils.getFromPreferences(Utils.USER_NUMBER));
                nameValuePairs.put("latitude", utils.getFromPreferences(Utils.USER_LATITUDE));
                nameValuePairs.put("longitude", utils.getFromPreferences(Utils.USER_LONGITUDE));
                nameValuePairs.put("token", utils.getFromPreferences(Utils.PROPERTY_REG_ID));
                Log.d(TAG, "create namevalue pairs");
                JSONObject jsonObject = parser.makeHttpRequest(utils.getCurrentIPAddress() +"tatua/api/v1.0/auth/user/register", "POST",nameValuePairs);
                return jsonObject;
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                try {
                    String response = jsonObject.getString("result");

                    if (response.equals("success")){
                        Log.d(TAG, "Successfull registration");
                        progressBarSplash.setVisibility(View.INVISIBLE);
                        utils.savePreferences(Utils.IS_USER_REGISTRED, "True");
                        utils.savePreferences(Utils.USER_ID, jsonObject.getString("user_id"));
                        getSupportFragmentManager().popBackStack();
                        Fragment fragmentCategories = new FragmentCategories();
                        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.add(R.id.content_frame, fragmentCategories, FragmentCategories.TAG);
                        fragmentTransaction.addToBackStack(FragmentCategories.TAG);
                        fragmentTransaction.commit();

                    }else if (response.equals("error")){
                        Log.d(TAG, "Error in registration");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute(null, null, null);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Google api client on connected" );
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
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (mCurrentLocation != null) {
            Log.d(TAG, "Location object not null ::: " + mCurrentLocation.toString());
//            LocationRequest locationRequest = createLocationRequest();
            utils.savePreferences(Utils.USER_LATITUDE, String.valueOf(mCurrentLocation.getLatitude()));
            utils.savePreferences(Utils.USER_LONGITUDE, String.valueOf(mCurrentLocation.getLongitude()));
        }
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    protected LocationRequest createLocationRequest() {
        Log.d(TAG, "Creating location request" );
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    protected void startLocationUpdates() {
        Log.d(TAG, "Starting location updates" );
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationRequest locationRequest = createLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Google api client on connection failed" );
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
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed" );
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        utils.savePreferences(Utils.USER_LATITUDE, String.valueOf(location.getLatitude()));
        utils.savePreferences(Utils.USER_LONGITUDE, String.valueOf(location.getLongitude()));
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, this);
    }

    @Override
    public void onCategoryClick(String name) {
        getProviders(name);
    }
    public void getProviders(final String catName){
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
                bundle.putString("provCategory",catName);
                fragmentProviders.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.content_frame, fragmentProviders, FragmentProviders.TAG);
                fragmentTransaction.addToBackStack(FragmentProviders.TAG);
                fragmentTransaction.commit();
            }
        }.execute(catName);
    }

    @Override
    public void onrequestProviderClick(final String cat) {
        new AsyncTask<String, Void, JSONObject>(){

            @Override
            protected JSONObject doInBackground(String... params) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("userId", utils.getFromPreferences(Utils.USER_NUMBER));
                hashMap.put("provCat", cat);
                JSONParser parser = new JSONParser();
                return parser.makeHttpRequest(utils.getCurrentIPAddress()+"tatua/api/v1.0/users/request", "GET", hashMap);
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
                //// TODO: 8/25/17 show engaged to 'provider' page

                try {
                    String response = jsonObject.getString("message");
                    Log.d(TAG, "After requesting prov: result is :: "+response);
                    if (response.equals("success")){
                        JSONObject userJSON = jsonObject.getJSONObject("activated_user");
                        utils.savePreferences(Utils.PROVIDER_NAME, userJSON.getString("provider_name"));
                        utils.savePreferences(Utils.PROVIDER_NUMBER, userJSON.getString("provider_number"));
                        utils.savePreferences(Utils.TRANSACTION_ID, userJSON.getString("transaction_id"));

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute(cat);
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
            ((SplashActivity)getActivity()).onDialogDismissed();
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
}
