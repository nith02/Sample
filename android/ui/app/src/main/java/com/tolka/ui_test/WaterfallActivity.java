package com.tolka.ui_test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

public class WaterfallActivity extends AppCompatActivity
{
    private RecyclerView mRvList;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_waterfall );

        StaggeredGridLayoutManager mgr = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRvList = findViewById( R.id.recycler );
        mRvList.setLayoutManager( mgr );
        mRvList.setAdapter( new WaterfallAdapter() );
    }
}
