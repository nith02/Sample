package com.tolka.ui_test;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

public class ConstraintLayoutActivity extends AppCompatActivity
{
    private boolean mFullscreen = true;
    private View mView;
    private Toolbar mToobar;
    private AutoCompleteTextView mAutoText;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_NO_TITLE);
        setContentView( R.layout.activity_constraint );
        mToobar = findViewById( R.id.toolbar );
        setSupportActionBar( mToobar );

        mView = findViewById( R.id.text6 );

        {
            Button btn = findViewById( R.id.btnFullscreen );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    if ( mFullscreen )
                    {
                        getWindow().clearFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN );
                    }
                    else
                    {
                        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                WindowManager.LayoutParams.FLAG_FULLSCREEN );
                    }
                    mFullscreen = !mFullscreen;
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btnShowHide );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    if ( mView.getVisibility() == View.VISIBLE )
                    {
                        mView.setVisibility( View.GONE );
                    }
                    else
                    {
                        mView.setVisibility( View.VISIBLE );
                    }
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btnToolbar );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    if ( mToobar.getVisibility() == View.VISIBLE )
                    {
                        mToobar.setVisibility( View.GONE );
                    }
                    else
                    {
                        mToobar.setVisibility( View.VISIBLE );
                    }
                }
            } );
        }
        {
            FloatingActionButton btn = findViewById( R.id.btnFloat );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    Snackbar.make(view,
                            "click me",
                            Snackbar.LENGTH_LONG)
                            .show();
                }
            } );
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        mAutoText = findViewById( R.id.text_autocomplete );
        mAutoText.setAdapter( adapter );
    }

    private static final String[] COUNTRIES = new String[] {
            "Belgium", "France", "Italy", "Germany", "Spain"
    };
}
