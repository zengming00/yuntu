package com.zengming.yuntu;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;


/**
 * 详细的手机信息(必需保证此类可以独立存在！放在放在任何一个安卓代码里不需要依赖其它文件）
	<uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
 */
public class UserInfo implements Serializable,Parcelable{
	private static final String tag = "UserInfo";
	private static final long serialVersionUID = 20150517;

	/**
	 * 蓝牙信息（必需打开设备才能获取，有的低端手机甚至没有蓝牙功能）
	 */
	public static final class Bluetooth implements Serializable {
		private static final long serialVersionUID = 1L;
		public String address;
		public String name;
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Bluetooth [\n\taddress=")
					.append(address).append(", \n\tname=")
					.append(name).append("\n]");
			return builder.toString();
		}
		
		public String toGetParam(){
			return "address="+urlEncode(address)+"&name="+urlEncode(name);
		}
	}
	public Bluetooth bluetooth = new Bluetooth();

	/**
	 * WIFI信息
	 */
	public static final class Wifi implements Serializable {
		private static final long serialVersionUID = 1L;
		/**
		 * 本机mac
		 */
		public String mac;
		/**
		 * 路由器mac
		 */
		public String BSSID;
		/**
		 * 路由器wifi热点名(2.3不带引号，4.0带引号）
		 */
		public String SSID;
		/**
		 * 网络ID,意义不清楚，未连接为-1
		 */
		public int networkId;
		public int ip;
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Wifi [\n\twifiMac=").append(mac)
					.append(", \n\twifiBSSID=").append(BSSID)
					.append(", \n\twifiSSID=").append(SSID)
					.append(", \n\twifiNetworkId=").append(networkId)
					.append(", \n\twifiIp=").append(ip)
					.append(", \n\twifiIp(转换后)=").append(intToInetAddress(ip))
					.append("\n]");
			return builder.toString();
		}

		public String toGetParam() {
			return "wifiMac=" +urlEncode( mac )+
					"&wifiBSSID=" +urlEncode( BSSID )+
					"&wifiSSID="+urlEncode( SSID )+
					"&wifiNetworkId=" +networkId+
					"&wifiIp=" + ip;
		}
	}
	public Wifi wifi = new Wifi();
	
	/**
	 * 电话信息
	 */
	public static final class Telephony implements Serializable {
		private static final long serialVersionUID = 1L;
		public String phoneNumber;
		public String networkCountryIso;
		public String simCountryIso;
		public String networkOperatorName;
		public String simOperatorName;
		public String imei;
		/**
		 * 手机网络的制式 GPRS\EDGE\HSPA...
		 */
		public int simNetworkType;
		public String imsi;
		public String networkOperator;
		public String simOperator;
		public String simSerial;
		/**
		 * 手机类型（GSM|CDMA）
		 */
		public int phoneType;
		public String deviceSoftwareVersion;
		
		public String toGetParam() {
			return "phoneNumber=" +urlEncode( phoneNumber )+
					"&networkCountryIso=" +urlEncode( networkCountryIso )+
					"&simCountryIso=" +urlEncode( simCountryIso )+
					"&networkOperatorName=" +urlEncode( networkOperatorName )+
					"&simOperatorName=" +urlEncode( simOperatorName )+
					"&imei=" +urlEncode( imei )+
					"&simNetworkType=" + simNetworkType +
					"&imsi=" +urlEncode( imsi )+
					"&networkOperator=" +urlEncode( networkOperator )+
					"&simOperator=" +urlEncode( simOperator )+
					"&simSerial=" +urlEncode( simSerial )+
					"&phoneType=" + phoneType +
					"&deviceSoftwareVersion=" + deviceSoftwareVersion;
		}
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Telephony [\n\tphoneNumber=").append(phoneNumber)
					.append(", \n\tnetworkCountryIso=")
					.append(networkCountryIso).append(", \n\tsimCountryIso=")
					.append(simCountryIso).append(", \n\tnetworkOperatorName=")
					.append(networkOperatorName)
					.append(", \n\tsimOperatorName=").append(simOperatorName)
					.append(", \n\timei=").append(imei)
					.append(", \n\tsimNetworkType=").append(simNetworkType)
					.append("(").append(getNetworkTypeName(simNetworkType)).append(")")
					.append(", \n\timsi=").append(imsi)
					.append(", \n\tnetworkOperator=").append(networkOperator)
					.append(", \n\tsimOperator=").append(simOperator)
					.append(", \n\tsimSerial=").append(simSerial)
					.append(", \n\tphoneType=").append(phoneType).append("(1:GSM,2:CDMA)")
					.append(", \n\tdeviceSoftwareVersion=")
					.append(deviceSoftwareVersion).append("\n]");
			return builder.toString();
		}
	}
	public Telephony telephony = new Telephony();
	public int op;//运营商，用于鉴别联通、移动、电信，内部使用
	public boolean canGetPhoneNumber;//是否可以获取手机号码
	public String android_id;
	/**
	 * Build.VERSION.RELEASE
	 */
	public String android_version;
	transient String android_version_sys;
	transient String android_version_prop;
	public String hardware_sid;
	transient String hardware_sid_sys;
	transient String hardware_sid_prop;
	public int sdk_int;
	transient int sdk_int_sys;
	transient int sdk_int_prop;
	public String incremental;
	transient String incremental_sys;
	transient String incremental_prop;
	
	public String netExtra;
	/**
	 * 判断是手机网络还是WIFI，ConnectivityManager.TYPE_MOBILE
	 */
	public int networkType;

	public int mcc;
	public int mnc;

	public String model;
	transient String model_sys;
	transient String model_prop;
	public String board;
	transient String board_sys;
	transient String board_prop;
	public String display;
	transient String display_sys;
	transient String display_prop;
	public String brand;
	transient String brand_sys;
	transient String brand_prop;
	public String device;
	transient String device_sys;
	transient String device_prop;
	public String host;
	transient String host_sys;
	transient String host_prop;
	public String hardware;
	transient String hardware_sys;
	transient String hardware_prop;
	public String manufacturer;
	transient String manufacturer_sys;
	transient String manufacturer_prop;
	public String id;
	transient String id_sys;
	transient String id_prop;
	public String product;
	transient String product_sys;
	transient String product_prop;
	public String tags;
	transient String tags_sys;
	transient String tags_prop;
	/**
	 * 此字段在设置Build.TIME时要乘1000，sys和prop获取时不用乘
	 */
	public long time;
	transient long time_sys;
	transient long time_prop;
	public String user;
	transient String user_sys;
	transient String user_prop;
	public String fingerprint;
	transient String fingerprint_sys;
	transient String fingerprint_sys2;
	transient String fingerprint_prop;
	transient String fingerprint_prop2;

	public int widthPixels;
	public int heightPixels;
	public int densityDpi;
	
	public String baseband;

	public UserInfo(){
		
	}

	public static UserInfo getFromContext(Context context) {
		UserInfo info = new UserInfo();
		info.android_id = Settings.Secure.getString(context.getContentResolver(),
				Settings.Secure.ANDROID_ID);
		try {
			BluetoothAdapter bluetoothAdapter = BluetoothAdapter
					.getDefaultAdapter();
			if (bluetoothAdapter != null) {
				info.bluetooth.address = bluetoothAdapter.getAddress();
				info.bluetooth.name = bluetoothAdapter.getName();
			} else {
				Toast.makeText(context, "不支持蓝牙", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {}
		try {
			WifiManager wm = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wm.getConnectionInfo();
			info.wifi.mac = wifiInfo.getMacAddress();
			info.wifi.BSSID = wifiInfo.getBSSID();
			info.wifi.SSID = wifiInfo.getSSID();
			info.wifi.networkId = wifiInfo.getNetworkId();
			info.wifi.ip = wifiInfo.getIpAddress();// ipv4
		} catch (Exception e) {}
		try {
			TelephonyManager tm = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			info.telephony.phoneNumber = tm.getLine1Number();
			info.telephony.networkCountryIso = tm.getNetworkCountryIso();
			info.telephony.simCountryIso = tm.getSimCountryIso();
			info.telephony.networkOperatorName = tm.getNetworkOperatorName();
			info.telephony.simOperatorName = tm.getSimOperatorName();
			info.telephony.imei = tm.getDeviceId();
			info.telephony.simNetworkType = tm.getNetworkType();
			info.telephony.imsi = tm.getSubscriberId();
			info.telephony.networkOperator = tm.getNetworkOperator();
			info.telephony.simOperator = tm.getSimOperator();
			info.telephony.simSerial = tm.getSimSerialNumber();
			info.telephony.phoneType = tm.getPhoneType();
			info.telephony.deviceSoftwareVersion = tm.getDeviceSoftwareVersion();
			Log.e(tag, "deviceSoftwareVersion = " + info.telephony.deviceSoftwareVersion);
			Log.e(tag, "getSimState()="+tm.getSimState());
		} catch (Exception e) {}
//		{
//			if(VERSION.SDK_INT > VERSION_CODES.LOLLIPOP_MR1){//22
//				// 安卓5.1才有
//				SubscriptionManager sm = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
//				List<SubscriptionInfo> lst = sm.getActiveSubscriptionInfoList();
//				for (SubscriptionInfo i : lst) {
//					Log.e(tag, i.toString()+"");
//					Log.e(tag, i.getNumber()+"");
//				}
//			}
//		}
		try {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo t = cm.getActiveNetworkInfo();
			if (t != null) {
				info.netExtra = t.getExtraInfo();
				info.networkType = t.getType();
			}
		} catch (Exception e) {}

		try {
			Resources res = android.content.res.Resources.getSystem();
			Configuration configuration = res.getConfiguration();
			info.mcc = configuration.mcc;
			info.mnc = configuration.mnc;
		} catch (Exception e) {}
		info.android_version = Build.VERSION.RELEASE;
		info.android_version_sys = getSystemProperties("ro.build.version.release");
		info.android_version_prop = getPropCmd("ro.build.version.release");

		info.sdk_int = VERSION.SDK_INT;
		info.sdk_int_sys = getSystemPropertiesInt("ro.build.version.sdk",0);
		info.sdk_int_prop = parseInt(getPropCmd("ro.build.version.sdk"));

		info.incremental = VERSION.INCREMENTAL;
		info.incremental_sys = getSystemProperties("ro.build.version.incremental");
		info.incremental_prop = getPropCmd("ro.build.version.incremental");

		info.hardware_sid = Build.SERIAL;
		info.hardware_sid_sys = getSystemProperties("ro.serialno");
		info.hardware_sid_prop = getPropCmd("ro.serialno");
		

		info.model = Build.MODEL;
		info.model_sys = getSystemProperties("ro.product.model");
		info.model_prop = getPropCmd("ro.product.model");
		
		info.board = Build.BOARD;
		info.board_sys = getSystemProperties("ro.product.board");
		info.board_prop = getPropCmd("ro.product.board");
		
		info.display = Build.DISPLAY;
		info.display_sys = getSystemProperties("ro.build.display.id");
		info.display_prop = getPropCmd("ro.build.display.id");
		
		info.brand = Build.BRAND;
		info.brand_sys = getSystemProperties("ro.product.brand");
		info.brand_prop = getPropCmd("ro.product.brand");
		
		info.device = Build.DEVICE;
		info.device_sys = getSystemProperties("ro.product.device");
		info.device_prop = getPropCmd("ro.product.device");

		info.host = Build.HOST;
		info.host_sys = getSystemProperties("ro.build.host");
		info.host_prop = getPropCmd("ro.build.host");
		
		info.hardware = Build.HARDWARE;
		info.hardware_sys = getSystemProperties("ro.hardware");
		info.hardware_prop = getPropCmd("ro.hardware");
		
		info.manufacturer = Build.MANUFACTURER;
		info.manufacturer_sys = getSystemProperties("ro.product.manufacturer");
		info.manufacturer_prop = getPropCmd("ro.product.manufacturer");

		info.id = Build.ID;
		info.id_sys = getSystemProperties("ro.build.id");
		info.id_prop = getPropCmd("ro.build.id");

		info.product = Build.PRODUCT;
		info.product_sys = getSystemProperties("ro.product.name");
		info.product_prop = getPropCmd("ro.product.name");

		info.tags = Build.TAGS;
		info.tags_sys = getSystemProperties("ro.build.tags");
		info.tags_prop = getPropCmd("ro.build.tags");
		
		info.time = Build.TIME/1000;
		info.time_sys = parseLong(getSystemProperties("ro.build.date.utc"));
		info.time_prop = parseLong(getPropCmd("ro.build.date.utc"));
		
		info.user = Build.USER;
		info.user_sys = getSystemProperties("ro.build.user");
		info.user_prop = getPropCmd("ro.build.user");

		//此字段和Build.RADIO相同，但真机(I50)Build.RADIO的值是Build.UNKNOWN
		info.baseband = getSystemProperties("gsm.version.baseband");//TelephonyProperties.PROPERTY_BASEBAND_VERSION
		
		info.fingerprint = Build.FINGERPRINT;
		info.fingerprint_sys = getSystemProperties("ro.build.fingerprint");
		info.fingerprint_prop = getPropCmd("ro.build.fingerprint");
		//(I50和A395E)获取结果为“”
		info.fingerprint_sys2 = getSystemProperties("ro.vendor.build.fingerprint");
		info.fingerprint_prop2 = getPropCmd("ro.vendor.build.fingerprint");

//		finger = getString("ro.product.brand") + '/' +
//                getString("ro.product.name") + '/' +
//                getString("ro.product.device") + ':' +
//                getString("ro.build.version.release") + '/' +
//                getString("ro.build.id") + '/' +
//                getString("ro.build.version.incremental") + ':' +
//                getString("ro.build.type") + '/' + 固定为user
//                getString("ro.build.tags");
		
//	       final String system = SystemProperties.get("ro.build.fingerprint");
//	       final String vendor = SystemProperties.get("ro.vendor.build.fingerprint");
		
		
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		info.widthPixels = dm.widthPixels;
		info.heightPixels = dm.heightPixels;
		info.densityDpi = dm.densityDpi;


		return info;
	}
	
	public static String getNetworkTypeName(int type) {
		try{
			Method m = TelephonyManager.class.getMethod("getNetworkTypeName", int.class);
			return (String) m.invoke(null, type);
		}catch(Exception e){}
		return "";
	}

	public static InetAddress intToInetAddress(int hostAddress) {
		byte[] addressBytes = { (byte) (0xff & hostAddress),
				(byte) (0xff & (hostAddress >> 8)),
				(byte) (0xff & (hostAddress >> 16)),
				(byte) (0xff & (hostAddress >> 24)) };

		try {
			return InetAddress.getByAddress(addressBytes);
		} catch (UnknownHostException e) {
			throw new AssertionError();
		}
	}

	@Override
	public String toString() {
		//	@formatter:off
		StringBuilder builder = new StringBuilder();
		builder.append("UserInfo [\n\tandroid_id=").append(android_id)
				.append(",\n\t android_version=").append(android_version)
				.append(",\n\t android_version_sys=").append(android_version_sys)
				.append(",\n\t android_version_prop=").append(android_version_prop)
				.append("\n")
				.append(",\n\t hardware_sid=").append(hardware_sid)
				.append(",\n\t hardware_sid_sys=").append(hardware_sid_sys)
				.append(",\n\t hardware_sid_prop=").append(hardware_sid_prop)
				.append("\n")
				.append(",\n\t sdk_int=").append(sdk_int)
				.append(",\n\t sdk_int_sys=").append(sdk_int_sys)
				.append(",\n\t sdk_int_prop=").append(sdk_int_prop)
				.append("\n")
				.append(",\n\t incremental=").append(incremental)
				.append(",\n\t incremental_sys=").append(incremental_sys)
				.append(",\n\t incremental_prop=").append(incremental_prop)
				.append("\n")
				.append(",\n\t netExtra=").append(netExtra)
				.append(",\n\t networkType=").append(networkType).append("(WIFI网1,手机网0)")
				.append(",\n\t mcc=").append(mcc)
				.append(",\n\t mnc=").append(mnc)
				.append("\n")
				.append(",\n\t model=").append(model)
				.append(",\n\t model_sys=").append(model_sys)
				.append(",\n\t model_prop=").append(model_prop)
				.append("\n")
				.append(",\n\t board=").append(board)
				.append(",\n\t board_sys=").append(board_sys)
				.append(",\n\t board_prop=").append(board_prop)
				.append("\n")
				.append(",\n\t display=").append(display)
				.append(",\n\t display_sys=").append(display_sys)
				.append(",\n\t display_prop=").append(display_prop)
				.append("\n")
				.append(",\n\t brand=").append(brand)
				.append(",\n\t brand_sys=").append(brand_sys)
				.append(",\n\t brand_prop=").append(brand_prop)
				.append("\n")
				.append(",\n\t device=").append(device)
				.append(",\n\t device_sys=").append(device_sys)
				.append(",\n\t device_prop=").append(device_prop)
				.append("\n")
				.append(",\n\t host=").append(host)
				.append(",\n\t host_sys=").append(host_sys)
				.append(",\n\t host_prop=").append(host_prop)
				.append("\n")
				.append(",\n\t hardware=").append(hardware)
				.append(",\n\t hardware_sys=").append(hardware_sys)
				.append(",\n\t hardware_prop=").append(hardware_prop)
				.append("\n")
				.append(",\n\t manufacturer=").append(manufacturer)
				.append(",\n\t manufacturer_sys=").append(manufacturer_sys)
				.append(",\n\t manufacturer_prop=").append(manufacturer_prop)
				.append("\n")
				.append(",\n\t id=").append(id)
				.append(",\n\t id_sys=").append(id_sys)
				.append(",\n\t id_prop=").append(id_prop)
				.append("\n")
				.append(",\n\t product=").append(product)
				.append(",\n\t product_sys=").append(product_sys)
				.append(",\n\t product_prop=").append(product_prop)
				.append("\n")
				.append(",\n\t tags=").append(tags)
				.append(",\n\t tags_sys=").append(tags_sys)
				.append(",\n\t tags_prop=").append(tags_prop)
				.append("\n")
				.append(",\n\t time=").append(time)
				.append(",\n\t time_sys=").append(time_sys)
				.append(",\n\t time_prop=").append(time_prop)
				.append("\n")
				.append(",\n\t user=").append(user)
				.append(",\n\t user_sys=").append(user_sys)
				.append(",\n\t user_prop=").append(user_prop)
				.append("\n")
				.append(",\n\t fingerprint=").append(fingerprint)
				.append(",\n\t fingerprint_sys=").append(fingerprint_sys)
				.append(",\n\t fingerprint_prop=").append(fingerprint_prop)
				.append(",\n\t fingerprint_sys2=").append(fingerprint_sys2)
				.append(",\n\t fingerprint_prop2=").append(fingerprint_prop2)
				.append("\n")
				.append(",\n\t widthPixels=").append(widthPixels)
				.append(",\n\t heightPixels=").append(heightPixels)
				.append(",\n\t densityDpi=").append(densityDpi)
				.append(",\n\t baseband=").append(baseband)
				.append(",\n bluetooth=").append(bluetooth)
				.append(",\n wifi=").append(wifi)
				.append(",\n telephony=").append(telephony)
				.append("\n]");
		return builder.toString();
		//	@formatter:on
	}
	
	private static String urlEncode(String str){
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (Exception e) {
		}
		return null;
	}
	
	public String toGetParam() {
		return "android_id="+urlEncode(android_id)+
			"&android_version="+urlEncode(android_version)+
			"&hardware_sid="+urlEncode(hardware_sid)+
			"&sdk_int="+sdk_int+
			"&incremental="+urlEncode(incremental)+
			"&netExtra="+urlEncode(netExtra)+
			"&networkType="+(networkType)+
			"&mcc="+(mcc)+
			"&mnc="+(mnc)+
			"&model="+urlEncode(model)+
			"&board="+urlEncode(board)+
			"&display="+urlEncode(display)+
			"&brand="+urlEncode(brand)+
			"&device="+urlEncode(device)+
			"&host="+urlEncode(host)+
			"&hardware="+urlEncode(hardware)+
			"&manufacturer="+urlEncode(manufacturer)+
			"&id="+urlEncode(id)+
			"&product="+urlEncode(product)+
			"&tags="+urlEncode(tags)+
			"&time="+(time)+
			"&user="+urlEncode(user)+
			"&fingerprint="+urlEncode(fingerprint)+
			"&widthPixels="+(widthPixels)+
			"&heightPixels="+(heightPixels)+
			"&densityDpi="+(densityDpi)+
			"&baseband="+urlEncode(baseband)+
			"&"+bluetooth.toGetParam()+
			"&"+wifi.toGetParam()+
			"&"+telephony.toGetParam();
	}
	/**
	 * 将本对象序列化为字节数组
	 * @return 成功返回字节数组，失败返回null
	 */
	public byte[] getBytes() {
		byte[] buf=null;
		try{
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bao);
			oos.writeObject(this);
			oos.flush();//!!
			buf = bao.toByteArray();
			bao.close();
		}catch(IOException e){}
		return buf;
	}
	public static UserInfo getFromBytes(byte[] bytes){
		UserInfo info = null;
		try{
			ByteArrayInputStream bai = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bai);
			info = (UserInfo) ois.readObject();
			bai.close();
		}catch(Exception e){}
		return info;
	}

	public boolean isWifi() {
		return this.networkType == ConnectivityManager.TYPE_WIFI;
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
	
	public static String getPropCmd(String k){
		String ret = "";
		Process proc = null;
		try {
			proc = Runtime.getRuntime().exec("getprop "+k);
			BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			return br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(proc!=null){
				proc.destroy();
			}
		}
		return ret;
	}
	public static String getSystemProperties(String k) {
		try {
			Class<?> clazz = Class.forName("android.os.SystemProperties");
			Method m = clazz.getDeclaredMethod("get", String.class);
			return (String) m.invoke(null, k);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int getSystemPropertiesInt(String k, int def) {
		try {
			Class<?> clazz = Class.forName("android.os.SystemProperties");
			Method m = clazz.getDeclaredMethod("getInt", String.class, int.class);
			m.setAccessible(true);
			return (Integer)m.invoke(null, k, def);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
/////////////////////////////Parcelable//////////////////////////////
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeSerializable(this);
	}
    
    public static final Parcelable.Creator<UserInfo> CREATOR = new Creator<UserInfo>()
    {
        @Override
        public UserInfo[] newArray(int size)
        {
            return new UserInfo[size];
        }
        
        @Override
        public UserInfo createFromParcel(Parcel in)
        {
        	return (UserInfo) in.readSerializable();
        }
    };
}
