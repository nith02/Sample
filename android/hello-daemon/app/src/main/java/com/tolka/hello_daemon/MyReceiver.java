package com.tolka.hello_daemon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive( Context context, Intent intent )
    {
        Log.e( "nith", "onReceive " + intent.getAction() );
        Util.scheduleJob( context );
    }
}
