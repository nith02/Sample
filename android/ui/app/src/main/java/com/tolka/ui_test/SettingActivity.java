package com.tolka.ui_test;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import java.util.List;

public class SettingActivity extends PreferenceActivity
{
    @Override
    public void onBuildHeaders( List<Header> target )
    {
        super.onBuildHeaders( target );
        loadHeadersFromResource( R.xml.header, target );
    }

    @Override
    protected boolean isValidFragment( String fragmentName )
    {
        return true;
    }

    public static class PrefFragment extends PreferenceFragment
    {
        @Override
        public void onCreate( @Nullable Bundle savedInstanceState )
        {
            super.onCreate( savedInstanceState );
            addPreferencesFromResource( R.xml.setting );
        }
    }

    public static class Pref2Fragment extends PreferenceFragment
    {
        @Override
        public void onCreate( @Nullable Bundle savedInstanceState )
        {
            super.onCreate( savedInstanceState );
            addPreferencesFromResource( R.xml.setting2 );
        }
    }
}
