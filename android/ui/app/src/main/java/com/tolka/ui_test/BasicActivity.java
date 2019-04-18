package com.tolka.ui_test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Spinner;

public class BasicActivity extends AppCompatActivity implements MyDialogFragment.OnFragmentInteractionListener
{
    Button mButton;
    Button mFullScreenButton;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_basic );

        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        mButton = findViewById( R.id.btnFloat );
        mFullScreenButton = findViewById( R.id.btnFullscreen );

        int uiOption = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        getWindow().getDecorView().setSystemUiVisibility( uiOption );

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener( new View.OnSystemUiVisibilityChangeListener()
        {
            @Override
            public void onSystemUiVisibilityChange( int visibility )
            {
                Log.e( "nith", "onSystemUiVisibilityChange " + visibility );
                if ( (visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0 )
                {
                    mButton.setVisibility( View.VISIBLE );
                }
                else
                {
                    mButton.setVisibility( View.GONE );
                }
            }
        } );
        {
            Button btn = findViewById( R.id.btn );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    PopupMenu popup = new PopupMenu( BasicActivity.this, v );
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate( R.menu.menu, popup.getMenu() );
                    popup.show();
                }
            } );
        }
        {
            Spinner spinner = findViewById( R.id.spinner2 );
            ArrayAdapter adapter = ArrayAdapter.createFromResource( this, R.array.items, R.layout.item_spinner );
            adapter.setDropDownViewResource( R.layout.item_spinner_style );
            spinner.setAdapter( adapter );
        }
        {
            Button btn = findViewById( R.id.btnDialog1 );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    MyDialogFragment.newInstance( MyDialogFragment.Type.DEFAULT, "I am dialog1" ).show( getSupportFragmentManager(), "dialog" );
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btnDialog2 );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    MyDialogFragment.newInstance( MyDialogFragment.Type.ITEMS, "I am dialog2" ).show( getSupportFragmentManager(), "dialog" );
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btnDialog3 );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    MyDialogFragment.newInstance( MyDialogFragment.Type.SINGLE, "I am dialog3" ).show( getSupportFragmentManager(), "dialog" );
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btnDialog4 );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    MyDialogFragment.newInstance( MyDialogFragment.Type.MULTI, "I am dialog4" ).show( getSupportFragmentManager(), "dialog" );
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btnDialog5 );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    MyDialogFragment.newInstance( MyDialogFragment.Type.ADAPTER, "I am dialog5" ).show( getSupportFragmentManager(), "dialog" );
                }
            } );
        }
        {
            mFullScreenButton.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    int uiOption;
                    int visibility = getWindow().getDecorView().getSystemUiVisibility();
                    if ( (visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0 )
                    {
                        uiOption = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                    }
                    else
                    {
                        uiOption = 0;
                    }

                    getWindow().getDecorView().setSystemUiVisibility( uiOption );
                }
            } );
        }
        {
            View view = findViewById( R.id.mainLayout );
            registerForContextMenu( view );
        }
    }

    @Override
    public void onCreateContextMenu( ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo )
    {
        super.onCreateContextMenu( menu, v, menuInfo );
        getMenuInflater().inflate( R.menu.navigation, menu );
        menu.add( "MyItem" );
    }

    @Override
    public boolean onContextItemSelected( MenuItem item )
    {
        Log.e( "nith", "onContextItemSelected " + item.getTitle() );
        return super.onContextItemSelected( item );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        getMenuInflater().inflate( R.menu.menu, menu );

        {
            MenuItem.OnActionExpandListener expandListener = new MenuItem.OnActionExpandListener()
            {
                @Override
                public boolean onMenuItemActionCollapse( MenuItem item )
                {
                    return true;  // Return true to collapse action view
                }

                @Override
                public boolean onMenuItemActionExpand( MenuItem item )
                {
                    return true;  // Return true to expand action view
                }
            };

            MenuItem item = menu.findItem( R.id.action_search );
            item.setOnActionExpandListener( expandListener );
            SearchView view = (SearchView) item.getActionView();
            view.setOnSearchClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    Log.e( "nith", "on search view click" );
                }
            } );

            view.setOnQueryTextListener( new SearchView.OnQueryTextListener()
            {
                @Override
                public boolean onQueryTextSubmit( String query )
                {
                    Log.e( "nith", "onQueryTextSubmit " + query );
                    return false;
                }

                @Override
                public boolean onQueryTextChange( String newText )
                {
                    Log.e( "nith", "onQueryTextChange " + newText );
                    return false;
                }
            } );
        }
        {
            MenuItem shareItem = menu.findItem( R.id.action_share );
            ShareActionProvider provider =
                    (ShareActionProvider) MenuItemCompat.getActionProvider( shareItem );

            Intent intent = new Intent( Intent.ACTION_SEND );
            intent.setType( "image/*" );
            intent.putExtra( Intent.EXTRA_STREAM, "http://www.fnordware.com/superpng/pnggrad16rgb.png" );
            provider.setShareIntent( intent );
        }

        return true;
    }

    @Override
    public void onFragmentInteraction( String strItem )
    {
        Log.e( "nith", "dialog return item = " + strItem );
    }
}
