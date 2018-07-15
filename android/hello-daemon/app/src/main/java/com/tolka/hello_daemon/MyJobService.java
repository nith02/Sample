package com.tolka.hello_daemon;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import static com.tolka.hello_daemon.Util.sleep;

public class MyJobService extends JobService
{
    public MyJobService()
    {
    }

    @Override
    public boolean onStartJob( JobParameters jobParameters )
    {
        new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                while ( true )
                {
                    Log.e( "nith", "JobService alive" );
                    sleep( 2000 );
                }
            }
        } ).start();

        return false;
    }

    @Override
    public boolean onStopJob( JobParameters jobParameters )
    {
        Log.e( "nith", "JobService stop" );
        return false;
    }
}
