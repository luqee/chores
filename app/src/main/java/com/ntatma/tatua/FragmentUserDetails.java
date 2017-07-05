package com.ntatma.tatua;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class FragmentUserDetails extends Fragment {
    public static final String TAG = "FragmentUserDetails";

    FragmentUserDetailsListener fragmentUserDetailsListener;

    public interface FragmentUserDetailsListener{
        void onBtnRegisterClicked();
    }

    Utils mUtils;
    //todo handle this context stuff
    Context context;

    EditText txtUserName;
    Button btnRegister;
    RadioGroup radioOptionGroup;
    RadioButton radioOptionsButton;

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
        final View root = inflater.inflate(R.layout.fragment_userdetails, container, false);
        txtUserName = (EditText)root.findViewById(R.id.txt_username);
        radioOptionGroup = (RadioGroup) root.findViewById(R.id.radio_options) ;
        btnRegister = (Button)root.findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get selected radio button from radioGroup
                int selectedId = radioOptionGroup.getCheckedRadioButtonId();
                // find the radiobutton by returned id
                radioOptionsButton = (RadioButton) root.findViewById(selectedId);
                mUtils.savePreferences(Utils.LOGED_IN_AS, radioOptionsButton.getText().toString());
                fragmentUserDetailsListener.onBtnRegisterClicked();
            }
        });

        return root;
    }
}
