package com.tolka.ui_test;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SwipeRefreshLayoutActivity extends AppCompatActivity
{

    private SwipeRefreshLayout mSwipeRefresh;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_swipe_refresh );
        
        mSwipeRefresh = findViewById( R.id.refresh );
        mSwipeRefresh.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                mSwipeRefresh.setRefreshing( false );
            }
        } );
    }
}
