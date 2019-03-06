package com.tolka.p2p_service;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.tolka.p2p_service.client.ClientActivity;
import com.tolka.p2p_service.server.ServerActivity;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        Button btnServer = findViewById( R.id.btn_server );
        btnServer.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                enterActivity( ServerActivity.class );
            }
        } );

        Button btnClient = findViewById( R.id.btn_client );
        btnClient.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                enterActivity( ClientActivity.class );
            }
        } );

    }

    private void enterActivity( Class clazz )
    {
        Intent intent = new Intent( this, clazz );
        startActivity( intent );
    }
}
