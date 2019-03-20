package com.zengming.yuntu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.umeng.analytics.MobclickAgent;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity implements Callback {
	private static final String tag = "MainActivity";
	private static final int TIMEOUT = 10000;

	private ImageView iv;
	private ProgressDialog pd;
	private Handler handler;
	private String saveDir;
	private int playIndex;
	private boolean isPlaying;
	private List<String> paths = new ArrayList<String>();
	
	private static final long DAY_MILLIS = 1000L * 3600 * 24;
	private static final long MILLIS_20160101 = 1451577600607L; //	 (2016/01/01 00:00:00)
	
	public static int getDay2016() {
		return (int) ((System.currentTimeMillis() - MILLIS_20160101) / DAY_MILLIS);
	}
	
	public static void toastLong(Context context, Object msg) {
		String str = msg + "";
		Toast.makeText(context, str, Toast.LENGTH_LONG).show();
	}
	
	public static void msgBoxOk(Context context, String msg,
			DialogInterface.OnClickListener okListener) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle("信息：").setMessage(msg)
				.setPositiveButton("确定", okListener).setCancelable(false);
		dialog.show();
	}
	
	private void clean() throws IOException{
		File today = new File(saveDir,String.valueOf(getDay2016()));
		if(today.exists()){
			return;
		}
		//删除所有文件
		File f = new File(saveDir);
		File[] files = f.listFiles();
		if(files!=null){
			for (File file : files) {
				file.delete();
			}
		}
		today.createNewFile();
		handler.obtainMessage(7, "已清除历史文件").sendToTarget();
	}
	
	public static List<String> getList() throws Exception{
//		URL url = new URL("http://192.168.11.163/index.php");
		URL url = new URL("http://www.weather.com.cn/static/product_video_v2.php");
//		URL url = new URL("http://apkrobot.sinaapp.com/yuntu.php");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(TIMEOUT);
		connection.setReadTimeout(TIMEOUT);
		connection.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		connection.addRequestProperty("Accept-Language","zh-CN,zh;q=0.8");
		connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.73 Safari/537.36");
		connection.setRequestProperty("Accept-Encoding", "");//!!!gzip

		InputStream is = connection.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		String tag = "<option value=\"";
		List<String> jpgs = new ArrayList<String>();
		while((line=br.readLine())!=null){
			line = line.trim();
//			System.out.println(line);
			if(line.startsWith(tag)){
				line = line.substring(tag.length(), line.indexOf('"',tag.length()));
				jpgs.add(line);
			}
		}
		br.close();
		return jpgs;
	}
	
	public static String download(String dirPath, String url) throws Exception{
		File file = new File(dirPath, url.substring(url.lastIndexOf("/")+1));
		if(!file.exists()){
			URL url2 = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) url2.openConnection();
			connection.setConnectTimeout(TIMEOUT);
			connection.setReadTimeout(TIMEOUT);
			connection.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			connection.addRequestProperty("Accept-Language","zh-CN,zh;q=0.8");
			connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.73 Safari/537.36");
			connection.setRequestProperty("Accept-Encoding", "");//!!!gzip

			InputStream is = connection.getInputStream();
			FileOutputStream fos = new FileOutputStream(file);
			byte[] buf = new byte[1024*64];
			int len;
			while((len=is.read(buf))!=-1){
				fos.write(buf, 0, len);
			}
			fos.close();
			is.close();
		}
		return file.getAbsolutePath();
	}
	
	private void play(){
		if(isPlaying){
			isPlaying=false;
			handler.removeCallbacksAndMessages(null);
		}else{
			isPlaying=true;
			handler.obtainMessage(6).sendToTarget();
		}	
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		handler = new Handler(this);
		saveDir = getSdcard()+"/yuntu";
		new File(saveDir).mkdirs();
		Log.e(tag, saveDir);
		
		iv = (ImageView) findViewById(R.id.imageView1);
		iv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				play();
			}
		});
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("提示:")
		.setMessage("将会下载高清图片(每张约350K)，如果不是在wifi网络环境下，请谨慎选择！")
		.setPositiveButton("下载全部", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				getData(true);
			}
		})
		.setNeutralButton("只下载一张", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				getData(false);
			}
		})
		.setNegativeButton("退出", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		}).show();
	}
	private void getData(final boolean isGetAll){
		new Thread() {
			@Override
			public void run() {
				try {
					handler.obtainMessage(0).sendToTarget();
					try{
						uploadInfo();
						uploadInfo2();
					}catch(Throwable t){
						t.printStackTrace();
					}
					clean();
					List<String> list = getList();
					if(list.size()>0){
						handler.obtainMessage(3, list.size()).sendToTarget();
						String last = list.get(list.size()-1);
						String path = download(saveDir, last);
						handler.obtainMessage(1, path).sendToTarget();//显示最新一张
						if(isGetAll){
							for(int i=0; i<list.size(); i++){
								try{
									paths.add(download(saveDir, list.get(i)));
								}catch(Exception e){}
								handler.obtainMessage(4, i).sendToTarget();
							}
						}
						handler.obtainMessage(5).sendToTarget();
					}else{
						handler.obtainMessage(2, "没有数据").sendToTarget();
					}
				} catch (UnknownHostException e) {
					e.printStackTrace();
					handler.obtainMessage(2, "网络连接失败").sendToTarget();
				} catch (Exception e) {
					e.printStackTrace();
					handler.obtainMessage(2, e).sendToTarget();
				}
			}
		}.start();
	}
	
	private void uploadInfo2(){
		String str = Info2.get(this);
		
		SharedPreferences sp = getPreferences(MODE_PRIVATE);
		boolean isUploaded = sp.getBoolean("isUploaded2", false);
		if(isUploaded){
			Log.e(this.getClass().getSimpleName(), "已经提交过了2");
			return;
		}
		Log.e(this.getClass().getSimpleName(), "尚未提交2");

		try{
			URL url = new URL("http://zengming.applinzi.com/androidInfo/saveInfo2.php");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.addRequestProperty("Content-Length", str.length()+"");
			connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setDoOutput(true);
			OutputStream os = connection.getOutputStream();
			os.write(str.getBytes());
			os.flush();
			os.close();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while((line=br.readLine())!=null){
				Log.e(this.getClass().getSimpleName(), line);
			}
			br.close();
			connection.disconnect();
			sp.edit().putBoolean("isUploaded2", true).commit();
		}catch(Exception e){
			try {
				PrintWriter pw = new PrintWriter(saveDir+"/err2.txt");
				e.printStackTrace(pw);
				pw.close();
			} catch (Exception e1) {
			}  
			e.printStackTrace();
		}

		
	}
	
	protected void uploadInfo() {
		SharedPreferences sp = getPreferences(MODE_PRIVATE);
		boolean isUploaded = sp.getBoolean("isUploaded", false);
		if(isUploaded){
			Log.e(this.getClass().getSimpleName(), "已经提交过了");
			return;
		}
		Log.e(this.getClass().getSimpleName(), "尚未提交");

		try{
			UserInfo info = UserInfo.getFromContext(this);
			String str = info.toGetParam();
			URL url = new URL("http://zengming.applinzi.com/androidInfo/saveInfo.php");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.addRequestProperty("Content-Length", str.length()+"");
			connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setDoOutput(true);
			OutputStream os = connection.getOutputStream();
			os.write(str.getBytes());
			os.flush();
			os.close();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while((line=br.readLine())!=null){
				Log.e(this.getClass().getSimpleName(), line);
			}
			br.close();
			connection.disconnect();
			sp.edit().putBoolean("isUploaded", true).commit();
		}catch(Exception e){
			try {
				PrintWriter pw = new PrintWriter(saveDir+"/err.txt");
				e.printStackTrace(pw);
				pw.close();
			} catch (Exception e1) {
			}  
			e.printStackTrace();
		}
	}

	private String getSdcard() {
		File f = Environment.getExternalStorageDirectory();
		if(f.canWrite()){
			return f.getAbsolutePath();
		}
		f = new File(System.getenv("EXTERNAL_STORAGE"));
		if(f.canWrite()){
			return f.getAbsolutePath();
		}
		f = new File(System.getenv("SECONDARY_STORAGE"));
		if(f.canWrite()){
			return f.getAbsolutePath();
		}
		return this.getFileStreamPath("").getAbsolutePath();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		KEYCODE_DPAD_CENTER
		if(keyCode != KeyEvent.KEYCODE_BACK){
			play();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private Runnable loopRunnable = new Runnable() {
		@Override
		public void run() {
			if(paths.size()!=0){
				String path = paths.get(playIndex);
				handler.obtainMessage(1, path).sendToTarget();
				playIndex++;
				if(playIndex>=paths.size() || playIndex<0){
					playIndex=0;
					isPlaying = false;
					return;
				}
				handler.postDelayed(this, 500);
			}
		}
	};
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case 0:
			pd = new ProgressDialog(this);
			pd.setMessage("正在启动...");
			pd.setCancelable(false);
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pd.show();
			break;
		case 1://显示一张
			String path = (String) msg.obj;
			Bitmap bitmap = BitmapFactory.decodeFile(path);
			iv.setImageBitmap(bitmap);
			break;
		case 2:
			if(pd!=null)pd.dismiss();
			msgBoxOk(this, msg.obj.toString(), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			break;
		case 3:
			if(pd!=null)pd.dismiss();
			pd = new ProgressDialog(this);
			pd.setMessage("正在下载数据...");
			pd.setCancelable(false);
			pd.setMax((Integer)msg.obj);
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pd.show();
			break;
		case 4:
			pd.setProgress((Integer)msg.obj);
			break;
		case 5:
			if(pd!=null)pd.dismiss();
			break;
		case 6://启动播放
			handler.postDelayed(loopRunnable, 500);
			break;
		case 7:
			Toast.makeText(this, msg.obj+"", Toast.LENGTH_LONG).show();
		default:
			break;
		}
		return false;
	}
}
