package com.tolka.libphonenumber_sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public class MainActivity extends AppCompatActivity
{
    TextView mResult;
    EditText mPhone;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        mPhone = (EditText) findViewById( R.id.edit_phone );
        mResult = (TextView) findViewById( R.id.text_result );
        Button btn = (Button) findViewById( R.id.btn_check );
        btn.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                String strPhone = mPhone.getText().toString();
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                try
                {
                    Phonenumber.PhoneNumber phone = phoneUtil.parse( strPhone, "PH" );
                    boolean bValid = phoneUtil.isValidNumber( phone );
                    mResult.setText( phone.toString() + " " + bValid );
                }
                catch ( NumberParseException e )
                {
                    System.err.println( "NumberParseException was thrown: " + e.toString() );
                }
            }
        } );

    }
}
