package com.tolka.screen_cast;

import android.content.Context;
import android.media.MediaRouter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        FloatingActionButton fab = findViewById( R.id.fab );
        fab.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                Log.i( "nith", "isScreenCasting " + isScreenCasting( MainActivity.this ) );
            }
        } );
    }

    private boolean isScreenCasting( Context context )
    {
        MediaRouter mediaRouter = (MediaRouter) context.getSystemService( Context.MEDIA_ROUTER_SERVICE );
        MediaRouter.RouteInfo info = mediaRouter.getDefaultRoute();
        return info.getPresentationDisplay() != null;
    }

}
