package com.tolka.p2p_service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.tolka.multimediachat.utility.UsbUtility;

public class MyService extends Service
{
    private static final String CHANNEL_WHATEVER="channel_whatever3";
    private static int NOTIFY_ID=1337;
    private static int FOREGROUND_ID=1338;
    private NotificationManager m_manager;
    private NotificationChannel m_notificationChannel;
    private UsbUtility mUsbUtil;

    public MyService()
    {
    }

    @Override
    public IBinder onBind( Intent intent )
    {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException( "Not yet implemented" );
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.e( "RHD", "oncreate" );

        m_manager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        mUsbUtil = new UsbUtility( this, R.xml.device_filter );

        new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                while ( true )
                {
                    Log.e( "RHD", "alive" );
                    try
                    {
                        Thread.sleep( 1000 );
                    }
                    catch ( InterruptedException e )
                    {
                        e.printStackTrace();
                    }

//                    Log.e( "RHD", "get usb permission" );
//                    mUsbUtil.openTargetDevice();
                    break;
                }
            }
        } ).start();

    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId )
    {
        startForegroundService();
        return super.onStartCommand( intent, flags, startId );
    }

    private void startForegroundService()
    {
        Log.e( "RHD", "startForegroundService" );
        {
            NotificationChannel c = new NotificationChannel( CHANNEL_WHATEVER,
                    "Whatever", NotificationManager.IMPORTANCE_DEFAULT );
            m_manager.createNotificationChannel( c );
        }


        Notification.Builder builder =
                new Notification.Builder( this )
                        .setSmallIcon( R.drawable.ic_launcher )
                        .setContentTitle( "My Love" )
                        .setContentText( "Hi, my love!" );

        builder.setChannelId( "nith123" );

        Notification notification = buildForegroundNotification("myaction");

        // Start foreground service.
        startForeground( FOREGROUND_ID, notification );
    }

    private Notification buildForegroundNotification(String filename) {
        NotificationCompat.Builder b=
                new NotificationCompat.Builder(this, CHANNEL_WHATEVER);

        b.setOngoing(true)
                .setContentTitle("downloading")
                .setContentText(filename)
                .setSmallIcon(android.R.drawable.stat_sys_download);

        return(b.build());
    }
}
