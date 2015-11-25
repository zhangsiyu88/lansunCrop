package com.lansun.qmyo.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.Inflater;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;


import cn.jpush.android.util.ac;

import com.android.pc.ioc.app.Ioc;
import com.android.pc.ioc.db.sqlite.Selector;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;

import com.android.pc.ioc.view.listener.OnClick;

/*import com.android.pc.ioc.adapter.WheelViewAdapter;*/
/*import com.android.pc.ioc.adapter.ArrayWheelAdapter;*/
/*import com.android.pc.ioc.adapter.AbstractWheelTextAdapter;*/
/*import com.android.pc.ioc.view.WheelView;*/
/*import com.android.pc.ioc.view.wheelview.OnWheelChangedListener;*/
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.Address;
import com.lansun.qmyo.domain.Address2;
import com.lansun.qmyo.domain.Address3;
import com.lansun.qmyo.domain.User;
import com.lansun.qmyo.domain.area.CityBean;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.DBManager;
import com.lansun.qmyo.utils.DensityUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.R;

/**
 * 编辑用户爱好
 * 
 * @author bhxx
 * 
 */
public class EditUserAddressFragment1 extends BaseFragment 
		implements OnClickListener,OnWheelChangedListener{
	@InjectAll
	Views v;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View  fl_comments_right_iv,
				tv_activity_shared, tv_edit_user_like_commit, rl_select_city;
		
		private EditText et_edit_user_address;
	    /*private TextView tv_edit_city;*/
		private TextView tv_activity_title, textView1;
	}

	private int cityEnd = 0;
	private int areaEnd = 0;

	private String area;

	private HashMap<String, ArrayList<String>> cityMaps = new HashMap<String, ArrayList<String>>();
	private HashMap<String, ArrayList<String>> areaMaps = new HashMap<String, ArrayList<String>>();
	private String[] provinces;
	
	
	
	/**
	 * ----------------------------------获取城市信息
	 */
	
	private DBManager dbManager;
	private DBManager manager;
	/**
	 * key - 省 value - 市
	 */
	protected Map<String, String[]> mCitisDatasMap = new HashMap<String, String[]>();
	
	protected Map<String, String[]> mCitisIdsMap = new HashMap<String, String[]>();
	/**
	 * key - 市 values - 区
	 */
	protected Map<String, String[]> mDistrictDatasMap = new HashMap<String, String[]>();
	protected Map<String, String[]> mDistrictIdsMap = new HashMap<String, String[]>();
	protected List<CityBean> queryByProvince = null;
	protected List<CityBean> queryByCity = null;
	protected List<CityBean> queryByCounty = null;
	/**
	 * 当前省的名称
	 */
	protected String mCurrentProviceName;
	/**
	 * 当前市的名称
	 */
	protected String mCurrentCityName;
	/**
	 * 当前区的名称
	 */
	protected String mCurrentDistrictName = "";
	/**
	 * 所有省
	 */
	protected String[] mProvinceDatas;
	private ExecutorService pool;
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(getArguments()!=null){
			mUserAddressCity = getArguments().getString("userAddressCity");
			mUserAddressArea = getArguments().getString("userAddressArea");
		}
		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		height = metrics.heightPixels;
		width = metrics.widthPixels;
		initDB();
		pool = Executors.newFixedThreadPool(6);
		MyCityTask mCityTask = new MyCityTask();
		pool.execute(mCityTask);
	}
	

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		rootView = inflater.inflate(R.layout.activity_edit_user_address,null);
		
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		v.fl_comments_right_iv.setVisibility(View.GONE);
		v.tv_activity_shared.setVisibility(View.GONE);
		initTitle(v.tv_activity_title, R.string.edit_address, null, 0);
		tv_edit_city = (TextView) rootView.findViewById(R.id.tv_edit_city);
		
		if((mUserAddressCity!=null&&mUserAddressCity!="" )||(mUserAddressArea!=null&&mUserAddressArea!="")){
			tv_edit_city.setText(mUserAddressCity);
			v.et_edit_user_address.setText(mUserAddressArea);
		}
	}
	

	
	private void click(View view) {
		switch (view.getId()) {
		case R.id.rl_select_city:
			/*selectCity();*/
			Log.d("tag", "请弹出弹框~~");
			cityPopupWindow();
			break;
		case R.id.tv_edit_user_like_commit:
			if (!TextUtils.isEmpty(tv_edit_city.getText().toString())) {
				InternetConfig config = new InternetConfig();
				config.setKey(0);
				HashMap<String, Object> head = new HashMap<>();
				head.put("Authorization",
						"Bearer " + App.app.getData("access_token"));
				config.setHead(head);
				LinkedHashMap<String, String> params = new LinkedHashMap<>();
				params.put("address", tv_edit_city.getText().toString()+" "
						+ v.et_edit_user_address.getText().toString());
				
				/*FastHttpHander.ajaxForm(GlobalValue.URL_USER_SAVE, params,
						null, config, EditUserAddressFragment.this);*/
				
				FastHttpHander.ajax(GlobalValue.URL_USER_SAVE, params,config,
						       EditUserAddressFragment1.this);
				
			} else {
				CustomToast.show(activity, getString(R.string.tip), "内容不能为空");
			}
			break;
		}
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 0:
				GlobalValue.user = Handler_Json.JsonToBean(User.class,
						r.getContentAsString());
				CustomToast.show(activity, getString(R.string.tip), "修改成功");
				
				/*back();*/
				//此处直接跳装至完善个人信息页
				EditUserFragment editUserFragment = new EditUserFragment();
				FragmentEntity fEntity = new FragmentEntity();
				fEntity.setFragment(editUserFragment);
				EventBus.getDefault().post(fEntity);
				
				break;
			}
		} else {
			CustomToast.show(activity, "网络故障", "网络错误");
		}

	}

	private WheelView wv_wheelcity_country;
	private WheelView wv_wheelcity_city;
	private WheelView wv_wheelcity_ccity;
	private TextView tv_city_accomplish;
	private String cityTxt = "";
	private Dialog dialog;

	
	private void initDB() {
		// TODO Auto-generated method stub
		manager = DBManager.getNewInstance(activity);

		// 查询省信息
		queryByProvince = manager.queryByProvince("1");

	}

	private class MyCityTask implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			initProvinceDatas();
		}

		
	}
	protected void initProvinceDatas() {

		if (queryByProvince != null && !queryByProvince.isEmpty()) {

			mCurrentProviceName = queryByProvince.get(0).getName();
		}

		if (queryByCity != null && !queryByCity.isEmpty()) {

			mCurrentCityName = queryByCity.get(0).getName();
		}

		if (queryByCounty != null && !queryByCounty.isEmpty()) {

			mCurrentDistrictName = queryByCounty.get(0).getName();
		}

		mProvinceDatas = new String[queryByProvince.size()];

		for (int i = 0; i < queryByProvince.size(); i++) {

			mProvinceDatas[i] = queryByProvince.get(i).getName();

			String provinceId = queryByProvince.get(i).getId();

			/**
			 * 查询省对应的市数据
			 */
			queryByCity = manager.queryByProAndCity(provinceId);

			String[] cityNames = new String[queryByCity.size()];
			
			String[] cityIds = new String[queryByCity.size()];

			for (int j = 0; j < queryByCity.size(); j++) {

				cityNames[j] = queryByCity.get(j).getName();
				
				cityIds[j] = queryByCity.get(j).getId();
				
				String id = queryByCity.get(j).getId();

				queryByCounty = manager.queryByCityAndCounty(id);

				String[] countyNameArray = new String[queryByCounty.size()];
				
				String[] countyIdArray = new String[queryByCounty.size()];

				for (int k = 0; k < countyNameArray.length; k++) {

					countyNameArray[k] = queryByCounty.get(k).getName();
					
					countyIdArray[k] = queryByCounty.get(k).getId();
				}
				mDistrictDatasMap.put(cityNames[j], countyNameArray);
				mDistrictIdsMap.put(id, countyIdArray);
			}
			mCitisDatasMap.put(queryByProvince.get(i).getName(), cityNames);
			mCitisIdsMap.put(provinceId, cityIds);
		}
	}
	
	/**
	 * 城市弹出框
	 * 
	 */
	private LayoutInflater mInflater;
	private PopupWindow popupWindow;
	private WheelView mViewProvince;
	private WheelView mViewCity;
	private WheelView mViewDistrict;
	private TextView tv_choice_city;
	private int height;
	private int width;
	private View data;
	
	private void cityPopupWindow() {
		// TODO Auto-generated method stub
		mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View contentView = mInflater.inflate(R.layout.fragment_receiver_screen_city, null);

		mViewProvince = (WheelView) contentView.findViewById(R.id.id_province);
		mViewCity = (WheelView) contentView.findViewById(R.id.id_city);
		mViewDistrict = (WheelView) contentView.findViewById(R.id.id_district);
		tv_choice_city = (TextView) contentView.findViewById(R.id.tv_choice_city);
		tv_choice_city.setOnClickListener(this);
		
		
		mViewProvince.setCyclic(false);
		mViewCity.setCyclic(false);
		mViewDistrict.setCyclic(false);

		mViewProvince.addChangingListener(this);
		mViewCity.addChangingListener(this);
		mViewDistrict.addChangingListener(this);
		
		ArrayWheelAdapter<String> provinceArrayWheelAdapter = new ArrayWheelAdapter<String>(activity,mProvinceDatas);
		provinceArrayWheelAdapter.setTextSize(18);
		
		mViewProvince.setViewAdapter(provinceArrayWheelAdapter);
		
		updateCities();
		updateAreas();

		
		Log.d("tag", "弹框弹不出来是因为这个鬼吗?~~");
		popupWindow = new PopupWindow(rootView.findViewById(R.id.fl_data), this.width,
				380, true);
		
		/*popupWindow.setAnimationStyle(R.style.AnimBottom);*/
		popupWindow.setAnimationStyle(R.style.PopupWindowAnimation);
		popupWindow.setContentView(contentView);
		popupWindow.setFocusable(true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());

		data = rootView.findViewById(R.id.fl_data);
		popupWindow.showAtLocation(data, Gravity.BOTTOM, 0, 0);
		
		backgroundAlpha(0.5f);
		popupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				backgroundAlpha(1f);
			}
		});
	}

	private void backgroundAlpha(float f) {
		WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
		lp.alpha = f;
		activity.getWindow().setAttributes(lp);
	}



	private String city_id_1;
	private String city_id_2;
	private String city_id_3;
	private TextView tv_region_select;
    private View rootView;
	private TextView tv_edit_city;
	private String mUserAddressCity;
	private String mUserAddressArea;

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.tv_choice_city://------------------------------------------>选中了弹出城市选择框中的某一个栏目,并且点击确定进行选中
		popupWindow.dismiss();

		city_id_1 = queryByProvince.get(mViewProvince.getCurrentItem()).getId();
		city_id_2 = mCitisIdsMap.get(city_id_1)[mViewCity.getCurrentItem()];
		if (mDistrictIdsMap.get(city_id_2).length == 0) {
			city_id_3 = "";
			/*tv_region_select.setText(mCurrentProviceName + "/"
					+ mCurrentCityName);*/
			
			tv_edit_city.setText(mCurrentProviceName + "/"+ mCurrentCityName);
		} else {
			city_id_3 = mDistrictIdsMap.get(city_id_2)[mViewDistrict.getCurrentItem()];
			
		/*	tv_region_select.setText(mCurrentProviceName
					+ "/"
					+ mCurrentCityName
					+ "/"
					+ mDistrictDatasMap.get(mCurrentCityName)[mViewDistrict.getCurrentItem()]);*///拼接三个部分的数据
			tv_edit_city.setText(mCurrentProviceName
					+ "/"
					+ mCurrentCityName
					+ "/"
					+ mDistrictDatasMap.get(mCurrentCityName)[mViewDistrict.getCurrentItem()]);
			
		}

		break;
		}
	}


	@Override
	public void onChanged(WheelView wheel, int oldValue, int newValue) {
		if (wheel == mViewProvince) {
			updateCities();
		} else if (wheel == mViewCity) {
			updateAreas();
		}/* else if (wheel == mIndustryType) {
			updateIndustry();
		}*/
	}
	
	private void updateCities() {

		int currentItem = mViewProvince.getCurrentItem();
		mCurrentProviceName = mProvinceDatas[currentItem];
		String[] cities = mCitisDatasMap.get(mCurrentProviceName);
		if (cities == null) {
			cities = new String[] { "" };
		}
		ArrayWheelAdapter<String> cityArrayWheelAdapter = new ArrayWheelAdapter<String>(activity, cities);
		cityArrayWheelAdapter.setTextSize(18);
		mViewCity.setViewAdapter(cityArrayWheelAdapter);
		mViewCity.setCurrentItem(0);
		
		updateAreas();
	}

	private void updateAreas() {
		int currentItem = mViewCity.getCurrentItem();
		mCurrentCityName = mCitisDatasMap.get(mCurrentProviceName)[currentItem];
		String[] areas = mDistrictDatasMap.get(mCurrentCityName);
		if (areas == null) {
			areas = new String[] { "" };
		}
		ArrayWheelAdapter<String> areasArrayWheelAdapter = new ArrayWheelAdapter<String>(activity, areas);
		areasArrayWheelAdapter.setTextSize(18);
		mViewDistrict.setViewAdapter(areasArrayWheelAdapter);
		mViewDistrict.setCurrentItem(0);
	}

}
