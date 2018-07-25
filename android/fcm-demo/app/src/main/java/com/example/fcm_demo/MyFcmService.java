package com.example.fcm_demo;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFcmService extends FirebaseMessagingService
{
    @Override
    public void onMessageReceived( RemoteMessage remoteMessage )
    {
        Log.e( "nith", "onMessageReceived: " + remoteMessage.toString() );

        Intent intent = new Intent( this, MainActivity.class );
        startActivity( intent );
    }
}
