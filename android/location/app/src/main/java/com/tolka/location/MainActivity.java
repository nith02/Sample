package com.tolka.location;

import android.Manifest;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    private static int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1011;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        requestPermission();

        FloatingActionButton fab = findViewById( R.id.fab );
        fab.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                Location location = getLastKnownLocation( MainActivity.this );
                String strAddress = getAddressFromLocation( MainActivity.this, location );
                Log.i( "nith", "Address " + strAddress );
            }
        } );
    }

    void requestPermission()
    {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_READ_CONTACTS );
    }

    public static Location getLastKnownLocation( Context context )
    {
        LocationManager LocationManager = (LocationManager) context.getSystemService( LOCATION_SERVICE );
        List<String> providers = LocationManager.getProviders( true );
        Location bestLocation = null;
        for ( String provider : providers )
        {
            Location l = null;
            try
            {
                l = LocationManager.getLastKnownLocation( provider );
            }
            catch ( SecurityException ignored )
            {
            }
            if ( l == null )
            {
                continue;
            }
            if ( bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy() )
            {
                // Found best last known location
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    public static String getAddressFromLocation( Context context, Location location )
    {
        if ( location == null )
        {
            return null;
        }

        Geocoder geo = new Geocoder( context, Locale.US );
        try
        {
            List<Address> list = geo.getFromLocation( location.getLatitude(), location.getLongitude(), 1 );
            if ( list.size() > 0 )
            {
                Address address = list.get( 0 );
                return address.getAddressLine( 0 );
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        return null;
    }

}
