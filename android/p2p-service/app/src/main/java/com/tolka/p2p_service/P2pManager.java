package com.tolka.p2p_service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class P2pManager
{
    private static final String TAG = "P2pManager";
    private Context mContext;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private boolean mConnecting = false;
    private InetAddress mAddress = null;
    private Listener mListener;

    public interface Listener
    {
        void onP2pScanResult( List<WifiP2pDevice> peers );
        void onP2pConnecting();

        void onStart( String name );
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive( Context context, Intent intent )
        {
            String action = intent.getAction();

            if ( WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals( action ) )
            {
                if ( !mConnecting )
                {
                    mConnecting = true;
                    mManager.requestPeers( mChannel, m_peerListListener );
                }
            }
            else if ( WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals( action ) )
            {
                if ( mListener != null )
                {
                    mListener.onP2pConnecting();
                }
                mManager.requestConnectionInfo( mChannel, new WifiP2pManager.ConnectionInfoListener()
                {
                    @Override
                    public void onConnectionInfoAvailable( WifiP2pInfo info )
                    {
                        mAddress = info.groupOwnerAddress;
                    }
                } );

                mManager.requestGroupInfo( mChannel, new WifiP2pManager.GroupInfoListener()
                {
                    @Override
                    public void onGroupInfoAvailable( WifiP2pGroup group )
                    {
                        if ( group == null )
                        {
                            return;
                        }

                        String strNetworkName = group.getNetworkName();
                        if ( strNetworkName != null )
                        {
                            String strName = strNetworkName.substring( 10 );
                            if ( mListener != null )
                            {
                                mListener.onStart( strName );
                            }
                        }
                    }
                } );
            }
        }
    };

    private WifiP2pManager.PeerListListener m_peerListListener = new WifiP2pManager.PeerListListener()
    {
        @Override
        public void onPeersAvailable( WifiP2pDeviceList peerList )
        {
            Collection<WifiP2pDevice> refreshedPeers = peerList.getDeviceList();
            List<WifiP2pDevice> peers = new ArrayList<>();

            for ( WifiP2pDevice device : refreshedPeers )
            {
                if ( device.deviceName.startsWith( "Android" ) )
                {
                    peers.add( device );
                }
            }

            if ( mListener != null )
            {
                mListener.onP2pScanResult( peers );
            }
        }
    };


    public P2pManager( Context context )
    {
        mContext = context;
    }

    public void open( Listener listener )
    {
        mManager = (WifiP2pManager) mContext.getSystemService( Context.WIFI_P2P_SERVICE );
        mChannel = mManager.initialize( mContext, mContext.getMainLooper(), null );
        mListener = listener;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction( WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION );
        intentFilter.addAction( WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION );
        intentFilter.addAction( WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION );
        intentFilter.addAction( WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION );
        mContext.registerReceiver( mReceiver, intentFilter );
    }

    public void close()
    {
        mContext.unregisterReceiver( mReceiver );
        mListener = null;
    }

    public void scan()
    {
        mManager.discoverPeers( mChannel, new WifiP2pManager.ActionListener()
        {
            @Override
            public void onSuccess()
            {
            }

            @Override
            public void onFailure( int reasonCode )
            {
                Log.e( TAG, "p2p discover fail" );
            }
        } );
    }

    public void connect( WifiP2pDevice device )
    {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect( mChannel, config, new WifiP2pManager.ActionListener()
        {
            @Override
            public void onSuccess()
            {
            }

            @Override
            public void onFailure( int reason )
            {
                Log.e( TAG, "p2p connect fail" );
            }
        } );
    }

    public String getPeerIp()
    {
        if ( mAddress == null )
        {
            return null;
        }

        return mAddress.getHostAddress();
    }

    public void start()
    {
        mManager.createGroup( mChannel, new WifiP2pManager.ActionListener()
        {
            @Override
            public void onSuccess()
            {
            }

            @Override
            public void onFailure( int reason )
            {
                Log.e( TAG, "p2p create group failed, reason: " + reason );
            }
        } );
    }

    public void stop()
    {
        mManager.removeGroup( mChannel, new WifiP2pManager.ActionListener()
        {
            @Override
            public void onSuccess()
            {
            }

            @Override
            public void onFailure( int reason )
            {
            }
        } );
    }

    public void registerService( int serverPort )
    {
        Map record = new HashMap();
        record.put( "listenport", String.valueOf( serverPort ) );
        record.put( "name", "myService" );
        record.put( "available", "visible" );

        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance( "_test", "_presence._tcp", record );

        mManager.addLocalService( mChannel, serviceInfo, new WifiP2pManager.ActionListener()
        {
            @Override
            public void onSuccess()
            {
                Log.e( TAG, "server ok" );
            }

            @Override
            public void onFailure( int arg0 )
            {
                Log.e( TAG, "server fail " + arg0 );
            }
        } );
    }

    public void discoverService()
    {
        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener()
        {
            @Override
            public void onDnsSdTxtRecordAvailable(
                    String fullDomain, Map record, WifiP2pDevice device )
            {
                Log.e( TAG, "DnsSdTxtRecord available fullDomain -" + fullDomain );
                Log.e( TAG, "DnsSdTxtRecord available record -" + record.toString() );
                Log.e( TAG, "DnsSdTxtRecord available device -" + device.toString() );
            }
        };

        WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager.DnsSdServiceResponseListener()
        {
            @Override
            public void onDnsSdServiceAvailable(
                    String instanceName, String registrationType,
                    WifiP2pDevice resourceType )
            {
                Log.e( TAG, "onBonjourServiceAvailable instanceName - " + instanceName );
                Log.e( TAG, "onBonjourServiceAvailable registrationType - " + registrationType );
                Log.e( TAG, "onBonjourServiceAvailable resourceType - " + resourceType );
            }
        };

        mManager.setDnsSdResponseListeners( mChannel, servListener, txtListener );


        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mManager.addServiceRequest( mChannel,
                serviceRequest,
                new WifiP2pManager.ActionListener()
                {
                    @Override
                    public void onSuccess()
                    {
                        Log.e( TAG, "addServiceRequest ok" );
                    }

                    @Override
                    public void onFailure( int code )
                    {
                        Log.e( TAG, "addServiceRequest fail " + code );
                    }
                } );

        mManager.discoverServices( mChannel, new WifiP2pManager.ActionListener()
        {
            @Override
            public void onSuccess()
            {
                Log.e( TAG, "discoverServices ok" );
            }

            @Override
            public void onFailure( int code )
            {
                Log.e( TAG, "discoverServices fail " + code );
            }
        } );
    }

}