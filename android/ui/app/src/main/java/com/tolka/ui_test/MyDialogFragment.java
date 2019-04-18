package com.tolka.ui_test;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;

public class MyDialogFragment extends DialogFragment
{
    enum Type
    {
        DEFAULT,
        ITEMS,
        SINGLE,
        MULTI,
        ADAPTER,
    }

    private static final String ARG_TYPE = "type";
    private static final String ARG_MESSAGE = "message";
    private Type mType;
    private String mMessage;
    private String [] mItems;
    private int mWhich;
    private OnFragmentInteractionListener mListener;

    public MyDialogFragment()
    {
        // Required empty public constructor
    }

    public static MyDialogFragment newInstance( Type param1, String param2 )
    {
        MyDialogFragment fragment = new MyDialogFragment();
        Bundle args = new Bundle();
        args.putInt( ARG_TYPE, param1.ordinal() );
        args.putString( ARG_MESSAGE, param2 );
        fragment.setArguments( args );
        return fragment;
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        mItems = getResources().getStringArray( R.array.items );
        mWhich = -1;
        if ( getArguments() != null )
        {
            int nType = getArguments().getInt( ARG_TYPE );
            mType = Type.values()[nType];
            mMessage = getArguments().getString( ARG_MESSAGE );
        }
    }

    private DialogInterface.OnClickListener mOnClick = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick( DialogInterface dialog, int which )
        {
            mWhich = which;
            if ( mType != Type.SINGLE )
            {
                onButtonPressed( mWhich != -1 ? mItems[mWhich] : null );
            }
        }
    };

    private DialogInterface.OnMultiChoiceClickListener mOnMultiChoiceClick = new DialogInterface.OnMultiChoiceClickListener()
    {
        @Override
        public void onClick( DialogInterface dialog, int which, boolean isChecked )
        {
            if ( isChecked )
            {
                mWhich = which;
            }
        }
    };

    @NonNull
    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
        builder.setTitle( "title" );

        switch ( mType )
        {
        case DEFAULT:
            builder.setMessage( mMessage );
            break;
        case ITEMS:
            builder.setItems( R.array.items, mOnClick );
            break;
        case SINGLE:
            builder.setSingleChoiceItems( R.array.items, -1, mOnClick );
            break;
        case MULTI:
            builder.setMultiChoiceItems( R.array.items, new boolean []{false, false, false}, mOnMultiChoiceClick );
            break;
        case ADAPTER:
            ArrayAdapter<String> adapter = new ArrayAdapter<>( getActivity(), android.R.layout.simple_list_item_1, new String[]{"item1","item2"});
            builder.setAdapter( adapter, mOnClick );
            break;
        }

        if ( mType == Type.SINGLE || mType == Type.MULTI )
        {
            builder.setPositiveButton( "OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick( DialogInterface dialog, int which )
                {
                    onButtonPressed( mWhich != -1 ? mItems[mWhich] : null );
                }
            } );

            builder.setNegativeButton( "CANCEL", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick( DialogInterface dialog, int which )
                {
                }
            } );
        }

        return builder.create();
    }

    public void onButtonPressed( String strItem )
    {
        if ( mListener != null )
        {
            mListener.onFragmentInteraction( strItem );
        }
    }

    @Override
    public void onAttach( Context context )
    {
        super.onAttach( context );
        if ( context instanceof OnFragmentInteractionListener )
        {
            mListener = (OnFragmentInteractionListener) context;
        }
        else
        {
            throw new RuntimeException( context.toString()
                    + " must implement OnFragmentInteractionListener" );
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction( String strItem );
    }
}
