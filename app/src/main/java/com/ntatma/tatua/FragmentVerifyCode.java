package com.tech.blue.flame;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by luqi on 3/17/17.
 */

public class FragmentVerifyCode extends Fragment {

    public static final String TAG = "FragmentVerifyCode";
    Button btnSendCode;
    EditText editTextCode;

    FragmentVerifyCodeListener fragmentVerifyCodeListener;

    public interface FragmentVerifyCodeListener{
        void onBtnSendCodeClick(String code);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fragmentVerifyCodeListener = (FragmentVerifyCodeListener)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentVerifyCodeListener = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_verify_code, container, false);

        btnSendCode = (Button) root.findViewById(R.id.btnVerify);
        editTextCode = (EditText) root.findViewById(R.id.editTextCode);

        btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = editTextCode.getText().toString();
                Log.d(TAG, "Verification code:: "+code +" received");
                fragmentVerifyCodeListener.onBtnSendCodeClick(code);
            }
        });

        return root;
    }
}
