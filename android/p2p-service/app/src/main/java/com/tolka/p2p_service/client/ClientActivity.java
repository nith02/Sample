package com.tolka.p2p_service.client;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.tolka.p2p_service.P2pManager;
import com.tolka.p2p_service.R;

public class ClientActivity extends AppCompatActivity
{
    private static final String TAG = "ClientActivity";
    private P2pManager mP2pManager;


    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_client );
        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        mP2pManager = new P2pManager( this );
        mP2pManager.open( null );
        mP2pManager.discoverService();
    }

}
