package com.tolka.app_usage;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 1101;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        FloatingActionButton fab = findViewById( R.id.fab );
        fab.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                getAppUsage();
            }
        } );
    }

    private void getAppUsage()
    {
        ActivityManager mActivityManager = (ActivityManager) getSystemService( Context.ACTIVITY_SERVICE );
        List<ActivityManager.RunningAppProcessInfo> process = mActivityManager.getRunningAppProcesses();
        for ( ActivityManager.RunningAppProcessInfo info : process )
        {
            Log.i( "nith", "running process " + info.processName );
        }

        if ( !hasPermission() )
        {
            startActivityForResult(
                    new Intent( Settings.ACTION_USAGE_ACCESS_SETTINGS ),
                    MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS );
            return;
        }

        getTopApp( this );
    }

    private boolean hasPermission()
    {
        AppOpsManager appOps = (AppOpsManager) getSystemService( Context.APP_OPS_SERVICE );
        int mode = appOps.checkOpNoThrow( AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName() );
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void getTopApp( Context context )
    {
        UsageStatsManager manager = (UsageStatsManager) context.getSystemService( Context.USAGE_STATS_SERVICE );
        if ( manager != null )
        {
            long now = System.currentTimeMillis();
            List<UsageStats> stats = manager.queryUsageStats( UsageStatsManager.INTERVAL_BEST, now - 60 * 1000, now );
            Log.i( "nith", "Running app number in last 60 seconds : " + stats.size() );

            String topActivity = "";
            if ( !stats.isEmpty() )
            {
                int j = 0;
                for ( int i = 0; i < stats.size(); i++ )
                {
                    if ( stats.get( i ).getPackageName().contains( "team" ) )
                    {
                        Log.i( "nith", "running app " + stats.get( i ).getPackageName() + " last time used " + stats.get( i ).getLastTimeUsed() );
                        Log.i( "nith", "running app " + stats.get( i ).getPackageName() + " last timestamp " + stats.get( i ).getLastTimeStamp() );
                        Log.i( "nith", "running app " + stats.get( i ).getPackageName() + " total time in foreground " + stats.get( i ).getTotalTimeInForeground() );
                    }

                    if ( stats.get( i ).getLastTimeUsed() > stats.get( j ).getLastTimeUsed() )
                    {
                        j = i;
                    }
                }
                topActivity = stats.get( j ).getPackageName();
            }
            Log.i( "nith", "Top running app is : " + topActivity );
        }
    }

}
