package com.tech.blue.flame;

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

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.PushNotificationTask;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.account.user.UserLoginTask;
import com.applozic.mobicomkit.uiwidgets.ApplozicSetting;

/**
 * Created by luqi on 2/11/17.
 */

public class FragmentUserDetails extends Fragment {
    public static final String TAG = "FragmentUserDetails";

    Utils mUtils;
    //todo handle this context stuff
    Context context;

    EditText txtUserName;
    ImageView imgProfile;
    Button btnContinue;

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
        txtUserName = (EditText)root.findViewById(R.id.txt_username);
        imgProfile = (ImageView)root.findViewById(R.id.img_profilePic);
        btnContinue = (Button)root.findViewById(R.id.btn_continue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = txtUserName.getText().toString();
                //check non null or empty name
                loginToApp(userName);
            }
        });

        return root;
    }

    private void loginToApp(String name){
        Log.d(TAG, "In loginToApp method");
        String number = mUtils.getFromPreferences(Utils.USER_NUMBER);
        UserLoginTask.TaskListener listener = getTaskListener();

        User user = new User();
        user.setUserId(number); //userId it can be any unique user identifier
        user.setDisplayName(name); //displayName is the name of the user which will be shown in chat messages
        //user.setEmail(email); //optional
        user.setAuthenticationTypeId(User.AuthenticationType.APPLOZIC.getValue());  //User.AuthenticationType.APPLOZIC.getValue() for password verification from Applozic server and User.AuthenticationType.CLIENT.getValue() for access Token verification from your server set access token as password
        user.setPassword(""); //optional, leave it blank for testing purpose, read this if you want to add additional security by verifying password from your server https://www.applozic.com/docs/configuration.html#access-token-url
        user.setImageLink("");//optional,pass your image link
        new UserLoginTask(user, listener, context).execute((Void) null);
    }

    private UserLoginTask.TaskListener getTaskListener() {
        return new UserLoginTask.TaskListener() {

            @Override
            public void onSuccess(RegistrationResponse registrationResponse, Context context) {
                Log.d(TAG, "In method UserLoginTask.TaskListener#onsuccess() regRespoonse:::"+ registrationResponse.toString());

                //After successful registration with Applozic server the callback will come here

                if(MobiComUserPreference.getInstance(context).isRegistered()) {

                    PushNotificationTask pushNotificationTask = null;
                    PushNotificationTask.TaskListener listener = new PushNotificationTask.TaskListener() {
                        @Override
                        public void onSuccess(RegistrationResponse registrationResponse) {

                        }
                        @Override
                        public void onFailure(RegistrationResponse registrationResponse, Exception exception) {

                        }

                    };

                    Log.d(TAG, "In method UserLoginTask.TaskListener#onsuccess() creating push notification task");
                    pushNotificationTask = new PushNotificationTask(mUtils.getFromPreferences(Utils.PROPERTY_REG_ID), listener, context);
                    pushNotificationTask.execute((Void) null);
                }

                ApplozicClient.getInstance(context).hideChatListOnNotification();

                ApplozicSetting.getInstance(context).enableRegisteredUsersContactCall();//To enable the applozic Registered Users Contact Note:for disable that you can comment this line of code
                ApplozicSetting.getInstance(context).showStartNewButton();//To show contact list.
                ApplozicSetting.getInstance(context).showStartNewGroupButton();//To enable group messaging

                ApplozicSetting.getInstance(context).showStartNewFloatingActionButton();
                ApplozicSetting.getInstance(context).setHideGroupAddButton(true);

                Intent intent = new Intent(context, MainActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putString("uName", mUtils.getFromPreferences(Utils.UserName));
//                intent.putExtra("Extras",bundle);
                getActivity().startActivity(intent);
                getActivity().finish();

            }

            @Override
            public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                //If any failure in registration the callback  will come here
            }};
    }

}
