package com.tolka.hello_daemon;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

public class Util
{
    public static void scheduleJob( Context context )
    {
        ComponentName serviceComponent = new ComponentName( context, MyJobService.class );
        JobInfo.Builder builder = new JobInfo.Builder( 0, serviceComponent );
        builder.setMinimumLatency( 1 * 1000 ); // wait at least
        builder.setOverrideDeadline( 3 * 1000 ); // maximum delay
        //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = context.getSystemService( JobScheduler.class );
        jobScheduler.schedule( builder.build() );
    }

    public static void sleep( int ms )
    {
        try
        {
            Thread.sleep( ms );
        }
        catch ( InterruptedException e )
        {
            e.printStackTrace();
        }
    }
}
