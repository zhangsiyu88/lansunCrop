package com.lansun.qmyo.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

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
import com.android.pc.ioc.view.listener.OnItemClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.adapter.SearchHotAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.AreaList;
import com.lansun.qmyo.domain.City;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.R;

public class SelectCityFragment extends BaseFragment {

	@InjectAll
	Views v;
	private ArrayList<City> citys;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View  fl_comments_right_iv,
				tv_activity_shared;
		@InjectBinder(listeners = { OnItemClick.class }, method = "itemClick")
		private GridView gv_select_city;
		private TextView tv_activity_title;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_select_city, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		v.fl_comments_right_iv.setVisibility(View.GONE);
		v.tv_activity_shared.setVisibility(View.GONE);
		
		initTitle(v.tv_activity_title, R.string.select_city, null, 0);

		String code = getArguments().getString("code");

		InternetConfig config = new InternetConfig();
		config.setKey(0);
		HashMap<String, Object> head = new HashMap<>();
		head.put("Authorization", "Bearer " + App.app.getData("access_token"));
		config.setHead(head);
		
		FastHttpHander.ajaxGet(GlobalValue.URL_SEARCH_CITY + code, config, this);

	}


	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 0:
				citys = new ArrayList<>();
				ArrayList<HashMap<String, String>> dataList = new ArrayList<>();
				AreaList areas = Handler_Json.JsonToBean(AreaList.class,
						r.getContentAsString());

				for (City city : areas.getData()) {
					HashMap<String, String> map = new HashMap<>();
					map.put("tv_search_hot_ad", city.getName());
					citys.add(city);
					dataList.add(map);
				}
				SearchHotAdapter adapter = new SearchHotAdapter(
						v.gv_select_city, dataList,
						R.layout.activity_search_hot_item);
				v.gv_select_city.setAdapter(adapter);

				break;
			}
		}

	}

	private void itemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		if (citys != null) {
			City city = citys.get(position);
			HomeFragment fragment = new HomeFragment();
			saveSelectCity(city.getCode(), city.getName());
			saveCurrentCity(city.getCode(), city.getName());
			Bundle args = new Bundle();
			FragmentEntity event = new FragmentEntity();
			event.setFragment(fragment);
			EventBus.getDefault().post(event);
		}
	}

}
