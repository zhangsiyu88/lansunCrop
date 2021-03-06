package com.lansun.qmyo;

import java.util.Timer;
import java.util.TimerTask;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.mapcore2d.ez;

import com.android.pc.ioc.app.Ioc;
import com.android.pc.ioc.db.sqlite.Selector;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectLayer;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.util.EvilTransform;
import com.android.pc.util.Gps;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.android.pc.util.Handler_System;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.AdCode;
import com.lansun.qmyo.domain.Sensitive;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.view.CustomToast;
/**
 * 欢迎界面
 * 
 * @author bhxx
 * 
 */
@InjectLayer(R.layout.activity_splash)
public class SplashActivity extends FragmentActivity {
	private boolean isDebug = false;
	Timer timer = new Timer();
	private LocationManagerProxy aMapLocManager = null;
	private AMapLocation aMapLocation;// 用于判断定位超时
	private boolean isTip;
	
	/**
	 * 定位监听
	 */
	private AMapLocationListener locationListener = new AMapLocationListener() {
		
		@Override
		public void onLocationChanged(AMapLocation location) {
			
			/* 来自高德API文档：错误码集合
			public static final int ERROR_CODE_FAILURE_AUTH	7
			public static final int	ERROR_CODE_FAILURE_CELL	11
			public static final int	ERROR_CODE_FAILURE_CONNECTION	4
			public static final int	ERROR_CODE_FAILURE_INIT	9
			public static final int	ERROR_CODE_FAILURE_LOCATION	6
			public static final int	ERROR_CODE_FAILURE_LOCATION_PARAMETER	3
			public static final int	ERROR_CODE_FAILURE_LOCATION_PERMISSION	12
			public static final int	ERROR_CODE_FAILURE_PARSER	5
			public static final int	ERROR_CODE_FAILURE_WIFI_INFO	2
			public static final int	ERROR_CODE_INVALID_PARAMETER	1
			public static final int	ERROR_CODE_SERVICE_FAIL	10
			public static final int	ERROR_CODE_UNKNOWN	8
			public static final int	LOCATION_SUCCESS	0
			public static final int	LOCATION_TYPE_CELL	6
			public static final int	LOCATION_TYPE_FAST	3
			public static final int	LOCATION_TYPE_FIX_CACHE	4
			public static final int	LOCATION_TYPE_GPS	1
			public static final int	LOCATION_TYPE_OFFLINE	8
			public static final int	LOCATION_TYPE_SAME_REQ	2
			public static final int	LOCATION_TYPE_WIFI	5*/
			
			//ERROR_CODE_FAILURE_CONNECTION	4
			if(location != null && location.getAMapException().getErrorCode() == 4){
				Toast.makeText(SplashActivity.this, "网络连接异常，请检查网络", Toast.LENGTH_SHORT).show();
			}
			//ERROR_CODE_FAILURE_INIT	9
			if(location != null && location.getAMapException().getErrorCode() == 9){
				Toast.makeText(SplashActivity.this, "定位服务初始化异常", Toast.LENGTH_SHORT).show();
			}
			//ERROR_CODE_FAILURE_WIFI_INFO	2
			if(location != null && location.getAMapException().getErrorCode() == 2){
				Toast.makeText(SplashActivity.this, "无线网络异常，请检查网络", Toast.LENGTH_SHORT).show();
			}
			//ERROR_CODE_UNKNOWN	8
			if(location != null && location.getAMapException().getErrorCode() == 8){
				Toast.makeText(SplashActivity.this, "定位服务启动失败，请重试", Toast.LENGTH_SHORT).show();
			}
			//LOCATION_SUCCESS	0
			if (location != null && location.getAMapException().getErrorCode() == 0) {
				aMapLocation = location;// 判断超时机制
				aMapLocation.getAddress();
				
				Double geoLat = location.getLatitude();
				Double geoLng = location.getLongitude();
				String desc = "";
				final Bundle locBundle = location.getExtras();
				/*
				 * 当使用gps和Agps进行定位时,需要纠偏处理
				 */
				/*Gps hasChangedGps = gps84_To_Gcj02(geoLat,geoLng);
				GlobalValue.gps = hasChangedGps;
				Log.i("Gps定位的纬度", String.valueOf(location.getLatitude()));
				Log.i("Gps定位的经度", String.valueOf(location.getLongitude()));
				
				Log.i("进行纠偏后并转回double值的Gps定位的纬度", String.valueOf(GlobalValue.gps.getWgLat()));
				Log.i("进行纠偏后并转回double值的Gps定位的经度", String.valueOf(GlobalValue.gps.getWgLon()));
				
				
				/*
				 * 当使用高德本身的混合纠偏时,本身拿到的定位就是已经进行纠偏过的
				 */
				GlobalValue.gps = new Gps(geoLat, geoLng);
				Log.i("Gps定位的纬度", String.valueOf(location.getLatitude()));
				Log.i("Gps定位的经度", String.valueOf(location.getLongitude()));
				Log.i("进行纠偏后并转回double值的Gps定位的纬度", String.valueOf(GlobalValue.gps.getWgLat()));
				Log.i("进行纠偏后并转回double值的Gps定位的经度", String.valueOf(GlobalValue.gps.getWgLon()));
				 if (aMapLocation != null) {//一旦定位成功就不需要一直定位监听了
					stopLocation();
				}

				InternetConfig config = new InternetConfig();
				config.setKey(0);
				 /** 如果定位失败,没拿到定位地点,那么自动将其转向默认地址(暂定为上海)去加载数据*/
				if(location.getLatitude()==0||location.getLongitude()==0){//只要高德地图定位正常,就不会走下面的操作           //Gps的权限被关闭后
					
					/*geoLat = 31.293688;
					geoLng = 121.524448;*/
					Log.i("首页GPS", "location碎不为空,但拿到的是中心坐标(0,0),则走默认地址: 人民广场");
					geoLat = 31.230431;
					geoLng = 121.473705;
					//注意此处走默认地址,需将GlobalValue.gps 进行了重新的赋值,供后面使用Gps变量请求数据
					GlobalValue.gps = new Gps(31.230431, 121.473705);
					
					Log.i("走默认地址的纬度", String.valueOf(String.valueOf(GlobalValue.gps.getWgLat())));
					Log.i("走默认地址的经度", String.valueOf(String.valueOf(GlobalValue.gps.getWgLon())));
					
					if(App.app.getData("firstEnter").isEmpty()){
						App.app.setData("gpsIsNotAccurate","true");
					}
					
					FastHttpHander.ajaxGet(String.format(GlobalValue.URL_GPS_ADCODE, geoLng,
							geoLat), null, config, SplashActivity.this);
					
				}else{//否则,按照地位地点进行加载                                                                                                                     //两种情况：1. Gps关闭，但网络定位开启    2.GPS开启，网络定位也是开启的
					Log.i("首页GPS", "走定位地址");
					String url = "http://mo.amap.com/service/geo/getadcode.json?";
					
					/*if(App.app.getData("firstEnter").isEmpty()){
						App.app.setData("gpsIsNotAccurate","true");
					}*/
					FastHttpHander.ajaxGet( url+"longitude="+GlobalValue.gps.getWgLon()+"&latitude="+GlobalValue.gps.getWgLat(), null, config,
							SplashActivity.this);
				}
			}else{//关掉了 GPS定位和网络定位  //
				if(location!=null&&(location.getAMapException().getErrorCode() == 4
						||location.getAMapException().getErrorCode() == 6
						||location.getAMapException().getErrorCode() == 9
						||location.getAMapException().getErrorCode() == 12
						||location.getAMapException().getErrorCode() == 10
						||location.getAMapException().getErrorCode() == 8
						)){
					Toast.makeText(SplashActivity.this, "网络连接异常，检查网络哟~", Toast.LENGTH_LONG).show();
				}
				
				
				
				Log.i("Location是不是为空？", "location居然是空！！！location == null");
				Log.i("首页GPS", "location为空时走默认地址: 人民广场");
				
				if(App.app.getData("firstEnter").isEmpty()){
					/**
					 * @type TAG
					 * 标记：未获得精确定位的权限
					 */
					App.app.setData("gpsIsNotAccurate","true");
				}
				
				InternetConfig config = new InternetConfig();
				config.setKey(0);
				config.setTimeout(5000);
				double baseLat = 31.230431;
				double baseLng = 121.473705;
				//注意此处走默认地址,需将GlobalValue.gps 进行了重新的赋值,供后面使用Gps变量请求数据
				GlobalValue.gps = new Gps(31.230431, 121.473705);
				
				Log.i("走默认地址的纬度", String.valueOf(String.valueOf(GlobalValue.gps.getWgLat())));
				Log.i("走默认地址的经度", String.valueOf(String.valueOf(GlobalValue.gps.getWgLon())));
				/*FastHttpHander.ajaxGet(String.format(GlobalValue.URL_GPS_ADCODE, baseLng, baseLat), null, config, SplashActivity.this);*/
				String url = "http://mo.amap.com/service/geo/getadcode.json?";
				FastHttpHander.ajaxGet( url+"longitude="+GlobalValue.gps.getWgLon()+"&latitude="+GlobalValue.gps.getWgLat(), null, config,
						SplashActivity.this);
			}

			
			/*if (paramAnonymousAMapLocation != null)
		      {
		        SplashActivity.this.aMapLocation = paramAnonymousAMapLocation;
		        SplashActivity.this.aMapLocation.getAddress();
		        Double.valueOf(paramAnonymousAMapLocation.getLatitude());
		        Double.valueOf(paramAnonymousAMapLocation.getLongitude());
		        paramAnonymousAMapLocation.getExtras();
		        if (SplashActivity.this.aMapLocation != null)
		        {
		          GlobalValue.gps = new Gps(paramAnonymousAMapLocation.getLatitude(), paramAnonymousAMapLocation.getLongitude());
		          SplashActivity.this.stopLocation();
		        }
		        InternetConfig localInternetConfig = new InternetConfig();
		        localInternetConfig.setKey(0);
		        String str = GlobalValue.URL_GPS_ADCODE;
		        Object[] arrayOfObject = new Object[2];
		        arrayOfObject[0] = Double.valueOf(paramAnonymousAMapLocation.getLongitude());
		        arrayOfObject[1] = Double.valueOf(paramAnonymousAMapLocation.getLatitude());
		        FastHttpHander.ajaxGet(String.format(str, arrayOfObject), null, localInternetConfig, SplashActivity.this);
		      }*/
		}

		@Override
		public void onLocationChanged(Location arg0) {
		}

		@Override
		public void onProviderDisabled(String arg0) {
		}

		@Override
		public void onProviderEnabled(String arg0) {
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		}
	};
	private long exitTime = (long) 0.0;

	@Override
	protected void onPause() {
		JPushInterface.onPause(this);
		super.onPause();
	}

	/**
	 * 对GPS获取到的经纬度数据进行了 国标局向国测局坐标体系的转换操作
	 */
	protected Gps gps84_To_Gcj02(Double Latitude, Double Longtitude) {
		//对国际标准的坐标进行 gps84_To_Gcj02偏移操作
		Gps gpsItem = EvilTransform.gps84_To_Gcj02(Latitude, Longtitude);
		
		//gpsItem.setWgLat(gpsItem.getWgLat().)
		double wgLat = gpsItem.getWgLat();
		String wgLatStr = String.valueOf(wgLat);
		int latPointAt = wgLatStr.indexOf(".");
		//Log.i("小数点是那个位置上的呢?","wgLatStr中的: "+latPointAt);
		wgLatStr = wgLatStr.substring(0, latPointAt+7);
		
		double wgLon = gpsItem.getWgLon();
		String wgLonStr = String.valueOf(wgLon);
		int lonPointAt = wgLonStr.indexOf(".");
		//Log.i("小数点是那个位置上的呢?","wgLonStr中的: "+lonPointAt);
		wgLonStr = wgLonStr.substring(0, lonPointAt+7);//substring的end位是不包括在内的
		
		/*Log.i("进行纠偏后并且截取位数后的Gps定位的纬度", wgLatStr);
		  Log.i("进行纠偏后并且截取位数后的Gps定位的经度", wgLonStr);*/
		
		return new Gps(Double.valueOf(wgLatStr), Double.valueOf(wgLonStr));
	}

	@Override
	protected void onResume() {
		JPushInterface.onResume(this);
		super.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
//		setContentView(R.layout.activity_splash);
		
	//---后期修改
		 if (((LocationManager) getSystemService("location")).isProviderEnabled("gps")){
			 
		      Intent localIntent = new Intent();
		      localIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
		      localIntent.addCategory("android.intent.category.ALTERNATIVE");
		      localIntent.setData(Uri.parse("3"));
		      sendBroadcast(localIntent);
		    }
		 
		   
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			//版本高于4.4，那么进行系统状态栏透明化处理
			setTranslucentStatus(true);
		}
		
		//原本在App.java中的获取敏感词数据库的操作放到SplashActivity界面来
		new Thread()
		    {
		      public void run()
		      {
		        Selector localSelector = Selector.from(Sensitive.class);
		        localSelector.select(new String[] { " * " });
		        //localSelector.limit(2147483647);
		        GlobalValue.sensitiveList = Ioc.getIoc().getDb(SplashActivity.this.getCacheDir().getPath(), "qmyo_sensitive.db").findAll(localSelector);

		      }
		    }.start();
		//------------------
		    
		Handler_Inject.injectFragment(this, null); 
		super.onCreate(savedInstanceState);
	}

	
	//设置Window为透明状态
    private void setTranslucentStatus(boolean on) {  
        Window win = getWindow();  
        WindowManager.LayoutParams winParams = win.getAttributes();  
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;  
        if (on) {  
            winParams.flags |= bits;  
        } else {  
            winParams.flags &= ~bits;  
        }  
        win.setAttributes(winParams);  
    }  
    
	/**
	 * 初始化去获取物理数据
	 */
	@InjectInit
	private void init() {
		
		TelephonyManager TelephonyMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE); 
		String szImei = TelephonyMgr.getDeviceId(); // Requires READ_PHONE_STATE
		//CustomToast.show(getApplicationContext(), "deviceImei", szImei);
		LogUtils.toDebugLog("id", "设备的deviceImei： "+szImei);
		
		String sdk = android.os.Build.VERSION.SDK;
		String brand = android.os.Build.BRAND;
		String id = android.os.Build.ID;
		String device = android.os.Build.DEVICE;
		
		LogUtils.toDebugLog("id", "设备的id： "+id);
		LogUtils.toDebugLog("id", "设备的device： "+device);
		
		if(Integer.valueOf(sdk) < 17){
			DialogUtil.createTipAlertDialog(SplashActivity.this,
					"亲爱的 "+brand+" 用户"+"\n"+"迈界暂不支持Android4.2以下版本的服务",
					new DialogUtil.TipAlertDialogCallBack() {
						@Override
						public void onPositiveButtonClick(
								DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
						}
						@Override
						public void onNegativeButtonClick(
								DialogInterface dialog, int which) {
							dialog.dismiss();
							/*finish();*/
						}
					});
			return;
		}
		
		isTip = true;
		if (!isDebug) {//非debug状态
			 locationData();
			
		} else {
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					finish();
					startActivity(new Intent(SplashActivity.this, MainActivity.class));
				}
			}, 1500);
		}
	}

	/**
	 * 销毁定位
	 */
	private void stopLocation() {
		if (aMapLocManager != null) {
			aMapLocManager.removeUpdates(locationListener);
			aMapLocManager.destroy();
		}
		aMapLocManager = null;
	}

	protected void locationData() {
		aMapLocManager = LocationManagerProxy.getInstance(this);
		/*
		 * Notice!!! 这里定位使用的是LocationManagerProxy.NETWORK_PROVIDER,没有使用高德地图的 混合定位！！！
		 *  高德的混合定位：
		 *  mLocationManagerProxy.requestLocationData(
                LocationProviderProxy.AMapNetwork, 60*1000, 15, this);
		 */
	/*	aMapLocManager.setGpsEnable(true);*/
	/*	aMapLocManager.requestLocationData("lbs", 2000L, 10.0F, this.locationListener);*/
		/*aMapLocManager.requestLocationData(LocationManagerProxy.NETWORK_PROVIDER, 60*1000 , 10, locationListener);*/
		aMapLocManager.requestLocationData(LocationProviderProxy.AMapNetwork, 5*1000 , 10, locationListener);
		
		/*三种情况： 允许后便开启了高德的定位服务；
				 未开启高德定位服务，走的是默认的地址“人民广场”
		*/
	}

	@Override
	protected void onDestroy() {
		stopLocation();// 停止定位
		aMapLocation = null;
		aMapLocManager = null;
		super.onDestroy();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {//对物理返回键做了拦截操作，让返回失效掉
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if((System.currentTimeMillis()-exitTime  ) > 1000){  
				Toast.makeText(SplashActivity.this, "网络异常，再按一次退出迈界哦~", Toast.LENGTH_LONG).show();
	            exitTime = System.currentTimeMillis();   
	        } else {
	        	finish();
	        }
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			/*Toast.makeText(SplashActivity.this, "迈界初始化完成，欢迎进入~", Toast.LENGTH_LONG).show();*/
			
			switch (r.getKey()) {
			case 0:
				final AdCode code = Handler_Json.JsonToBean(AdCode.class,r.getContentAsString());
				
				Log.i("服务器端到底返回了什么玩意？",r.getContentAsString());
				/*10-12 11:02:31.642: I/服务器端到底返回了什么玩意？(3739): {"result":false,"code":"0","message":"缺少必要参数 longitude, latitude"}
				 * 由上面返回的信息可知： 移动端请求出现错误！
				 * 原因： longitude 和 latitude之间丢失了一个 &
				 * */
				Log.i("code是否有值",code.toString());
				Log.i("code是否有值",code.getAdcode().toString());
				
				Log.i("isTip是否有值",String.valueOf(isTip));
				if (isTip) { //isTip的含义
					isTip = false;
					
					Log.i("select_cityName是否有值",String.valueOf(App.app.getData("select_cityName")));
					
					if (TextUtils.isEmpty(App.app.getData("select_cityName"))) {//如果本地中的select_cityName对应的值为空的话，则将网络获取到的值写入本地，供以后判断使用
						DialogUtil.createTipAlertDialog(SplashActivity.this,
								R.string.use_city,
								new DialogUtil.TipAlertDialogCallBack() {//暂时没有位置信息，是否使用当前位置？  -->是否使用当前GPS定位城市
									@Override
									public void onPositiveButtonClick(
											DialogInterface dialog, int which) {
										dialog.dismiss();
										//TODO
										timer.schedule(new TimerTask() {
											@Override
											public void run() {
											App.app.setData("cityCode",
													code.getAdcode());
											App.app.setData("cityName",
													code.getCity());
											App.app.setData("select_cityCode",
													code.getAdcode());
											App.app.setData("select_cityName",
													code.getCity());
										
											Log.i("cityCode()的值为",App.app.getData("cityCode"));
											Log.i("cityName()的值为",App.app.getData("cityName"));
											Log.i("select_cityCode()的值为",App.app.getData("select_cityCode"));
											Log.i("select_cityName()的值为",App.app.getData("select_cityName"));
											App.app.setData("isPos", "true");
											
											finish();
											startActivity(new Intent(SplashActivity.this,MainActivity.class));
											}
										}, 1500);
										stopLocation();
									}

									@Override
									public void onNegativeButtonClick(
											DialogInterface dialog, int which) {
										dialog.dismiss();
										timer.schedule(new TimerTask() {
											@Override
											public void run() {
												App.app.setData("cityCode",
														"310000");
												App.app.setData("cityName",
														"上海市");
												App.app.setData("select_cityCode",
														"310000");
												App.app.setData("select_cityName",
														"上海市");
											
												Log.i("cityCode()的值为",App.app.getData("cityCode"));
												Log.i("cityName()的值为",App.app.getData("cityName"));
												Log.i("select_cityCode()的值为",App.app.getData("select_cityCode"));
												Log.i("select_cityName()的值为",App.app.getData("select_cityName"));
												
												//将GPS设置为人民广场在高德地图中的经纬度
												GlobalValue.gps = new Gps(31.230431, 121.473705);
												
												App.app.setData("isPos", "false");
												
												finish();
												startActivity(new Intent(SplashActivity.this,MainActivity.class));
											}
										}, 1500);
										stopLocation();
									}
								});
					} else {//按select_cityName取得的值并不为空，即select_cityName肯定是有值
						    //并且这次GPS检测到返回的值city_code和以前写在本地的city_code值不相等时
						Log.i("select_cityCode是否有值",String.valueOf(App.app.getData("select_cityCode")));
						
						if (!code.getAdcode().equals(App.app.getData("select_cityCode"))) {
							Log.i("code.getAdcode()的值为",code.getAdcode());//很有可能code.getCode没有值，或者App.app.getData("select_cityCode")没有值为null
							Log.i("select_cityCode()的值为",App.app.getData("select_cityCode"));
							
							DialogUtil.createTipAlertDialog(SplashActivity.this, R.string.switch_city,
									new DialogUtil.TipAlertDialogCallBack() {//您上次城市与此次城市不同是否切换为当前城市？
										@Override
										public void onPositiveButtonClick(DialogInterface dialog, int which) {
											
											dialog.dismiss();
											
											App.app.setData("cityCode",
													code.getAdcode());
											App.app.setData("cityName",
													code.getCity());
											App.app.setData("select_cityCode",
													code.getAdcode());
											App.app.setData("select_cityName",
													code.getCity());
											
											App.app.setData("isPos", "true");
											
											timer.schedule(new TimerTask() {
												@Override
												public void run() {
													finish();
													startActivity(new Intent(
															SplashActivity.this,
															MainActivity.class));
												}
											}, 1500);
											stopLocation();
										}

										@Override
										public void onNegativeButtonClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
											
											App.app.setData("isPos", "false");
											
											timer.schedule(new TimerTask() {
												@Override
												public void run() {
													finish();
													startActivity(new Intent(
															SplashActivity.this,
															MainActivity.class));
												}
											}, 1500);
											stopLocation();
										}
									});
						} else {
							finish();
							startActivity(new Intent(SplashActivity.this,MainActivity.class));
						}
					}
				}
				break;
			}
		}

	}
}
