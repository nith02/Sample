package com.tolka.billingsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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
    private boolean mSubscribeMonthly;
    private boolean mSubscribeYearly;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        mBillingManager = new BillingManager( this, this );

        {
            Button btn = findViewById( R.id.btn_gas );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    mBillingManager.initiatePurchaseFlow( BillingConstants.SKU_GAS, BillingClient.SkuType.INAPP );
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btn_premium );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    mBillingManager.initiatePurchaseFlow( BillingConstants.SKU_PREMIUM, BillingClient.SkuType.INAPP );
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btn_monthly );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    if ( mSubscribeYearly )
                    {
                        ArrayList<String> oldSku = new ArrayList<>();
                        oldSku.add( BillingConstants.SKU_GOLD_YEARLY );
                        mBillingManager.initiatePurchaseFlow( BillingConstants.SKU_GOLD_MONTHLY, oldSku, BillingClient.SkuType.SUBS );
                    }
                    else
                    {
                        mBillingManager.initiatePurchaseFlow( BillingConstants.SKU_GOLD_MONTHLY, null, BillingClient.SkuType.SUBS );
                    }
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btn_yearly );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    if ( mSubscribeMonthly )
                    {
                        ArrayList<String> oldSku = new ArrayList<>();
                        oldSku.add( BillingConstants.SKU_GOLD_MONTHLY );
                        mBillingManager.initiatePurchaseFlow( BillingConstants.SKU_GOLD_YEARLY, oldSku, BillingClient.SkuType.SUBS );
                    }
                    else
                    {
                        mBillingManager.initiatePurchaseFlow( BillingConstants.SKU_GOLD_YEARLY, null, BillingClient.SkuType.SUBS );

                    }
                }
            } );
        }
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
        Log.e( "nith", "onPurchasesUpdated" );
        mSubscribeMonthly = false;
        mSubscribeYearly = false;

        for ( Purchase purchase : purchases )
        {
            Log.e( "nith", purchase.getOriginalJson() );
            Log.e( "nith", purchase.getSignature() );
            if ( purchase.getSku().equals( BillingConstants.SKU_GAS ) || purchase.getSku().equals( BillingConstants.SKU_PREMIUM ) )
            {
                mBillingManager.consumeAsync( purchase.getPurchaseToken() );
            }
            else if ( purchase.getSku().equals( BillingConstants.SKU_GOLD_MONTHLY ) )
            {
                mSubscribeMonthly = true;
            }
            else if ( purchase.getSku().equals( BillingConstants.SKU_GOLD_YEARLY ) )
            {
                mSubscribeYearly = true;
            }
        }
    }
}
