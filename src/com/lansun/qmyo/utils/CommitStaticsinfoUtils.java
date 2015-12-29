package com.lansun.qmyo.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import com.lansun.qmyo.R;
import com.lansun.qmyo.app.App;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

public class CommitStaticsinfoUtils {

	 static int STATISTICS_STATUE_FIRST_OPEN = 1;
	 static int STATISTICS_STATUE_LOGIN = 2;
	 private static int mStatistics_situation;
	 
	
	public static  void commitStaticsinfo( int statistics_situation){
		/**
		 * 上传渠道包的标示内容
		 * 
		 * 1.拿到写在MEAT-INF文件中的不同的标示code
		 * 2.从本地拿到该手机的IMEI号码
		 * 3.拿到定位成功后需要的地址
		 * 4.将上面获取到的两个参数，一起作为接口的参数，上传到服务器上
		 */

		String phoneIMEI = getPhoneIMEI();
		String channelCode = getChannelCode(App.getInstance());
		int intChannelCode = 0;
		if(channelCode.equals("default")){
			//NO-OP
		}else{
			intChannelCode = Integer.valueOf(channelCode);
		}
		
		String cityName = App.app.getData("cityName");
		int version = getVersion();
		int user_id = -1;
		
		
		LogUtils.toDebugLog("start", "phoneIMEI :"+phoneIMEI);
		LogUtils.toDebugLog("start", "channelCode :"+channelCode);
		LogUtils.toDebugLog("start", "cityName :"+cityName);
		LogUtils.toDebugLog("start", "user_id :"+"原始的user_id: "+user_id);
		LogUtils.toDebugLog("start", "version :"+version);

		HttpUtils httpUtils = new HttpUtils();
		RequestCallBack<String> requestCallBack = new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException e, String result ) {
			}
			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				LogUtils.toDebugLog("start", "已提交至后台记录");
				LogUtils.toDebugLog("start", arg0.result.toString());
				
			}
		};

		

		
		//CommitStaticsinfoUtils.mStatistics_situation = statistics_situation;
		switch(statistics_situation){
			
		case 1://下载后第一次打开应用程序
			httpUtils.send(HttpMethod.POST, "http://appapi.qmyo.com/statistic/collection?" +
					"device=Android" +
					"&device_id="+phoneIMEI+
					"&platform="+intChannelCode+
					"&city="+cityName+
					"&version="+version, null,requestCallBack);
			
			break;
		case 2://用户登录时上传数据
			RequestParams requestParams = new RequestParams();
			requestParams.addHeader("Authorization", "Bearer" + App.app.getData("access_token"));
			
			user_id = GlobalValue.user.getId();
			LogUtils.toDebugLog("start", "user_id :"+"新的user_id: "+user_id);
			httpUtils.send(HttpMethod.POST, "http://appapi.qmyo.com/statistic/collection?" +
					"device=Android" +
					"&device_id="+phoneIMEI+
					"&platform="+intChannelCode+
					"&city="+cityName+
					"&version="+version+
					"&user_id="+user_id , requestParams,requestCallBack );
			break;
		}
	}
	
	/*
	 * 手机IMEI唯一标示符
	 */
	public static String  getPhoneIMEI(){
		TelephonyManager TelephonyMgr = (TelephonyManager)(App.app.getSystemService("phone")); 
		
		String szImei = TelephonyMgr.getDeviceId(); // Requires READ_PHONE_STATE
		//CustomToast.show(getApplicationContext(), "deviceImei", szImei);
		LogUtils.toDebugLog("imei", "设备的deviceImei： "+szImei);
		
		return szImei;
	}
	
	public static String getChannelCode(Context context){
		String mChannel = "";
		if(!TextUtils.isEmpty(mChannel)){
	        return mChannel;
	    }
	    mChannel = "default";
	 
	    ApplicationInfo appinfo = context.getApplicationInfo();
	    String sourceDir = appinfo.sourceDir;
	    //Log.d("getChannel sourceDir", sourceDir);
	 
	    ZipFile zf = null;
	    InputStream in = null;
	    ZipInputStream zin = null;
	 
	    try {
	        zf = new ZipFile(sourceDir);
	        in = new BufferedInputStream(new FileInputStream(sourceDir));
	        zin = new ZipInputStream(in);
	 
	        ZipEntry ze;
	        Enumeration<?> entries = zf.entries();
	 
	        while (entries.hasMoreElements()) {
	            ZipEntry entry = ((ZipEntry) entries.nextElement());
	            //Log.d("getChannel getName", entry.getName());
	            if( entry.getName().equalsIgnoreCase("META-INF/channel_info")){
	                long size = entry.getSize();
	                if (size > 0) {
	                    BufferedReader br = new BufferedReader( new InputStreamReader(zf.getInputStream(entry)));
	                    String line;
	                    while ((line = br.readLine()) != null) {
	                        mChannel = line;
	                    }
	                    br.close();
	                }
	            }
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    finally {
	        if(in != null){
	            try {
	                in.close();
	            }
	            catch (Exception e){
	            }
	        }
	        if(zin != null){
	            try {
	                zin.closeEntry();
	            }
	            catch (Exception e){
	            }
	        }
	 
	        if(zf != null){
	            try {
	                zin.closeEntry();
	            }
	            catch (Exception e){
	            }
	        }
	        try {
				zf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    Log.d("getChannel", mChannel);
	    return mChannel;
	}
	
	/*
	 * 获取版本号
	 */
	public static int getVersion(){
		PackageInfo info = null;
		PackageManager manager = App.app.getPackageManager();
		try {
		  info = manager.getPackageInfo(App.app.getPackageName(), 0);
		} catch (NameNotFoundException e) {
		  e.printStackTrace();
		}
		return info.versionCode;
	}
	
}
