package com.tolka.check_signature;

import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        try
        {
            Signature[] sigs;
            sigs = this.getPackageManager()
                    .getPackageInfo( this.getPackageName(), PackageManager.GET_SIGNATURES )
                    .signatures;
            Log.e( "nith", "sig = " + sigs.hashCode() );
        }
        catch ( PackageManager.NameNotFoundException e )
        {
            e.printStackTrace();
        }
    }
}
