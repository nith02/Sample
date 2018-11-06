package com.tolka.billingsample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.tolka.billingsample.billing.BillingConstants;
import com.tolka.billingsample.billing.BillingManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BillingManager.BillingUpdatesListener
{
    private BillingManager mBillingManager;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        mBillingManager = new BillingManager( this, this );

        FloatingActionButton fab = findViewById( R.id.fab );
        fab.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                mBillingManager.initiatePurchaseFlow( BillingConstants.SKU_GAS, BillingClient.SkuType.INAPP );
            }
        } );
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if ( mBillingManager != null && mBillingManager.getBillingClientResponseCode() == BillingClient.BillingResponse.OK )
        {
            mBillingManager.queryPurchases();
        }
    }

    private void queryPurchases()
    {
        List<String> skuList = new ArrayList<>();
        skuList.add( BillingConstants.SKU_GAS );
        mBillingManager.querySkuDetailsAsync( BillingClient.SkuType.INAPP, skuList, new SkuDetailsResponseListener()
        {
            @Override
            public void onSkuDetailsResponse( int responseCode, List<SkuDetails> skuDetailsList )
            {
                Log.e( "nith", "onSkuDetailsResponse: responseCode " + responseCode );
                for ( SkuDetails sku : skuDetailsList )
                {
                    Log.e( "nith", sku.toString() );
                }
            }
        } );
    }

    @Override
    public void onBillingClientSetupFinished()
    {
        queryPurchases();
    }

    @Override
    public void onConsumeFinished( String token, int result )
    {
        if ( result != BillingClient.BillingResponse.OK )
        {
            Log.e( "nith", "onConsumeFinished: consume error " + result );
            return;
        }
        Log.e( "nith", "onConsumeFinished: success" );
    }

    @Override
    public void onPurchasesUpdated( List<Purchase> purchases )
    {
        for ( Purchase purchase : purchases )
        {
            Log.e( "nith", purchase.getOriginalJson() );
            Log.e( "nith", purchase.getSignature() );
            mBillingManager.consumeAsync( purchase.getPurchaseToken() );
        }
    }
}
