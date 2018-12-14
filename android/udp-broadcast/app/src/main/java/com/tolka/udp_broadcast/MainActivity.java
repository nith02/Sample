package com.tolka.udp_broadcast;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "nith";
    private static final int PORT = 5000;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        FloatingActionButton fab = findViewById( R.id.fab );
        fab.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                new Thread( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            DatagramSocket clientSocket = new DatagramSocket();
                            clientSocket.setBroadcast( true );

                            String msg = "test";
                            byte[] sendData;

                            sendData = msg.getBytes();
                            InetAddress address = getBroadcastAdrress();
                            DatagramPacket sendPacket = new DatagramPacket( sendData,
                                    sendData.length, address, PORT );
                            clientSocket.send( sendPacket );
                            clientSocket.close();
                        }
                        catch ( Exception e )
                        {
                            e.printStackTrace();
                        }
                    }
                } ).start();

                new Thread( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            DatagramSocket socket = new DatagramSocket( PORT, InetAddress.getByName( "0.0.0.0" ) );
                            socket.setBroadcast( true );

                            while ( true )
                            {
                                Log.i( TAG, "Ready to receive broadcast packets!" );
                                byte[] recvBuf = new byte[15000];
                                DatagramPacket packet = new DatagramPacket( recvBuf, recvBuf.length );
                                socket.receive( packet );

                                Log.e( TAG, "Packet received from: " + packet.getAddress().getHostAddress() );
                                String data = new String( packet.getData() ).trim();
                                Log.e( TAG, "Packet received; data: " + data );
                            }
                        }
                        catch ( IOException ex )
                        {
                            Log.e( TAG, "Oops" + ex.getMessage() );
                        }
                    }
                } ).start();
            }
        } );
    }

    private InetAddress getBroadcastAdrress() throws UnknownHostException
    {
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService( Context.WIFI_SERVICE );
        DhcpInfo dhcp = wifi.getDhcpInfo(); // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for ( int k = 0; k < 4; k++ )
        {
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        }
        return InetAddress.getByAddress( quads );
    }
}
