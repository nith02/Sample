package com.tolka.multimediachat.utility;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.util.ArrayList;

import static com.tolka.multimediachat.utility.Common.sleep;

public class UsbUtility
{
    private static final String TAG = UsbUtility.class.getSimpleName();
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private ArrayList<UsbDeviceId> mUsbDeviceIds;
    private Context mContext;
    private UsbManager mUsbManager;
    private boolean m_bPermissionRequestDone;
    private boolean m_bUserDenyGrantUsbPermission;
    private boolean m_bUsbPermissionGranted;

    private final BroadcastReceiver mUsbPermissionActionReceiver = new BroadcastReceiver()
    {
        public void onReceive( Context context, Intent intent )
        {
            String action = intent.getAction();
            Log.d( "RHDSEED", "action 11= " + action );
            if ( ACTION_USB_PERMISSION.equals( action ) )
            {
                synchronized ( this )
                {
                    UsbDevice usbDevice = intent.getParcelableExtra( UsbManager.EXTRA_DEVICE );
                    if ( intent.getBooleanExtra( UsbManager.EXTRA_PERMISSION_GRANTED, false ) )
                    {
                        //user choose YES for your previously popup window asking for grant perssion for this usb device
                        if ( null != usbDevice )
                        {
                            if ( isTargetDevice( usbDevice ) )
                            {
                                m_bUsbPermissionGranted = true;
                            }
                        }
                    }
                    else
                    {
                        //user choose NO for your previously popup window asking for grant perssion for this usb device
                        m_bUserDenyGrantUsbPermission = true;
                    }
                    m_bPermissionRequestDone = true;
                }
            }
        }
    };

    /**
     * Create a UsbUtil instance.
     *
     * @param nDevicesXmlId The target usb devices xml resource ID.
     */
    public UsbUtility( Context context, int nDevicesXmlId )
    {
        mContext = context;
        mUsbManager = (UsbManager) context.getSystemService( Context.USB_SERVICE );
        mUsbDeviceIds = Common.getUsbDeviceIdFromXml( context, nDevicesXmlId );
        if ( mUsbDeviceIds.size() == 0 )
        {
            Log.e( TAG, "device_filter.xml is empty!");
        }
    }

    public boolean isTargetDevice( UsbDevice usbDevice )
    {
        for ( UsbDeviceId usb : mUsbDeviceIds )
        {
            if ( usbDevice.getVendorId() == usb.mVendorId && usbDevice.getProductId() == usb.mProductId )
            {
                return true;
            }
        }

        return false;
    }

    public boolean hasTargetDevice()
    {
        for ( final UsbDevice usbDevice : mUsbManager.getDeviceList().values() )
        {
            if ( isTargetDevice( usbDevice ) )
            {
                return true;
            }
        }

        return false;
    }

    public UsbDeviceId getTargetDeviceId()
    {
        for ( final UsbDevice usbDevice : mUsbManager.getDeviceList().values() )
        {
            if ( isTargetDevice( usbDevice ) )
            {
                return new UsbDeviceId( usbDevice.getVendorId(), usbDevice.getProductId() );
            }
        }

        return null;
    }

    public boolean hasTargetDevicePermission()
    {
        for ( final UsbDevice usbDevice : mUsbManager.getDeviceList().values() )
        {
            if ( isTargetDevice( usbDevice ) )
            {
                return mUsbManager.hasPermission( usbDevice );
            }
        }

        return false;
    }

    /**
     * Grant target usb device permission.
     *
     * @return true if the target usb device permission is granted.
     */
    @WorkerThread
    public boolean openTargetDevice()
    {
        if ( hasTargetDevicePermission() )
        {
            return true;
        }

        m_bUsbPermissionGranted = false;
        IntentFilter filter = new IntentFilter( ACTION_USB_PERMISSION );
        mContext.registerReceiver( mUsbPermissionActionReceiver, filter );

        final PendingIntent mPermissionIntent = PendingIntent.getBroadcast( mContext, 0, new Intent( ACTION_USB_PERMISSION ), 0 );
        Log.d( "RHDSEED", "usbDevice size= " + mUsbManager.getDeviceList().size() );

        //
        // <HACK> Request all usb devices permission to make dongle work (for LG2 / note2 / note3).
        //
        UsbDevice iteDongleDevice = null;
        for ( final UsbDevice usbDevice : mUsbManager.getDeviceList().values() )
        {
            Log.d( "RHDSEED", "usbDevice.getDeviceName = " + usbDevice.getDeviceName() );
            Log.d( "RHDSEED", "usbDevice.getDeviceId = " + usbDevice.getDeviceId() );
            Log.d( "RHDSEED", "usbDevice.getProductId = " + usbDevice.getProductId() );
            Log.d( "RHDSEED", "usbDevice.getVendorId = " + usbDevice.getVendorId() );

            if ( isTargetDevice( usbDevice ) )
            {
                iteDongleDevice = usbDevice;
                continue;
            }

            if ( mUsbManager.hasPermission( usbDevice ) )
            {
                continue;
            }

            m_bPermissionRequestDone = false;
            m_bUserDenyGrantUsbPermission = false;
            mUsbManager.requestPermission( usbDevice, mPermissionIntent );
            while ( !m_bPermissionRequestDone )
            {
                sleep( 100 );
            }

            if ( m_bUserDenyGrantUsbPermission )
            {
                mContext.unregisterReceiver( mUsbPermissionActionReceiver );
                return false;
            }
        }

        if ( iteDongleDevice != null )
        {
            Log.d( "RHDSEED", "iteDongleDevice.getVendorId = " + iteDongleDevice.getVendorId() );
            Log.d( "RHDSEED", "iteDongleDevice.getProductId = " + iteDongleDevice.getProductId() );
            m_bPermissionRequestDone = false;
            mUsbManager.requestPermission( iteDongleDevice, mPermissionIntent );
            while ( !m_bPermissionRequestDone )
            {
                sleep( 100 );
            }
        }

        mContext.unregisterReceiver( mUsbPermissionActionReceiver );
        return m_bUsbPermissionGranted;
    }
}
