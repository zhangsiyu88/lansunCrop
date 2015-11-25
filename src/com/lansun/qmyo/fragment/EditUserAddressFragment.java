package com.lansun.qmyo.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.pc.ioc.adapter.AbstractWheelTextAdapter;
import com.android.pc.ioc.adapter.ArrayWheelAdapter;
import com.android.pc.ioc.app.Ioc;
import com.android.pc.ioc.db.sqlite.Selector;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.WheelView;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.ioc.view.wheelview.OnWheelChangedListener;
/*import com.android.pc.ioc.adapter.WheelViewAdapter;*/
/*import com.android.pc.ioc.adapter.ArrayWheelAdapter;*/
/*import com.android.pc.ioc.adapter.AbstractWheelTextAdapter;*/
/*import com.android.pc.ioc.view.WheelView;*/
/*import com.android.pc.ioc.view.wheelview.OnWheelChangedListener;*/
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.R;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.Address;
import com.lansun.qmyo.domain.Address2;
import com.lansun.qmyo.domain.Address3;
import com.lansun.qmyo.domain.User;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;

/**
 * 编辑用户爱好
 * 
 * @author bhxx
 * 
 */
public class EditUserAddressFragment extends BaseFragment {
	@InjectAll
	Views v;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View  fl_comments_right_iv,
				tv_activity_shared, tv_edit_user_like_commit, rl_select_city;
		private EditText et_edit_user_address;
		private TextView tv_edit_city;
		private TextView tv_activity_title, textView1;
	}

	private int cityEnd = 0;
	private int areaEnd = 0;

	private String area;

	private HashMap<String, ArrayList<String>> cityMaps = new HashMap<String, ArrayList<String>>();
	private HashMap<String, ArrayList<String>> areaMaps = new HashMap<String, ArrayList<String>>();
	private String[] provinces;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_edit_user_address,null);
		
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		v.fl_comments_right_iv.setVisibility(View.GONE);
		v.tv_activity_shared.setVisibility(View.GONE);
		initTitle(v.tv_activity_title, R.string.edit_address, null, 0);
	   
		pd.show();
		
		loadCity();
		
		// 加载城市信息
		/*new Thread(new Runnable() {
			public void run() {
				try{
					//初始化省市区
					loadCity();
				}catch(Exception e){
					CustomToast.show(activity, "初始化省市区出现异常", "异常已被抓住");
					e.printStackTrace();
				}
			}
		}).start();*/
	    
	}
	

	/**
	 * 加载省市区的信息
	 * 由init()去访问
	 */
	private void loadCity() {
		Selector provinceSelector = Selector.from(Address.class);
		provinceSelector.select(" * ");
		provinceSelector.limit(Integer.MAX_VALUE);
		Selector citySelector = Selector.from(Address2.class);
		provinceSelector.select(" * ");
		provinceSelector.limit(Integer.MAX_VALUE);
		Selector areaSelector = Selector.from(Address3.class);
		provinceSelector.select(" * ");
		provinceSelector.limit(Integer.MAX_VALUE);
		
		/*根据在后台拿到的数据库文件,并将其对应列下的数据放入到对应List中*/
		List<Address> province = Ioc.getIoc()
				.getDb(activity.getCacheDir().getPath(), "province")
				.findAll(provinceSelector);
		
		List<Address2> city = Ioc.getIoc()
				.getDb(activity.getCacheDir().getPath(), "city")
				.findAll(citySelector);
		List<Address3> area = Ioc.getIoc()
				.getDb(activity.getCacheDir().getPath(), "area")
				.findAll(areaSelector);
		
		initCityData(province, city, area);
		
	/*	try{
			//初始化省市区
			initCityData(province, city, area);
		}catch(Exception e){
			CustomToast.show(activity, "初始化省市区出现异常", "异常已被抓住");
			e.printStackTrace();
		}*/
	}

	/**
	 * 初始化省
	 * @param province
	 */
	private void initCityData(List<Address> province, List<Address2> city,List<Address3> area) {
		Log.i("", "走到了InitCityData()这个方法");
		provinces = new String[province.size()];
		for (int i = 0; i < province.size(); i++) {
			if (cityEnd != city.size()) {
				initCity(province.get(i).getName(), i, province.get(i).getCityCount(), city, area);
			}
			provinces[i] = province.get(i).getName();
		}
		
		
	}

	/**
	 * 初始化市
	 * @param size
	 *            省的大小
	 * @param city
	 */
	private void initCity(String pName, int index, int size,
			List<Address2> city, List<Address3> area) {
		
		Log.i("", "走到了InitCity()这个方法");
		if (city != null) {
			ArrayList<String> citys = new ArrayList<String>();
			for (int i = 0; i < size; i++) {
				Address2 address2 = city.get(cityEnd);
				citys.add(address2.getName());
				initArea(address2.getName(), index, i, address2.getAreaCount(),
						area);
				cityEnd++;
			}
			cityMaps.put(pName, citys);
		}
	}

	/**
	 * 初始化区域
	 * @param i
	 * @param areaCount
	 * @param area
	 */
	private void initArea(String cityName, int pIndex, int cIndex, int size,
			List<Address3> area) {
		Log.i("", "走到了InitCityArea()这个方法");
		if (area != null) {
			ArrayList<String> areas = new ArrayList<String>();
			for (int i = 0; i < size; i++) {
				Address3 address3 = area.get(areaEnd);
				areas.add(address3.getName());
				areaEnd++;
			}
			areaMaps.put(cityName, areas);
		}
		
		this.pd.dismiss();
	}

	
	
	private void click(View view) {
		switch (view.getId()) {
		case R.id.rl_select_city:
			selectCity();
			break;
		case R.id.tv_edit_user_like_commit:
			if (!TextUtils.isEmpty(v.tv_edit_city.getText().toString())) {
				InternetConfig config = new InternetConfig();
				config.setKey(0);
				HashMap<String, Object> head = new HashMap<>();
				head.put("Authorization",
						"Bearer " + App.app.getData("access_token"));
				config.setHead(head);
				LinkedHashMap<String, String> params = new LinkedHashMap<>();
				params.put("address", v.tv_edit_city.getText().toString()
						+ v.et_edit_user_address.getText().toString());
				
				/*FastHttpHander.ajaxForm(GlobalValue.URL_USER_SAVE, params,
						null, config, EditUserAddressFragment.this);*/
				
				FastHttpHander.ajax(GlobalValue.URL_USER_SAVE, params,config,
						       EditUserAddressFragment.this);
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
				back();
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

	
	//弹出城市选择框
	public void selectCity() {
		CustomToast.show(activity, "证明点击进入城市选择的模块", "城市选择进行");
		/*inflater = LayoutInflater.from(activity);*/
		
		View view = inflater.inflate(R.layout.city_choose_dialog, null);
		
		
		Log.i("TAGTAGTAGTAGTAG","findViewbyId之前");
		
		//下面三个WheelView的对象已经findView到了
		wv_wheelcity_country = (WheelView) view.findViewById(R.id.wv_wheelcity_country);
		wv_wheelcity_city = (WheelView) view.findViewById(R.id.wv_wheelcity_city);
		wv_wheelcity_ccity = (WheelView) view.findViewById(R.id.wv_wheelcity_ccity);
		Log.i("TAGTAGTAGTAGTAG","findViewbyId之后");
		
		
		tv_city_accomplish = (TextView) view.findViewById(R.id.tv_city_accomplish);//提交二字
		
		tv_city_accomplish.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				v.tv_edit_city.setText(cityTxt);
				v.textView1.setVisibility(View.GONE);
				dialog.dismiss();
			}
		});
		
		Log.i("TAGTAGTAGTAGTAG","wheelCity内容展现之前");
		
		
		wv_wheelcity_country.setVisibleItems(3);
		wv_wheelcity_country.setViewAdapter(new CountryAdapter(activity));
		
		CountryAdapter countryAdapter = new CountryAdapter(activity);
		int itemsCount = countryAdapter.getItemsCount();
		Log.i("", "CountryAdapter 中的 数据为"+ itemsCount);
		Log.i("", countryAdapter == null?"countryAdapter为空":"countryAdapter不为空");
		
		
		wv_wheelcity_city.setVisibleItems(3);
		wv_wheelcity_ccity.setVisibleItems(3);
		

		Log.i("TAGTAGTAGTAGTAG","wv_wheelcity_country.setViewAdapter(new CountryAdapter(activity))内容展现之后见不到");

		/*省的选择*/
		Log.i("TAGTAGTAGTAGTAG","wv_wheelcity_country添加监听器");
		wv_wheelcity_country.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				String pName = provinces[wv_wheelcity_country.getCurrentItem()];
				String cName = "";
				if (cityMaps.get(pName) != null
						&& cityMaps.get(pName).size() > 0) {
					cName = cityMaps.get(pName).get(0);
				}
				String aName = "";
				if (areaMaps.get(cName) != null
						&& areaMaps.get(cName).size() > 0) {
					if (wv_wheelcity_ccity.getCurrentItem() < areaMaps.get(
							cName).size()) {
						aName = areaMaps.get(cName).get(
								wv_wheelcity_ccity.getCurrentItem());
					}
				}
				updateCities(wv_wheelcity_city, pName);
				updatecCities(wv_wheelcity_ccity, cName);
				cityTxt = pName + " | " + cName + " | " + aName;
			}

		});

		/*市的选择*/
		Log.i("TAGTAGTAGTAGTAG","wv_wheelcity_city添加监听器");
		wv_wheelcity_city.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				String pName = provinces[wv_wheelcity_country.getCurrentItem()];
				String cName = "";
				if (cityMaps.get(pName) != null
						&& cityMaps.get(pName).size() > 0) {
					cName = cityMaps.get(pName).get(
							wv_wheelcity_city.getCurrentItem());
				}
				String aName = "";
				if (areaMaps.get(cName) != null
						&& areaMaps.get(cName).size() > 0) {
					if (wv_wheelcity_ccity.getCurrentItem() < areaMaps.get(
							cName).size()) {
						aName = areaMaps.get(cName).get(
								wv_wheelcity_ccity.getCurrentItem());
					}
				}
				updatecCities(wv_wheelcity_ccity, cName);
				cityTxt = pName + " | " + cName + " | " + aName;
			}
		});

		/*区的选择*/
		Log.i("TAGTAGTAGTAGTAG","wv_wheelcity_ccity添加监听器");
		wv_wheelcity_ccity.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {

				String pName = provinces[wv_wheelcity_country.getCurrentItem()];
				String cName = cityMaps.get(pName).get(
						wv_wheelcity_city.getCurrentItem());
				String aName = "";
				if (areaMaps.get(cName) != null
						&& areaMaps.get(cName).size() > 0) {
					if (wv_wheelcity_city.getCurrentItem() < areaMaps
							.get(cName).size()) {
						aName = areaMaps.get(cName).get(
								wv_wheelcity_city.getCurrentItem());
					}
				}
				cityTxt = pName + " | " + cName + " | " + aName;
			}
		});

		wv_wheelcity_country.setCurrentItem(0);
		wv_wheelcity_city.setCurrentItem(0);
		wv_wheelcity_ccity.setCurrentItem(0);
		
		

		Log.i("TAGTAGTAGTAGTAG","下面生成Dialog");
		dialog = new Dialog(activity, R.style.transparentFrameWindowStyle);
		dialog.setContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		Window window = dialog.getWindow();
		// 设置显示动画
		window.setWindowAnimations(R.style.PopupWindowAnimation);
		WindowManager.LayoutParams wl = window.getAttributes();
		wl.x = 0;
		wl.y = activity.getWindowManager().getDefaultDisplay().getHeight();
		// 以下这两句是为了保证按钮可以水平满屏
		wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
		wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		// 设置显示位置
		dialog.onWindowAttributesChanged(wl);
		// 设置点击外围解散
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
		Log.i("TAGTAGTAGTAGTAG","生成Dialog的Show()不出来！");
		
	}

	/**
	 * Updates the city wheel
	 */
	public void updateCities(WheelView city, String pName) {
		if (cityMaps.get(pName) != null && cityMaps.get(pName).size() > 0) {
			String[] array = new String[cityMaps.get(pName).size()];
			cityMaps.get(pName).toArray(array);
			
			ArrayWheelAdapter<String> adapter = new ArrayWheelAdapter<String>(activity, array);
			
//			adapter.setTextSize(18);
//			city.setViewAdapter(adapter);
			
			city.setCurrentItem(0);
			cityMaps.get(pName);
		} else {
			city.setViewAdapter(null);
		}
	}

	/**
	 * Updates the ccity wheel
	 */
	public void updatecCities(WheelView city, String cName) {
		if (areaMaps.get(cName) != null && areaMaps.get(cName).size() > 0) {
			String[] array = new String[areaMaps.get(cName).size()];
			areaMaps.get(cName).toArray(array);
			ArrayWheelAdapter<String> adapter = new ArrayWheelAdapter<String>(activity, array);
			
//		    adapter.setTextSize(18);
//			city.setViewAdapter(adapter);
//			
			city.setCurrentItem(0);
		} else {
			city.setViewAdapter(null);
		}
	}

	public class CountryAdapter extends AbstractWheelTextAdapter {
		// Countries names
		public String countries[] = provinces;

		/**
		 * Constructor
		 */
		protected CountryAdapter(Context context) {
			super(context, R.layout.wheelcity_country_layout, NO_RESOURCE);
			setItemTextResource(R.id.wheelcity_country_name);
		}

		@Override
		public View getItem(int index, View cachedView, ViewGroup parent) {
			View view = super.getItem(index, cachedView, parent);
			return view;
		}

		@Override
		protected CharSequence getItemText(int index) {
			return countries[index];
		}

		/**
		 * 此处重写了WheelViewAdapter接口中的方法getItemsCount()
		 */
		
		public int getItemsCount() {
			return countries.length;
		}
	}
}
