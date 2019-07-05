package com.tolka.p2p_service.server;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.tolka.p2p_service.P2pManager;
import com.tolka.p2p_service.R;

import java.util.List;

public class ServerActivity extends AppCompatActivity
{
    private static final String TAG = "ServerActivity";
    private static final int SERVER_PORT = 8080;
    private P2pManager mP2pManager;

    P2pManager.Listener mListener = new P2pManager.Listener()
    {
        @Override
        public void onP2pScanResult( List<WifiP2pDevice> peers )
        {
        }

        @Override
        public void onP2pConnecting()
        {
        }

        @Override
        public void onStart( String name )
        {
            Log.e( TAG, "p2p start " + name );
        }
    };

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_server );
        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        mP2pManager = new P2pManager( this );
        mP2pManager.open( mListener );
        mP2pManager.start();
        mP2pManager.registerService( SERVER_PORT );
    }

    @Override
    protected void onDestroy()
    {
        mP2pManager.stop();
        mP2pManager.close();
        super.onDestroy();
    }



}
