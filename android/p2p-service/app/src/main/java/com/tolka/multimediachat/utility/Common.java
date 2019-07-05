package com.tolka.multimediachat.utility;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.View;
import android.view.Window;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Common {

	public static String s_strCrashLogDir = Environment.getExternalStorageDirectory() + "/tolka/temp/crashlog";
	public static String s_strCrashLogPath = s_strCrashLogDir + "/crash.log";
	public static String s_strVideoLogDir = Environment.getExternalStorageDirectory() + "/tolka/temp/videolog";
	public static String s_strVideoLogPath = s_strVideoLogDir + "/video_log.log";
    public static String s_strPhoneId = null;

	/**
	 * get present connection wifi's SSID
	 * 
	 * @param ctx
	 * @return
	 */
	public static String getConnectWifiSsid(Context ctx) {
		String ssid;
		WifiManager wifiManager = (WifiManager) ctx
				.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager == null)
			return "";

		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo != null) {
			ssid = wifiInfo.getSSID();
			if (ssid != null) {
				Log.d("SSID", ssid);
				return ssid;
			}
		}

		return "";
	}

	public static boolean isNetworkConnected( Context context )
	{
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

	public static boolean isPortOpen(final String ip, final int port, final int timeout) {
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(ip, port), timeout);
			socket.close();
			return true;
		}

		catch(ConnectException ce){
			Log.d("RHDSEED","ce = " + ce.toString());
			return false;
		}

		catch (Exception ex) {
			Log.d("RHDSEED","ex = " + ex.toString());
			return false;
		}
	}

	public static String getFileExt( String fileName )
	{
		return fileName.substring( fileName.lastIndexOf( "." ) + 1, fileName.length() );
	}

	public static void sleep( long ms )
	{
		try
		{
			Thread.sleep( ms );
		}
		catch ( InterruptedException ignored )
		{
		}
	}

//	public static byte[] hmacSHA1Encrypt(byte [] text, byte [] key) throws Exception
//	{
//		//byte[] data=encryptKey.getBytes(ENCODING);
//		//根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
//		SecretKey secretKey = new SecretKeySpec(key, "HmacSHA1");
//		//生成一个指定 Mac 算法 的 Mac 对象
//		Mac mac = Mac.getInstance("HmacSHA1");
//		//用给定密钥初始化 Mac 对象
//		mac.init(secretKey);
//		//完成 Mac 操作
//		return mac.doFinal(text);
//	}

	/**
	 * Returns signal quality [0,100].
	 */
	public static int rssi2quality( int rssi )
	{
		int quality;

		if ( rssi <= -100 )
		{
			quality = 0;
		}
		else if ( rssi >= -50 )
		{
			quality = 100;
		}
		else
		{
			quality = 2 * (rssi + 100);
		}

		return quality;
	}

	/**
	 * Returns signal quality [0,100].
	 */
	public static int ber2quality( int ber )
	{
		if ( ber >= 500 )
		{
			return 0;
		}
		else if ( ber <= 0 )
		{
			return 100;
		}
		else
		{
			return 100 - ber / 5;
		}
	}

	public static void joinThread( Thread thread )
	{
		if ( thread != null )
		{
			try
			{
				thread.join();
			}
			catch ( InterruptedException ignored )
			{
			}
		}
	}

	public static String getMacAddr()
	{
		try
		{
			List<NetworkInterface> all = Collections.list( NetworkInterface.getNetworkInterfaces() );
			for ( NetworkInterface nif : all )
			{
				if ( !nif.getName().equalsIgnoreCase( "wlan0" ) )
				{
					continue;
				}

				byte[] macBytes = nif.getHardwareAddress();
				if ( macBytes == null )
				{
					return "";
				}

				StringBuilder res1 = new StringBuilder();
				for ( byte b : macBytes )
				{
					res1.append( Integer.toHexString( b & 0xFF ) + ":" );
				}

				if ( res1.length() > 0 )
				{
					res1.deleteCharAt( res1.length() - 1 );
				}
				return res1.toString();
			}
		}
		catch ( Exception ex )
		{
		}
		return "02:00:00:00:00:00";
	}

    public static ArrayList<UsbDeviceId> getUsbDeviceIdFromXml( Context context, int nXmlId )
    {
        ArrayList<UsbDeviceId> usbDeviceIds = new ArrayList<>();
        XmlResourceParser xml = context.getResources().getXml( nXmlId );

        try
        {
            xml.next();

            int eventType;
            while ( (eventType = xml.getEventType()) != XmlPullParser.END_DOCUMENT )
            {
                switch ( eventType )
                {
                case XmlPullParser.START_TAG:
                    if ( xml.getName().equals( "usb-device" ) )
                    {
                        AttributeSet as = Xml.asAttributeSet( xml );
                        int nVid = Integer.parseInt( as.getAttributeValue( null, "vendor-id" ) );
                        int nPid = Integer.parseInt( as.getAttributeValue( null, "product-id" ) );
                        usbDeviceIds.add( new UsbDeviceId( nVid, nPid ) );
                    }
                }
                xml.next();
            }
        }
        catch ( XmlPullParserException | IOException ignored )
        {
        }

        return usbDeviceIds;
    }

    public static String readFile( String strFilePath )
	{
		try
		{
			File file = new File( strFilePath );
			if ( !file.exists() )
			{
				return null;
			}

			FileInputStream fis = new FileInputStream( file );

			InputStreamReader inputStreamReader = new InputStreamReader( fis );
			BufferedReader bufferedReader = new BufferedReader( inputStreamReader );
			String receiveString;
			StringBuilder stringBuilder = new StringBuilder();

			while ( (receiveString = bufferedReader.readLine()) != null )
			{
				stringBuilder.append( receiveString );
			}

			fis.close();
			return stringBuilder.toString();
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			return null;
		}
	}

	public static void clearLogs()
	{
		{
			File file = new File( s_strCrashLogPath );
			file.delete();
		}
		{
			File file = new File( s_strVideoLogPath );
			file.delete();
		}
	}

	public static int dp2px( Context context, float dip )
	{
		Resources r = context.getResources();
		float px = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				dip,
				r.getDisplayMetrics()
		);
		return (int) px;
	}

	public static void fullScreen( Window window )
	{
		View decorView = window.getDecorView();
		decorView.setSystemUiVisibility( View.SYSTEM_UI_FLAG_LOW_PROFILE
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION );
	}
}
