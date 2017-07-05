package com.ntatma.tatua;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;


public class FragmentSplash extends Fragment {

    public static final String TAG = "FragmentSplash";
    private static final int SPLASH_SHOW_TIME = 5000;

    private ProgressBar mRegistrationProgressBar;
    ImageView imageView;

    Utils utils;
    FragmentSplashListener fragmentSplashListener;

    public interface FragmentSplashListener{
        void putVerifyView();
        void putUserDetailsView();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        utils = new Utils(this.getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_splash, container, false);
        mRegistrationProgressBar = (ProgressBar) root.findViewById(R.id.ProgressRegistration);
        imageView = (ImageView) root.findViewById(R.id.imgSplashBG);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "In onActivityCreated, checking if user exists & is registered ");

        if (utils.getFromPreferences(Utils.IS_NUM_VERIFIED) == ""){
            Log.d(TAG, "calling listener method : putVerifyView()");
            fragmentSplashListener.putVerifyView();
        }else if (utils.getFromPreferences(Utils.IS_USER_REGISTRED)== "" ) {
            Log.d(TAG, "getFromPreferences(Utils.USER_NAME) returned empty string ");
            Log.d(TAG, "calling listener method : putUserDetailsView()");
            fragmentSplashListener.putUserDetailsView();
        }else {
            Log.d(TAG, "User details good, Login to app");
            loginToApp(utils.getFromPreferences(Utils.USER_NAME), utils.getFromPreferences(Utils.USER_NUMBER));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fragmentSplashListener = (FragmentSplashListener)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentSplashListener = null;
    }

}