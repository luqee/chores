package com.ntatma.tatua;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.nexmo.sdk.NexmoClient;
import com.nexmo.sdk.core.client.ClientBuilderException;
import com.nexmo.sdk.verify.client.VerifyClient;
import com.nexmo.sdk.verify.event.UserObject;
import com.nexmo.sdk.verify.event.VerifyClientListener;
import com.nexmo.sdk.verify.event.VerifyError;

import java.io.IOException;


public class FragmentVerify extends Fragment {

    public static final String TAG = "FragmentVerify";

    FragmentVerifyListener fragmentVerifyListener;

    public interface FragmentVerifyListener{
        void onBtnVerifyClicked(String number);
    }

    Utils mUtils;
    Context mContext;

    EditText txtPhoneNo;
    Button btnVerify;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fragmentVerifyListener = (FragmentVerifyListener)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentVerifyListener = null;
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "In onCreate method");
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();
        mUtils = new Utils(mContext);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "In onCreateView method");
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_verify, container, false);
        txtPhoneNo = (EditText) root.findViewById(R.id.editTxtPhoneNo);
        btnVerify = (Button)root.findViewById(R.id.btnVerify);
//        btnVerify.setVisibility(View.INVISIBLE);
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNo = txtPhoneNo.getText().toString();
                Log.d(TAG, "Btn verify clicked");
                fragmentVerifyListener.onBtnVerifyClicked(phoneNo);

            }
        });
        return root;
    }

}
