package com.tolka.hello_daemon;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import static com.tolka.hello_daemon.Util.sleep;

public class MyService extends Service
{
    public MyService()
    {
    }

    @Nullable
    @Override
    public IBinder onBind( Intent intent )
    {
        return null;
    }

    @Override
    public int onStartCommand(
            Intent intent, int flags, int startId )
    {
        new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                while ( true )
                {
                    Log.e( "nith", "MyService alive" );
                    sleep( 2000 );
                }
            }
        } ).start();

        return super.onStartCommand( intent, flags, startId );
    }
}
