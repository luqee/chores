package com.ntatma.tatua;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.nexmo.sdk.NexmoClient;
import com.nexmo.sdk.core.client.ClientBuilderException;
import com.nexmo.sdk.verify.client.VerifyClient;
import com.nexmo.sdk.verify.event.UserObject;
import com.nexmo.sdk.verify.event.VerifyClientListener;
import com.nexmo.sdk.verify.event.VerifyError;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SplashActivity  extends AppCompatActivity
        implements FragmentVerify.FragmentVerifyListener, FragmentVerifyCode.FragmentVerifyCodeListener, FragmentUserDetails.FragmentUserDetailsListener {

    public static String TAG = "SplashActivity";
    Context mContext;
    Utils utils;

    private NexmoClient nexmoClient;
    private VerifyClient verifyClient;
    private ProgressBar progressBarSplash;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        Log.d(TAG, "Entering onCreate method of Splash Activity" );
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();
        utils = new Utils(mContext);
        setContentView(R.layout.activity_splash);
        progressBarSplash =(ProgressBar) findViewById(R.id.progress_splash);

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
                    .commit();
        }else {
            lauchApp();
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
                nameValuePairs.put("name", utils.getFromPreferences(Utils.USER_NAME));
                nameValuePairs.put("number", utils.getFromPreferences(Utils.USER_NUMBER));
                nameValuePairs.put("registered_as", utils.getFromPreferences(Utils.LOGED_IN_AS));
                Log.d(TAG, "create namevalue pairs");
                JSONObject jsonObject = parser.makeHttpRequest(utils.getCurrentIPAddress() +"tatua/api/v1.0/auth/register", "GET",nameValuePairs);
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
                        lauchApp();

                    }else if (response.equals("error")){
                        Log.d(TAG, "Error in registration");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute(null, null, null);
    }

    private void lauchApp(){
        Log.d(TAG, "Launching main activity");
        Intent mainActivityIntent = new Intent(mContext, MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainActivityIntent);
    }
}
