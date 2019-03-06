package com.tolka.p2p_service.server;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.tolka.p2p_service.R;

public class ServerActivity extends AppCompatActivity
{

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_server );
        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );
    }
}
