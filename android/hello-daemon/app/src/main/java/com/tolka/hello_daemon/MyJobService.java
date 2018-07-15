package com.tolka.hello_daemon;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;

public class MyJobService extends JobService
{
    public MyJobService()
    {
    }

    @Override
    public boolean onStartJob( JobParameters jobParameters )
    {
        Log.e( "nith", "onStartJob" );

        if ( !Util.isServiceRunning( this, MyService.class ) )
        {
            Intent intent = new Intent( this, MyService.class );
            startService( intent );
        }

        return false;
    }

    @Override
    public boolean onStopJob( JobParameters jobParameters )
    {
        Log.e( "nith", "JobService stop" );
        return false;
    }
}
