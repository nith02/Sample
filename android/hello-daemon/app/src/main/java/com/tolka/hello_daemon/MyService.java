package com.tolka.hello_daemon;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
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
            Intent intent, int flags, final int startId )
    {
        new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                while ( true )
                {
                    Log.e( "nith", "MyService alive " + startId );
                    sleep( 2000 );
                }
            }
        } ).start();

        Intent notificationIntent = new Intent( this, MainActivity.class );
        PendingIntent pendingIntent = PendingIntent.getActivity( this, 0,
                notificationIntent, 0 );

        Notification notification = new NotificationCompat.Builder( this )
                .setContentTitle( "My Awesome App" )
                .setContentText( "Doing some work..." )
                .setContentIntent( pendingIntent ).build();

        startForeground( 1, notification );

        return START_NOT_STICKY;
    }
}
