package com.tolka.ui_test;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;

public class CoordinatorLayoutActivity extends AppCompatActivity
{

    private RecyclerView mRvList;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView( R.layout.activity_coordinator );
        Toolbar toobar = findViewById( R.id.toolbar );
        setSupportActionBar( toobar );

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

        StaggeredGridLayoutManager mgr = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRvList = findViewById( R.id.recycler );
        mRvList.setLayoutManager( mgr );
        mRvList.setAdapter( new WaterfallAdapter() );
    }
}
