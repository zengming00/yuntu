package com.zengming.yuntu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Random;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

public class Zmlib {
	public static Random random = new Random();
	public static boolean isDebug = true;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.CHINESE);
	private static WifiManager wifiManager;
	private static PowerManager powerManager;
	
	
	public static String getAvaliableSdcard(){
		File f = new File(System.getenv("EXTERNAL_STORAGE"));
		if(f.canWrite()){
			return f.getAbsolutePath();
		}
		f = new File(System.getenv("SECONDARY_STORAGE"));
		if(f.canWrite()){
			return f.getAbsolutePath();
		}
		return null;
	}
	
	/**
	 * 包含头不包含尾（虽然包含头，但头出现的概率极小）
	 */
    public static int random(int howsmall, int howbig) {
        if (howsmall >= howbig) return howsmall;
        return (int) (random.nextFloat() * (howbig - howsmall) + howsmall);
    }
    
    public static float random(float howsmall, float howbig) {
    	if (howsmall >= howbig) return howsmall;
    	return (random.nextFloat() * (howbig - howsmall) + howsmall);
    }
    
    public static int randomMaxNum(int max){
      return (int)(Math.random() * max);
    }
    
	public static boolean isScreenOn(){
		return powerManager.isScreenOn();
	}
	
	public static void initServices(Context context){
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
	}
	
	public static boolean isWifiOn() {
		return wifiManager.isWifiEnabled();
	}

	public static void wifiOn() {
		wifiManager.setWifiEnabled(true);
	}

	public static void wifiOff() {
		wifiManager.setWifiEnabled(false);
	}

	public static void wifiReset() {
		wifiManager.setWifiEnabled(false);
		SystemClock.sleep(2000);
		wifiManager.setWifiEnabled(true);
		wifiManager.startScan();
	}
	
	public static boolean isHavePermission(Context context, String permission) {
		PackageManager pm = context.getPackageManager();
		int ret = pm.checkPermission(permission,context.getPackageName());
		return ret == PackageManager.PERMISSION_GRANTED;
	}

	public static String getCurrentTime() {
		return sdf.format(new Date());
	}
	/**
	 * 确保一个文件存在（如果不存在则创建）
	 */
	public static void ensureFileExists(File f) throws IOException {
		if (!f.exists()) {
			f.getParentFile().mkdirs();
			f.createNewFile();
		}
	}
	
	public static int parseInt(String str) {
		try{
			return Integer.parseInt(str);
		}catch(Exception e){
		}
		return 0;
	}
	
	public static long parseLong(String n) {
		try {
			return Long.parseLong(n);
		} catch (NumberFormatException e) {
		}
		return 0;
	}
	public static long ip2long(String ip){
		 String[] ipNums = ip.split("\\.");  
		 return (Long.parseLong(ipNums[0]) << 24)  
		      | (Long.parseLong(ipNums[1]) << 16)  
		      | (Long.parseLong(ipNums[2]) << 8)  
		      | (Long.parseLong(ipNums[3]));  
	}
	public static String long2ip(long ip) {
		return new StringBuilder()
			.append(((ip >> 24) & 0xff)).append('.')
			.append((ip >> 16) & 0xff).append('.')
			.append((ip >> 8) & 0xff).append('.')
			.append((ip & 0xff)).toString();
	}
	/**
	 * 获取IPv4地址 
	 * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
	 * <uses-permission android:name="android.permission.INTERNET"/>
	 */
	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					String address = inetAddress.getHostAddress();
					if (!inetAddress.isLoopbackAddress() && !address.contains(":")){//!inetAddress.isLinkLocalAddress() && !inetAddress.isAnyLocalAddress()) {
						return address;
					}
					
//					InetAddressUtils.
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获得MAC地址
	 */
	public static String getLocalMacAddress(Context context) {
		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		return info.getMacAddress();
	}

	/**
	 * 释放一个Assets目录下的文件到files目录下
	 * 
	 * @param context
	 * @param filename
	 * @return files目录下此文件的完整路径
	 * @throws IOException
	 */
	public static String releaseFile(Context context, String filename)
			throws IOException {
		File outFile = context.getFileStreamPath(filename);
		if (!outFile.exists()) {
			InputStream is = context.getAssets().open(filename);
			OutputStream out = context.openFileOutput(filename,Context.MODE_PRIVATE);
			byte[] buf = new byte[1024 * 4];
			int len = 0;
			while ((len = is.read(buf)) != -1) {
				out.write(buf, 0, len);
			}
			is.close();
			out.close();
		}
		return outFile.getAbsolutePath();
	}

	/**
	 * 获取应用程序名称
	 */
	public static String getAppName(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(
					context.getPackageName(), 0);
			int labelRes = packageInfo.applicationInfo.labelRes;
			return context.getResources().getString(labelRes);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * [获取应用程序版本名称信息]
	 * 
	 * @param context
	 * @return 当前应用的版本名称
	 */
	public static String getVersionName(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(
					context.getPackageName(), 0);
			return packageInfo.versionName;

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void toast(Context context, Object msg) {
		String str = msg + "";
		if (isDebug)
			Log.e("Zmlib", str);
		Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
	}

	public static void toastLong(Context context, Object msg) {
		String str = msg + "";
		if (isDebug)
			Log.e("Zmlib", str);
		Toast.makeText(context, str, Toast.LENGTH_LONG).show();
	}

	public static void msgBoxOkCancel(Context context, String msg,
			DialogInterface.OnClickListener okListener) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle("信息：").setMessage(msg)
				.setPositiveButton("确定", okListener)
				.setNegativeButton("取消", null).show();
	}

	public static void msgBoxOk(Context context, String msg,
			DialogInterface.OnClickListener okListener) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle("信息：").setMessage(msg)
				.setPositiveButton("确定", okListener).setCancelable(false);
		dialog.show();
	}

	/**
	 * 安装apk
	 * 
	 * @param context
	 * @param file
	 */
	public static void installApk(Context context, File file) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		context.startActivity(intent);
	}

	public static void saveObjectToFile(Object obj, String filename) throws IOException{
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
		oos.writeObject(obj);
		oos.close();
	}
	
	public static Object getObjectFromFile(String filename) throws IOException, ClassNotFoundException{
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename));
		Object obj = ois.readObject();
		ois.close();
		return obj;
	}
	/**
	 * dp转px
	 */
	public static int dp2px(Context context, float dpVal) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				dpVal, context.getResources().getDisplayMetrics());
	}

	/**
	 * sp转px
	 */
	public static int sp2px(Context context, float spVal) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
				spVal, context.getResources().getDisplayMetrics());
	}

	/**
	 * px转dp
	 */
	public static float px2dp(Context context, float pxVal) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (pxVal / scale);
	}

	/**
	 * px转sp
	 */
	public static float px2sp(Context context, float pxVal) {
		return (pxVal / context.getResources().getDisplayMetrics().scaledDensity);
	}
}
