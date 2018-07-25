package com.example.fcm_demo;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFcmInstanceIdService extends FirebaseInstanceIdService
{
    @Override
    public void onTokenRefresh()
    {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.e( "nith", "refresh token:" + refreshedToken );
    }
}
