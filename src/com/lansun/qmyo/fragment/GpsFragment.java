package com.lansun.qmyo.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectListener;
import com.android.pc.ioc.inject.InjectMethod;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.internet.CallBack;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.util.AbCharacterParser;
import com.android.pc.ioc.view.AbLetterFilterListView;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.ioc.view.listener.OnItemClick;
import com.android.pc.ioc.view.listener.OnTextChanged;
import com.android.pc.util.Gps;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.android.pc.util.Handler_Ui;
import com.lansun.qmyo.MainActivity;
import com.lansun.qmyo.MainFragment;
import com.lansun.qmyo.SplashActivity;
import com.lansun.qmyo.adapter.CityListAdapter;
import com.lansun.qmyo.adapter.SearchHotAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.AdCode;
import com.lansun.qmyo.domain.City;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.service.LocationService;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lansun.qmyo.R;
import com.umeng.socialize.utils.Log;

/**
 * 地理定位界面
 * 
 * @author bhxx
 * 
 */
public class GpsFragment extends BaseFragment {
	// 定位
	private LocationManagerProxy aMapLocManager = null;
	private AMapLocation aMapLocation;// 用于判断定位超时
	
	private TextView tv_city_header_current_city;
	private LinearLayout city_gps_loading;
	private String currentState = "all";

	@InjectAll
	Views v;
	@InjectBinder(listeners = { OnItemClick.class }, method = "recordItemClick")
	private GridView gv_city_header_recent_visit;
	@InjectBinder(listeners = { OnItemClick.class }, method = "hotItemClick")
	private GridView gv_city_header_hot_city;

	// @InjectBinder(listeners = { OnItemClick.class }, method =
	// "overseasItemClick")
	// private GridView gv_city_header_overseas_city;
	// private RelativeLayout rl_city_header_overseas_city;
  
	class Views {
		@InjectBinder(listeners = { OnItemClick.class }, method = "itemClick")
		private ListView listView;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView tv_gps_cancle;
		
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView tv_city_all, tv_city_overseas;
		@InjectBinder(listeners = { OnTextChanged.class }, method = "changeText")
		private EditText et_home_search;
		@InjectView
		public AbLetterFilterListView letterView;
		@InjectView
		public LinearLayout ll_gps_overseas_no;
	
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		/*
		 * 进入GPSFragment中时，默认用户已经同意进行定位操作
		 * 即离开当前GPSFragment页面后，重新开启的定位服务应该是可以复写真实的坐标值
		 */
		App.app.setData("isPos", "true");
		
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.city_list, null);
		Handler_Inject.injectFragment(this, rootView);
	
		
		tv_city_header_current_city.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				if(localCityCode!=null&&localCity!=null){
					//将城市数据放入到
					/*HomeFragment fragment = new HomeFragment();*/
					MainFragment fragment = new MainFragment(0);
					Bundle args = new Bundle();
					args.putBoolean("restartGps", true);
					
					saveSelectCity(localCityCode, localCity);
					fragment.setArguments(args);
					FragmentEntity event = new FragmentEntity();
					event.setFragment(fragment);
					EventBus.getDefault().post(event);
				}
			}
		});
		return rootView;
	}

	@InjectInit
	private void init() {
		
		//1.停止程序中的location服务
		//2.初始化一下当前定位城市!
		MainActivity mainActivity = (MainActivity) activity;
		mainActivity.stopLocationService();
		
		/* 由于下方代码的locationData()方法中的AMap核心代码是全局单例的形式，当单例未被关闭时，会导致下方代码无效的情况，故移至界面搭建完成后再执行
		 * locationData();
		 * 代码移至290行前后
		 * */
		
		
		//headerView是 当前城市定位:一栏和下面的两个GridView(见下面)
		View headerView = LayoutInflater.from(activity).inflate(R.layout.city_header, null);
		
		//这个ListView的顶部加上了一个头部的View内容，底部是一个 AbsLetterListView
		v.listView.addHeaderView(headerView);
		
		city_gps_loading = (LinearLayout) v.listView 
				.findViewById(R.id.city_gps_loading);      //当前城市定位的正在进行
		
		tv_city_header_current_city = (TextView) v.listView
				.findViewById(R.id.tv_city_header_current_city);                                            

		gv_city_header_recent_visit = (GridView) v.listView
				.findViewById(R.id.gv_city_header_recent_visit);   //最近访问的城市
		gv_city_header_recent_visit.setOnItemClickListener(recordItemClick);

		gv_city_header_hot_city = (GridView) v.listView
				.findViewById(R.id.gv_city_header_hot_city);	//热门搜索的城市
		gv_city_header_hot_city.setOnItemClickListener(hotItemClick);
		
		
		

		// rl_city_header_overseas_city = (RelativeLayout) v.listView
		// .findViewById(R.id.rl_city_header_overseas_city);
		// gv_city_header_overseas_city = (GridView) v.listView
		// .findViewById(R.id.gv_city_header_overseas_city);
		// gv_city_header_overseas_city.setOnItemClickListener(overseasItemClick);

		InternetConfig config = new InternetConfig();
		config.setKey(0);
		HashMap<String, Object> head = new HashMap<>();
		head.put("Authorization", "Bearer " + App.app.getData("access_token"));
		config.setHead(head);
		FastHttpHander.ajaxGet(GlobalValue.URL_SEARCH_SITE + currentState,config, this);
		
		/*使用FastHttpHander去访问服务器
		InternetConfig config1 = new InternetConfig();
		config1.setKey(2);
		if(aMapLocation==null){
			System.out.println("aMapLocation为null!");
		}
		FastHttpHander.ajaxGet(String.format(GlobalValue.URL_GPS_ADCODE,116.950404 ,
				31.043848), config1, this);*/
		
	if(App.app.getData("gpsIsNotAccurate").equals("true")){
		
		DialogUtil.createTipAlertDialog(activity,
				"您还未开启精确定位哦\n\r请前往应用权限页开启",
				new DialogUtil.TipAlertDialogCallBack() {
					@Override
					public void onPositiveButtonClick(
							DialogInterface dialog, int which) {
						  Intent localIntent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
				          localIntent.setData(Uri.fromParts("package", "com.lansun.qmyo", null));
				          activity.startActivity(localIntent);//前往权限设置的页面
				          
				          if(App.app.getData("firstEnter").isEmpty()){
								App.app.setData("gpsIsNotAccurate","");//将gps的提醒标签置为空
								App.app.setData("firstEnter","notblank");//但此时已不是第一次进入
							}
				          dialog.dismiss();
					}
					@Override
					public void onNegativeButtonClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		}
	}

	/**
	 * 搜索内容监听
	 * 
	 * @param s
	 * @param start
	 * @param before
	 * @param count
	 */
	private void changeText(CharSequence s, int start, int before, int count) {
		if (TextUtils.isEmpty(s)) {
			v.tv_gps_cancle.setText(R.string.cancle);
			v.tv_gps_cancle.setTextColor(Color.parseColor("#939393"));
		} else {
			v.tv_gps_cancle.setText(R.string.search);
			v.tv_gps_cancle.setTextColor(getResources().getColor(R.color.app_green1));
		}
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			
			switch (r.getKey()) {
			case 0:   //拿到城市的返回结果，将返回城市的数据放到ListView中
				if (citys != null) {
					citys.clear();
				}
				String json = r.getContentAsString();
				/* parseCity(Json): 将返回的json串转换为HashMap<String, ArrayList<City>>
				 * fillGridView(citys):填充GridView对象的数据,将城市数据和gv对象挂上钩,如:近期访问和热门城市
				 * filledData(cityList):将获取到的城市名链表转换成 按拼音排序的链表
				 */
				try {
					citys = parseCity(json);
					fillGridView(citys);
					// rl_city_header_overseas_city.setVisibility(View.VISIBLE);
					ArrayList<City> cityList = citys.get("city");
					CityListAdapter mCityListAdapter = new CityListAdapter(activity, filledData(cityList));
					
					v.listView.setAdapter(mCityListAdapter);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				//在Init()方法中，之前由于stopLocation代码执行的速度问题，导致下方的locationData()实际操作无效，故将代码移至此处
				locationData();
				
				
				break;
			case 1:
				if (citys != null) {
					citys.clear();
				}
				String json1 = r.getContentAsString();
				try {
					citys = parseCity(json1);
					fillGridView(citys);
					ArrayList<City> countryList = citys.get("country");
					//rl_city_header_overseas_city.setVisibility(View.GONE);
					CityListAdapter mCityListAdapter = new CityListAdapter(activity, filledData(countryList));
					
					v.listView.setAdapter(mCityListAdapter);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case 2:
				Log.i("能不能走到这儿","说明拿到了访问服务器返回的值");
				AdCode code = Handler_Json.JsonToBean(AdCode.class,r.getContentAsString());
				
				localCity = code.getCity();//从网络获取而来的定位城市信息
				localCityCode = code.getAdcode();
				tv_city_header_current_city.setText(Html.fromHtml(String.format(getString(R.string.current_city),localCity)));
				city_gps_loading.setVisibility(View.GONE);
				break;
			}
		}
	}

	/**
	 * 填充adapter数据
	 * 
	 * @param citys
	 */
	private void fillGridView(HashMap<String, ArrayList<City>> citys) {
		ArrayList<City> recordCitys = citys.get("record");
		ArrayList<HashMap<String, String>> dataList = new ArrayList<>();
		for (City recordCity : recordCitys) {
			HashMap<String, String> map = new HashMap<>();
			map.put("tv_search_hot_ad", recordCity.getName());
			dataList.add(map);
		}
		SearchHotAdapter recentAdapter = new SearchHotAdapter(
				gv_city_header_recent_visit, dataList,
				R.layout.activity_search_hot_item);
		gv_city_header_recent_visit.setAdapter(recentAdapter);//最近访问搜索

		ArrayList<City> recommendCitys = citys.get("recommend");
		ArrayList<HashMap<String, String>> dataList1 = new ArrayList<>();
		for (City recommendCity : recommendCitys) {
			HashMap<String, String> map = new HashMap<>();
			map.put("tv_search_hot_ad", recommendCity.getName());
			dataList1.add(map);
		}
		SearchHotAdapter recommendAdapter = new SearchHotAdapter(
				gv_city_header_recent_visit, dataList1,
				R.layout.activity_search_hot_item);
		gv_city_header_hot_city.setAdapter(recommendAdapter);//热门推荐搜索

		if (!"foreign".equals(currentState)) {
			ArrayList<City> foreignCitys = citys.get("foreign");
			ArrayList<HashMap<String, String>> dataList2 = new ArrayList<>();
			for (City foreignCity : foreignCitys) {
				HashMap<String, String> map = new HashMap<>();
				map.put("tv_search_hot_ad", foreignCity.getName());
				dataList2.add(map);
			}
			SearchHotAdapter foreignAdapter = new SearchHotAdapter(
					gv_city_header_recent_visit, dataList2,
					R.layout.activity_search_hot_item);
			// gv_city_header_overseas_city.setAdapter(foreignAdapter);
		}

	}

	/**
	 * 解析城市数据
	 * 
	 * @param json
	 * @return
	 * @throws JSONException
	 */
	public HashMap<String, ArrayList<City>> parseCity(String json)
			throws JSONException {
		HashMap<String, ArrayList<City>> citys = new HashMap<>();
		
		JSONObject obj = new JSONObject(json);
		JSONObject data = obj.getJSONObject("data");
		JSONArray record = data.getJSONArray("record");
		ArrayList<City> recordCity = new ArrayList<>();
		if (record != null) {
			for (int i = 0; i < record.length(); i++) {
				City city = Handler_Json.JsonToBean(City.class, record.get(i)
						.toString());
				recordCity.add(city);
			}
			citys.put("record", recordCity);
		}
		JSONArray recommend = data.getJSONArray("recommend");
		if (recommend != null) {
			ArrayList<City> recommendCity = new ArrayList<>();
			for (int i = 0; i < recommend.length(); i++) {
				City city = Handler_Json.JsonToBean(City.class, recommend
						.get(i).toString());
				recommendCity.add(city);
			}
			citys.put("recommend", recommendCity);
		}
		if ("foreign".equals(currentState)) {
			JSONObject country = data.getJSONObject("country");
			if (country != null) {

				ArrayList<City> countryCity = new ArrayList<>();
				Iterator keys = country.keys();
				while (keys.hasNext()) {
					JSONArray array = country.getJSONArray(keys.next().toString());
					for (int i = 0; i < array.length(); i++) {
						City city2 = Handler_Json.JsonToBean(City.class, array
								.get(i).toString());
						countryCity.add(city2);
					}
				}
				citys.put("country", countryCity);
			}
		} else {
			JSONArray foreign = data.getJSONArray("foreign");
			if (foreign != null) {

				ArrayList<City> foreignCity = new ArrayList<>();
				for (int i = 0; i < foreign.length(); i++) {
					City city = Handler_Json.JsonToBean(City.class, foreign.get(i).toString());
					foreignCity.add(city);
				}
				citys.put("foreign", foreignCity);
			}
			JSONObject city = data.getJSONObject("city");
			if (city != null) {

				ArrayList<City> cityCity = new ArrayList<>();
				Iterator keys = city.keys();
				while (keys.hasNext()) {
					JSONArray array = city.getJSONArray(keys.next().toString());
					for (int i = 0; i < array.length(); i++) {
						City city2 = Handler_Json.JsonToBean(City.class, array
								.get(i).toString());
						cityCity.add(city2);
					}
					citys.put("city", cityCity);
				}
			}
		}
		return citys;
	}

	private List<City> filledData(ArrayList<City> citys) {
		// List<City> newList = new ArrayList<City>();
		// 实例化汉字转拼音类
		AbCharacterParser characterParser = AbCharacterParser.getInstance();

		for (City city : citys) {
			String pinyin = characterParser.getSelling(city.getName());
			String sortString = pinyin.substring(0, 1).toUpperCase();
			if (sortString.matches("[A-Z]")) {
				city.setFirstLetter(sortString.toUpperCase());
			} else {
				city.setFirstLetter("#");
			}
		}
		Collections.sort(citys);
		// for (int i = 0; i < array.length; i++) {
		// City city = new City();
		// city.setName(array[i]);
		// 汉字转换成拼音
		// String pinyin = characterParser.getSelling(array[i]);
		// String sortString = pinyin.substring(0, 1).toUpperCase();
		// 正则表达式，判断首字母是否是英文字母
		// if (sortString.matches("[A-Z]")) {
		// city.setFirstLetter(sortString.toUpperCase());
		// } else {
		// city.setFirstLetter("#");
		// }
		// newList.add(city);
		// }
		// Collections.sort(newList);
		// return newList;
		return citys;
	}

	
	@Override
	public void onPause() {
		/*v.et_home_search.setInputType(InputType.TYPE_DATETIME_VARIATION_NORMAL);*/
		super.onPause();
	}
	@Override
	public void onDestroy() {
		stopLocation();// 停止定位
		aMapLocation = null;
		LogUtils.toDebugLog("GpsFragment", "GpsFragment中的定位终止");
		activity.sendBroadcast(new Intent("com.lansun.qmyo.restartGPS"));
		super.onDestroy();
	}
	/**
	 * 利用定位代理来定位城市信息
	 */
	protected void locationData() {
		aMapLocManager = LocationManagerProxy.getInstance(activity);
		aMapLocManager.requestLocationData(LocationProviderProxy.AMapNetwork, 1000, 10, locationListener);
		/* Notice!!! 这里定位使用的是LocationManagerProxy.NETWORK_PROVIDER !!!,没有使用高德地图的 混合定位
		 * aMapLocManager.requestLocationData(LocationManagerProxy.NETWORK_PROVIDER, 2000, 10, locationListener);*///此处使用的是网络（硬件）定位，故会产生偏差
		aMapLocManager.setGpsEnable(true);
	}
	private AMapLocationListener locationListener = new AMapLocationListener(){
		/**
		 * 混合定位回调函数(未走)
		 * 暂时走Network的渠道
		 */
		@Override
		public void onLocationChanged(final AMapLocation location) {
			if (location != null) {
				aMapLocation = location;// 判断超时机制
				Double geoLat = location.getLatitude();
				Double geoLng = location.getLongitude();
				String cityCode = "";
				String desc = "";
				
				final Bundle locBundle = location.getExtras();
			
				if (locBundle != null) {
					cityCode = locBundle.getString("citycode");
					desc = locBundle.getString("desc");
				}
				
				/*当location并没有拿到真实的定位坐标时,
				 * 即将location赋为0.0&0.0时,aMapLocation被赋值后为0.0&0.0,
				 * 故在此处停止位置定位监听是不正确的
				 * if (aMapLocation != null) {
					stopLocation();
				}*/
				
				
				/*String cityName = App.app.getData("cityName");
				tv_city_header_current_city.setText(Html.fromHtml(String
						.format(getString(R.string.current_city),  cityName)));
				city_gps_loading.setVisibility(View.GONE);*/
				
				
				
				/*将定位到的数据存到了GlobalValue.gps上 */
				if(location.getLatitude()==0||location.getLongitude()==0){
					GlobalValue.gps = new Gps(31.230431, 121.473705);
				}else{
					GlobalValue.gps = new Gps(location.getLatitude(),location.getLongitude());
				}
				Log.i("location的地址信息: ",GlobalValue.gps.getWgLat() +" & "+ GlobalValue.gps.getWgLon());
				
				
		    	//使用FastHttpHander去访问服务器
				InternetConfig config1 = new InternetConfig();
				config1.setKey(2);
				FastHttpHander.ajaxGet(String.format(GlobalValue.URL_GPS_ADCODE, GlobalValue.gps.getWgLon(),
						GlobalValue.gps.getWgLat()), null, config1, GpsFragment.this);
				
				if (aMapLocation != null) {
					stopLocation();
				}
				
				
				/* 使用HttpUtils去访问服务器
				HttpUtils httpUtils = new HttpUtils();
				RequestCallBack<String> requestCallback = new RequestCallBack<String>() {
					@Override
					public void onFailure(HttpException arg0, String arg1) {
					}
					@Override
					public void onSuccess(ResponseInfo<String> arg0) {
						Log.i("显示成功: ", "确定能去访问数据!但就是无法对下面的控件进行操作");
						AdCode code = Handler_Json.JsonToBean(AdCode.class,arg0.result);
						localCity = code.getCity();
						tv_city_header_current_city.setText(Html.fromHtml(String.format(getString(R.string.current_city),
								localCity)));
						city_gps_loading.setVisibility(View.GONE);
					}
				};
				String url = "http://mo.amap.com/service/geo/getadcode.json?longitude="
							+location.getLongitude()+"latitude="+location.getLatitude();
				httpUtils.send(HttpMethod.GET, url,requestCallback); */
			}
		}

		@Override
		public void onProviderEnabled(String arg0) {

		}

		@Override
		public void onProviderDisabled(String arg0) {

		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

		}

		@Override
		public void onLocationChanged(Location location) {
			
		}
	};
	
	private HashMap<String, ArrayList<City>> citys =new HashMap<String, ArrayList<City>>() ;
	
	
	
	
	
	private void click(View view) {
		InternetConfig config = new InternetConfig();
		HashMap<String, Object> head = new HashMap<>();
		head.put("Authorization", "Bearer " + App.app.getData("access_token"));
		config.setHead(head);
		switch (view.getId()) {
		case R.id.tv_city_all:// 境内城市 全部
			currentState = "all";
			changeTextColor(v.tv_city_all);
			v.ll_gps_overseas_no.setVisibility(View.GONE);
			config.setKey(0);
			FastHttpHander.ajaxGet(GlobalValue.URL_SEARCH_SITE + currentState,
					config, this);
			break;
		case R.id.tv_city_overseas:// 海外
			currentState = "foreign";
			changeTextColor(v.tv_city_overseas);
			
			/*暂时不去拿值，只是展示图片
			 * config.setKey(1);
			FastHttpHander.ajaxGet(GlobalValue.URL_SEARCH_SITE + currentState,config, this);*/
			v.ll_gps_overseas_no.setVisibility(View.VISIBLE);
			v.listView.setAdapter(null);
			
			
			break;
		case R.id.tv_gps_cancle:// 搜索取消
			if (v.tv_gps_cancle.getText().equals(getString(R.string.search))) {

			} else if (v.tv_gps_cancle.getText().equals(getString(R.string.cancle))) {
				back();
			}
			break;
			
			
		/*case R.id.tv_city_header_current_city: //点击定位城市显示栏
			//将城市数据放入到
			HomeFragment fragment = new HomeFragment();
			Bundle args = new Bundle();
			saveSelectCity(localCityCode, localCity);
			
			saveCurrentCity(localCityCode, localCity);
			fragment.setArguments(args);
			FragmentEntity event = new FragmentEntity();
			event.setFragment(fragment);
			EventBus.getDefault().post(event);
			break;*/
			
		}
	}
	/**
	 * 销毁定位
	 */
	protected void stopLocation() {
		if (aMapLocManager != null) {
			aMapLocManager.removeUpdates(locationListener);
			aMapLocManager.destroy();
		}
		aMapLocManager = null;
		
	}

	private void changeTextColor(TextView tv) {
		v.tv_city_overseas.setTextColor(getResources().getColor(R.color.app_grey5));
		v.tv_city_all.setTextColor(getResources().getColor(R.color.app_grey5));
		
		tv.setTextColor(getResources().getColor(R.color.app_green2));
	};

	/**
	 * 海外站点点击事件
	 * 
	 * @param arg0
	 * @param arg1
	 * @param position
	 * @param arg3
	 */
	private OnItemClickListener overseasItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			City city = citys.get("foreign").get(position);
			Fragment fragment;
			Bundle args = new Bundle();
			if (city.getCity() > 0) {
				fragment = new SelectCityFragment();
				args.putString("code", city.getCode());
				currentState = "all";
			} else {
				
				/*fragment = new HomeFragment();*/
				fragment = new MainFragment(0);
				saveSelectCity(city.getCode(), city.getName());
				saveCurrentCity(city.getCode(), city.getName());
			}
			fragment.setArguments(args);
			FragmentEntity event = new FragmentEntity();
			event.setFragment(fragment);
			EventBus.getDefault().post(event);
		}
	};

	/**
	 * 列表点击事件
	 */
	private void itemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {
		City city;
		if ("foreign".equals(currentState)) {
			city = citys.get("country").get(position - 1);
		} else {
			city = citys.get("city").get(position - 1);
		}
		Fragment fragment;
		Bundle args = new Bundle();
		if (city.getCity() > 0) {// 有2个城市就要跳转
			fragment = new SelectCityFragment();
			args.putString("code", city.getCode());
			currentState = "all";
		} else {
			/*fragment = new HomeFragment();*/
			fragment = new MainFragment(0);
			args.putBoolean("restartGps", true);
			saveSelectCity(city.getCode(), city.getName());
			
			/** 不去变更当前城市的定位值
			 *  saveCurrentCity(city.getCode(), city.getName());
			 */
		}
		fragment.setArguments(args);
		FragmentEntity event = new FragmentEntity();
		event.setFragment(fragment);
		EventBus.getDefault().post(event);
	}

	/**
	 * 热门站点点击事件
	 * 
	 * @param arg0
	 * @param arg1
	 * @param position
	 * @param arg3
	 */
	private OnItemClickListener hotItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			City city = citys.get("recommend").get(position);
			Fragment fragment;
			Bundle args = new Bundle();
			if(city.getCity() > 0){// 有2个城市就要跳转
				fragment = new SelectCityFragment();
				args.putString("code", city.getCode());
				currentState = "all";
			}else{
				/*fragment = new HomeFragment();*/
				fragment = new MainFragment(0);
				args.putBoolean("restartGps", true);
				saveSelectCity(city.getCode(), city.getName());
				/** 不去变更当前城市的定位值
				 *  saveCurrentCity(city.getCode(), city.getName());
				 */
			}
			fragment.setArguments(args);
			FragmentEntity event = new FragmentEntity();
			event.setFragment(fragment);
			EventBus.getDefault().post(event);
		}
	};

	/**
	 * 近期访问点击事件
	 * 
	 * @param arg0
	 * @param arg1
	 * @param position
	 * @param arg3
	 */
	private OnItemClickListener recordItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			City city = citys.get("record").get(position);
			Fragment fragment;
			Bundle args = new Bundle();
			if (city.getCity() > 0) {// 有2个城市就要跳转
				fragment = new SelectCityFragment();
				args.putString("code", city.getCode());
				currentState = "all";
			} else {
				/*fragment = new HomeFragment();*/
				fragment = new MainFragment(0);
				args.putBoolean("restartGps", true);
				saveSelectCity(city.getCode(), city.getName());
				/** 不去变更当前城市的定位值
				 *  saveCurrentCity(city.getCode(), city.getName());
				 */
			}
			fragment.setArguments(args);
			FragmentEntity event = new FragmentEntity();
			event.setFragment(fragment);
			EventBus.getDefault().post(event);
		}
	};
	private String localCity;
	private String localCityCode;
	/*private TextView tv_city_header_current_city2;*/

	
	

}
