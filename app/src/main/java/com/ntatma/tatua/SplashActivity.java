package com.ntatma.tatua;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.nexmo.sdk.NexmoClient;
import com.nexmo.sdk.core.client.ClientBuilderException;
import com.nexmo.sdk.verify.client.VerifyClient;
import com.nexmo.sdk.verify.event.UserObject;
import com.nexmo.sdk.verify.event.VerifyClientListener;
import com.nexmo.sdk.verify.event.VerifyError;

import java.io.IOException;


public class SplashActivity  extends AppCompatActivity
        implements FragmentSplash.FragmentSplashListener, FragmentVerify.FragmentVerifyListener, FragmentVerifyCode.FragmentVerifyCodeListener {

    public static String TAG = "SplashActivity";
    Context mContext;
    Utils utils;

    private NexmoClient nexmoClient;
    private VerifyClient verifyClient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        Log.d(TAG, "Entering onCreate method of Splash Activity" );
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();
        utils = new Utils(mContext);
        if (utils.getFromPreferences(Utils.IS_NUM_VERIFIED) == ""){
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
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_frame ,fragmentVerify, FragmentVerify.TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .addToBackStack(FragmentVerify.TAG)
                    .commit();
//            setContentView(R.layout.activity_splash);
//            Log.d(TAG, "After setting content view, Now setting Fragment splash.");
//            Fragment fragmentSplash = new FragmentSplash();
//            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//            fragmentTransaction.add(R.id.content_frame, fragmentSplash, FragmentSplash.TAG);
//            fragmentTransaction.addToBackStack(FragmentSplash.TAG);
//            fragmentTransaction.commit();
        }
    }

    @Override
    public void putVerifyView() {


    }

    @Override
    public void onBtnVerifyClicked(String number) {
        //show progrees bar
        Log.d(TAG, "Initialize Verify Client");
        verifyClient = new VerifyClient(nexmoClient);
        verifyClient.addVerifyListener(new VerifyClientListener() {
            @Override
            public void onVerifyInProgress(VerifyClient verifyClient, UserObject user) {
                Log.d(TAG, "onVerifyInProgress for number: " + user.getPhoneNumber());

                Log.d(TAG, "pop back stack");
                getSupportFragmentManager().popBackStack();
                Fragment verifyCodeFragment = new FragmentVerifyCode();
                Log.d(TAG, "Launching fragment");
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.content_frame, verifyCodeFragment, FragmentVerifyCode.TAG)
                        .commit();
            }

            @Override
            public void onUserVerified(VerifyClient verifyClient, UserObject user) {
                Log.d(TAG, "onUserVerified for number: " + user.getPhoneNumber());
                utils.savePreferences(Utils.USER_NUMBER, user.getPhoneNumber());
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
    public void putUserDetailsView() {
        Log.d(TAG, "SplashActivity#putUserDetailsView().. starting UserDetails fragment");
        Fragment userDetailsFragment = new FragmentUserDetails();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.content_frame, userDetailsFragment, FragmentUserDetails.TAG)
                .commit();
    }

    @Override
    public void onBtnSendCodeClick(String code) {
        verifyClient.checkPinCode(code);
    }
}
