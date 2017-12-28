package com.luqi.chores;

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

public class FragmentUserDetails extends Fragment {
    public static final String TAG = "FragmentUserDetails";

    FragmentUserDetailsListener fragmentUserDetailsListener;

    public interface FragmentUserDetailsListener{
        void onBtnRegisterClicked(String username);
    }

    Utils mUtils;
    //todo handle this context stuff
    Context context;

    EditText txtUserName;
    Button btnRegister;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fragmentUserDetailsListener = (FragmentUserDetailsListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentUserDetailsListener = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "In onCreate method");
        super.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();
        mUtils = new Utils(context);
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "In onCreateView method");
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_userdetails, container, false);
        txtUserName = (EditText)root.findViewById(R.id.edit_username);
        btnRegister = (Button)root.findViewById(R.id.btn_register);
        Log.d(TAG, "In onCreateView method >> setting up button listener");
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Button Register Clicked");
                mUtils.savePreferences(Utils.USER_NAME, txtUserName.getText().toString());
                fragmentUserDetailsListener.onBtnRegisterClicked(txtUserName.getText().toString());
            }
        });

        return root;
    }
}
