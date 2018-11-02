package com.tolka.screenshot;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity
{
    private static int MY_PERMISSIONS_REQUEST_STORAGE = 1011;

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
                takeScreenshot();
            }
        } );
    }

    void requestPermission()
    {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_STORAGE );
    }

    private void takeScreenshot()
    {
        {
            Bitmap bitmap = screenshot1();
            String strPath = Environment.getExternalStorageDirectory().toString() + "/screenshot1.jpg";
            writeBitmap( bitmap, strPath );
        }
        {
            Bitmap bitmap = screenshot2();
            String strPath = Environment.getExternalStorageDirectory().toString() + "/screenshot2.jpg";
            writeBitmap( bitmap, strPath );
        }
    }

    public Bitmap screenshot1()
    {
        View view = getWindow().getDecorView().getRootView();
        view.setDrawingCacheEnabled( true );
        Bitmap bitmap = Bitmap.createBitmap( view.getDrawingCache() );
        view.setDrawingCacheEnabled( false );

        return bitmap;
    }

    public Bitmap screenshot2()
    {
        View view = getWindow().getDecorView();
        Bitmap bitmap = Bitmap.createBitmap( view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888 );
        Canvas canvas = new Canvas( bitmap );
        view.draw( canvas );

        return bitmap;
    }

    private void writeBitmap( Bitmap bitmap, String strPath )
    {
        try
        {
            File imageFile = new File( strPath );
            FileOutputStream outputStream = new FileOutputStream( imageFile );
            int quality = 100;
            bitmap.compress( Bitmap.CompressFormat.JPEG, quality, outputStream );
            outputStream.flush();
            outputStream.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

}
