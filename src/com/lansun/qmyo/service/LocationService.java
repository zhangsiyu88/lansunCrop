package com.lansun.qmyo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
/*import cn.letsbook.running.model.FixedLengthList;
import cn.letsbook.running.util.Constants;*/

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.android.pc.util.Gps;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.view.CustomToast;

	/**
	 * 定位服务,基于高德的定位API V1.3.0实现
	 * 
	 * @author Yeun
	 *
	 */
	public class LocationService extends Service implements AMapLocationListener {
	    
		public static final long LOCATION_UPDATE_MIN_TIME = 30 * 1000;
	    public static final float LOCATION_UPDATE_MIN_DISTANCE = 1;

	    //private FixedLengthList<AMapLocation> locationList = FixedLengthList.newInstance();
	    
	    // 位置服务代理
	    private LocationManagerProxy locationManagerProxy;

	    public LocationService() {

	    }

	    @Override
	    public void onCreate() {
	        super.onCreate();
	        //使用参数为Context的方法，Service也是Context实例，是四大组件之一
	        locationManagerProxy = LocationManagerProxy.getInstance(this);
	        // 定位方式设置为混合定位，包括网络定位和GPS定位
	        locationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, LOCATION_UPDATE_MIN_TIME,LOCATION_UPDATE_MIN_DISTANCE, this);
	        // 如果定位方式包括GPS定位需要手动设置GPS可用
	        locationManagerProxy.setGpsEnable(true);
	        Log.v("locationservice", "locationservicestart");
	    }

	    @SuppressWarnings("deprecation")
	    @Override
	    public void onDestroy() {
	        // 在Service销毁的时候销毁定位资源
	        if (locationManagerProxy != null) {
	            locationManagerProxy.removeUpdates(this);
	            locationManagerProxy.destory();
	        }
	        //设置为null是为了提醒垃圾回收器回收资源
	        locationManagerProxy = null;
//	        Toast.makeText(App.app, "定位服务被销毁", Toast.LENGTH_SHORT).show();
	        super.onDestroy();
	    }

	    
	    
	    
	    
	    @Override
	    public void onLocationChanged(Location location) {
	        //在较新的SDK版本中，这个方法在位置发生变化的时候不会被
	        //调用。这个方法默认是使用原生GPS服务的时候，当位置
	        //变化被调用的方法。
	    }

	    @Override
	    public void onStatusChanged(String provider, int status, Bundle extras) {

	    }

	    @Override
	    public void onProviderEnabled(String provider) {

	    }

	    @Override
	    public void onProviderDisabled(String provider) {

	    }

	    //当位置发生变化的时候调用这个方法。
	    @Override
	    public void onLocationChanged(AMapLocation aMapLocation) {
	        // 如果位置获取错误则不作处理，退出本方法
	        // 返回错误码如果为0则表明定位成功，反之则定位失败
	        // 在虚拟机测试的时候，返回错误码31，为未知错误
	        // 如果使用虚拟机测试的时候遇到这个问题，建议使用真机测试。
	        if (aMapLocation == null|| aMapLocation.getAMapException().getErrorCode() != 0) {
	            Log.v("locationservice", aMapLocation == null ? "null" : "not null");
	            Toast.makeText(App.app, "定位异常，请检查网络", Toast.LENGTH_LONG).show();
	            
	            if (aMapLocation != null) {
	                Log.v("locationservice", "errorcode" + aMapLocation.getAMapException().getErrorCode());
	                Log.v("locationservice", "errormessage"+ aMapLocation.getAMapException().getErrorMessage());
	            }
	            Log.v("locationservice", "request error");
	            return;
	        }
	        
//	        if(aMapLocation.getLatitude()==0||aMapLocation.getLongitude()==0){
//	        	CustomToast.show(this, "迈界提示", "请前往权限页面打开定位权限");
//	        }
	        
	        if(aMapLocation.getLatitude()!=0 && aMapLocation.getLongitude()!=0){
				if(App.app.getData("firstEnter").isEmpty()){
					App.app.setData("gpsIsNotAccurate","");//将gps的提醒标签置为空
					App.app.setData("firstEnter","notblank");//但此时已不是第一次进入
				}
			}
	        
	        
	        //locationList是一个自定义实现的泛型类，
	        //用于实现定长固定列表的功能。
	       /* locationList.addElement(aMapLocation);
	          Log.v("test", locationList.getLastElement().toString()); */

	        // 发送广播传送地点位置信息到地图显示界面
	        // 当数据正常获取的时候，把位置信息通过广播发送到接受方,
	        // 也就是需要处理这些数据的组件。
	        
	        //将新请求下来的坐标写入到本地的Gps中
//	        GlobalValue.gps.setWgLat(aMapLocation.getLatitude());
//	        GlobalValue.gps.setWgLon(aMapLocation.getLongitude());
//	        
	    	if(!(App.app.getData("select_cityCode").equals(App.app.getData("cityCode")))){
				//如果是当前定位城市 不是 你所选中的城市，那么前往访问的就是： 默认城市中的默认position
				//Gps的值不做更新，维持之前写定的Gps的值
	    		Log.d("更新的坐标","更新的操作在进行，但Gps值并未替换");
			}else{
				if(App.app.getData("isPos")!=null &&
						!(App.app.getData("isPos").equals("false"))){
					//如果是当前定位城市 正是 你所选中的城市，那么前往访问的就是：当前定位到的position值
					correctGpsCoordinateAndSetData(aMapLocation.getLatitude(),aMapLocation.getLongitude());
					Log.d("更新的坐标","高德更新的坐标:"+aMapLocation.getLatitude()+","+aMapLocation.getLongitude());
					Log.d("更新的坐标","修整为固定小数点后六位的坐标:"+GlobalValue.gps.getWgLat()+","+GlobalValue.gps.getWgLon());
				}
			}
	        //Toast.makeText(App.app, "坐标已更新。。。", Toast.LENGTH_SHORT).show();
	        
	        // CustomToast.show(getApplicationContext(), "更新坐标位置", "已更新");
	        //CustomToast.show(getApplicationContext(), "纬度：", ""+aMapLocation.getLatitude());
	        //CustomToast.show(getApplicationContext(), "经度：", ""+aMapLocation.getLongitude());
	        
	       /* 原本的数据
	        * Intent intent = new Intent();
	          intent.setAction(Constants.INTENT_ACTION_UPDATE_DATA);
	          intent.putExtra(Constants.INTENT_ACTION_UPDATE_DATA_EXTRA_LATITUDE,aMapLocation.getLatitude());
	          intent.putExtra(Constants.INTENT_ACTION_UPDATE_DATA_EXTRA_LONGITUDE,aMapLocation.getLongitude());
	          this.sendBroadcast(intent);*/
	    }

		@Override
		public IBinder onBind(Intent arg0) {
			return null;
		}
		
		/**
		 * 返回修整过的坐标
		 * @param wgLat
		 * @param wgLon
		 * @return
		 */
		public void correctGpsCoordinateAndSetData(double wgLat,double wgLon){
			
			double newWgLat = interceptDoubleNum(wgLat);
			double newWgLon = interceptDoubleNum(wgLon);
			GlobalValue.gps.setWgLat(newWgLat);
		    GlobalValue.gps.setWgLon(newWgLon);
		    
		    App.app.setData("gps.Wglat", String.valueOf(newWgLat));
		    App.app.setData("gps.Wglon", String.valueOf(newWgLon));
		}
		/**
		 * 截取到输入数字的后六位
		 */
		private double interceptDoubleNum(double baseNumber) {
			String newNumber = String.valueOf(baseNumber);
			int littlePoint = newNumber.indexOf(".");
			newNumber = newNumber.substring(0, littlePoint+7);
			double resultDoubleNum = Double.valueOf(newNumber);
			return resultDoubleNum;
		}
 }
