package com.tolka.ui_test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        {
            Button btn = findViewById( R.id.btnConstraint );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    startActivity( ConstraintLayoutActivity.class );
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btnWaterfall );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    startActivity( WaterfallActivity.class );
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btnCoordinator );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    startActivity( CoordinatorLayoutActivity.class );
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btnDrawer );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    startActivity( DrawerLayoutActivity.class );
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btnSwipeRefresh );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    startActivity( SwipeRefreshLayoutActivity.class );
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btnBottomNavigate );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    startActivity( BottmNavigateActivity.class );
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btnTabbed );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    startActivity( TabbedActivity.class );
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btnFullscreen );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    startActivity( FullscreenActivity.class );
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btnItemDetail );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    startActivity( ItemListActivity.class );
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btnViewpager );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    startActivity( ViewPagerActivity.class );
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btnBasic );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    startActivity( BasicActivity.class );
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btnLogin );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    startActivity( LoginActivity.class );
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btnSetting );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    startActivity( SettingActivity.class );
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btnProgressbars );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    startActivity( ProgressbarsActivity.class );
                }
            } );
        }
    }

    private void startActivity( Class clazz )
    {
        Intent intent = new Intent( MainActivity.this, clazz );
        startActivity( intent );
    }
}
