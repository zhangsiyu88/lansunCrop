package com.lansun.qmyo.fragment;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.location.core.CoordinateConvert;
import com.amap.api.location.core.GeoPoint;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.SupportMapFragment;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.maps2d.model.NaviPara;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.EvilTransform;
import com.android.pc.util.Gps;
import com.android.pc.util.Handler_Inject;
import com.lansun.qmyo.adapter.CommonAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.R;

public class MapFragment extends BaseFragment implements LocationSource,
		AMapLocationListener {

	@InjectAll
	Views v;
	private AMap aMap;
	private UiSettings mUiSettings;
	private OnLocationChangedListener mListener;
	private LocationManagerProxy aMapManager;
	private AMapLocation aMapLocation;
	private String shopName;
	private String shopAddress;
	private SupportMapFragment fragment;
	private LatLng latLng;

	class Views {
		private TextView shop_name, shop_address;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View tv_map_navigation;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_map, null);
		//
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		shopName = getArguments().getString("shopname");
		shopAddress = getArguments().getString("shopaddress");
		v.shop_name.setText(shopName);
		v.shop_address.setText(shopAddress);
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		myLocationStyle.myLocationIcon(BitmapDescriptorFactory
				.fromResource(R.drawable.icon));// 设置小蓝点的图标
		myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
		myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
		// myLocationStyle.anchor(int,int)//设置小蓝点的锚点
		myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细

		if (aMap == null) {
			fragment = ((SupportMapFragment) getChildFragmentManager()
					.findFragmentById(R.id.map));
			aMap = fragment.getMap();
		}
		mUiSettings = aMap.getUiSettings();
		mUiSettings.setZoomControlsEnabled(false);
		mUiSettings.setCompassEnabled(false);
		mUiSettings.setScaleControlsEnabled(true);
		mUiSettings.setMyLocationButtonEnabled(true);
		aMap.setMyLocationEnabled(true);
		String Lat = App.app.getData("TargetPointLat");
		String Lng = App.app.getData("TargetPointLng");
		latLng = new LatLng(Double.parseDouble(Lat),Double.parseDouble(Lng));
		/*LatLng shopLatLng = new LatLng(GlobalValue.gps.getWgLat(),
				GlobalValue.gps.getWgLon());*/
		LatLng shopLatLng = latLng;
		CameraUpdate came = CameraUpdateFactory
				.newCameraPosition(new CameraPosition(shopLatLng, 18, 30, 0));
		aMap.animateCamera(came);
		MarkerOptions shopMarker = new MarkerOptions().anchor(0.5f, 0.5f)
				.position(shopLatLng).title(shopName).snippet(shopAddress);
		aMap.addMarker(shopMarker);
		aMap.setLocationSource(this);// 设置定位监听
	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		if (aMapManager == null) {
			aMapManager = LocationManagerProxy.getInstance(activity);
			aMapManager.requestLocationData(LocationProviderProxy.AMapNetwork,
					2000, 10, this);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		stopLocation();// 停止定位
	}

	/**
	 * 销毁定位
	 */
	private void stopLocation() {
		if (aMapManager != null) {
			aMapManager.removeUpdates(this);
			aMapManager.destroy();
		}
		aMapManager = null;
	}

	@Override
	public void deactivate() {
		mListener = null;
		if (aMapManager != null) {
			aMapManager.removeUpdates(this);
			aMapManager.destroy();
		}
		aMapManager = null;
		unRegisterSensorListener();
	}

	public void registerSensorListener() {
	}

	public void unRegisterSensorListener() {
	}

	@Override
	public void onLocationChanged(Location location) {

	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChanged(AMapLocation aLocation) {
		if (mListener != null) {
			mListener.onLocationChanged(aLocation);// 显示系统小蓝点
		}
		if (aLocation != null) {
			this.aMapLocation = aLocation;// 判断超时机制
			Double geoLat = aLocation.getLatitude();
			Double geoLng = aLocation.getLongitude();
		}
	}

	private void click(View view) {
		switch (view.getId()) {
		case R.id.tv_map_navigation:
			
			/*此方法暂禁!
			 * LatLng latLng = new LatLng(GlobalValue.gps.getWgLat(),GlobalValue.gps.getWgLon());*/
			
			/*
			 * 使用本地存储来传递目标店铺的信息
			 */
			String Lat = App.app.getData("TargetPointLat");
			String Lng = App.app.getData("TargetPointLng");
			LatLng latLng = new LatLng(Double.parseDouble(Lat),Double.parseDouble(Lng));
			startAMapNav(latLng);
			break;
		}
	}

	/**
	 * 开启高德地图导航
	 */
	private void startAMapNav(final LatLng point) {

		Builder dialog = new AlertDialog.Builder(activity);
		/*dialog.setTitle(R.string.tip);*/
		dialog.setTitle("请选择本地已有的地图软件：");
		dialog.setItems(new String[] { "百度地图", "高德地图" }, new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int position) {
				if (position == 1) {
					NaviPara naviPara = new NaviPara();
					// 设置终点位置
					naviPara.setTargetPoint(point);
					// 设置导航策略，这里是避免拥堵
					naviPara.setNaviStyle(NaviPara.DRIVING_AVOID_CONGESTION);
					// 调起高德地图导航
					try {
						AMapUtils.openAMapNavi(naviPara, activity);
					} catch (com.amap.api.maps2d.AMapException e) {
						AMapUtils.getLatestAMapApp(activity);
					}
				} else {
					if (isAvilible(activity, "com.baidu.BaiduMap")) {// 传入指定应用包名
						Intent intent = null;
						try {
							;
							Gps gps = EvilTransform.gcj02_To_Bd09(
									point.latitude, point.longitude);
							if (aMapLocation != null) {
								intent = Intent
										.getIntent("intent://map/direction?origin=latlng:"
												+ aMapLocation.getLatitude()
												+ ","
												+ aMapLocation.getLongitude()
												+ "|name:"
												+ shopName
												+ "&destination="
												+ shopAddress
												+ "|latlng:"
												+ gps.toString()
												+ "&mode=driving&src=yourCompanyName|yourAppName#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
							} else {
								intent = Intent
										.getIntent("intent://map/direction?origin=latlng:"
												+ GlobalValue.gps.toString()
												+ "|name:"
												+ shopName
												+ "&destination="
												+ shopAddress
												+ "|latlng:"
												+ gps.toString()
												+ "&mode=driving&src=yourCompanyName|yourAppName#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
							}
							startActivity(intent); // 启动调用
						} catch (URISyntaxException e) {
							Uri uri = Uri
									.parse("market://details?id=com.baidu.BaiduMap");
							intent = new Intent(Intent.ACTION_VIEW, uri);
							startActivity(intent);
						}
					}else{
						CustomToast.show(activity, "迈界小贴士", "总裁大大,小迈没找到该地图软件哦");
					}
				}
			}
		});
		// 构造导航参数
		dialog.create();
		dialog.show();
	}

	private class MyAdapter extends CommonAdapter {

		private String[] data = new String[] { "百度地图", "高德地图" };

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public View view(int position, View convertView, ViewGroup parent) {
			CheckBox cb = new CheckBox(activity);
			cb.setText(data[position]);
			return cb;
		}

	}

	/**
	 * 判断是否有三方应用
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	private boolean isAvilible(Context context, String packageName) {
		final PackageManager packageManager = context.getPackageManager();
		List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
		// 用于存储所有已安装程序的包名
		List<String> packageNames = new ArrayList<String>();
		// 从pinfo中将包名字逐一取出，压入pName list中
		if (packageInfos != null) {
			for (int i = 0; i < packageInfos.size(); i++) {
				String packName = packageInfos.get(i).packageName;
				packageNames.add(packName);
			}
		}
		// 判断packageNames中是否有目标程序的包名，有TRUE，没有FALSE
		return packageNames.contains(packageName);
	}

}
