package com.lansun.qmyo.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.ioc.view.listener.OnItemClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.adapter.BankCardAdapter;
import com.lansun.qmyo.adapter.InstitutionAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.BankCardData;
import com.lansun.qmyo.domain.BankCardList;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ListViewSwipeGesture;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.R;

public class InstitutionFragment extends BaseFragment {

	@InjectAll
	Views v;

	class Views {
		private MyListView lv_institution_list;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_institution, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		ArrayList<HashMap<String, Object>> dataList = new ArrayList<>();
		ArrayList<String> content = new ArrayList<>();
		content.add("广岛翻译");
		content.add("导游协会");
		content.add("官网:www.j-higa.net");
		for (int i = 0; i < 3; i++) {
			HashMap<String, Object> map = new HashMap<>();
			map.put("tv_institution_title", "中国语内饰会" + i);
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < content.size(); j++) {
				if (i == content.size()) {
					sb.append("\u2022  " + content.get(i));
				} else {
					sb.append("\u2022  " + content.get(i) + "\r\n");
				}
			}

			map.put("tv_institution_content", sb.toString());
			map.put("tv_institution_title2", "中国语内饰会2" + i);

			map.put("tv_institution_telephone", Html.fromHtml(String.format(
					getString(R.string.telephone2), "082-245-8346")));
			map.put("tv_institution_address", Html.fromHtml(String.format(
					getString(R.string.tele_address2), "730大厦")));
			dataList.add(map);
		}
		InstitutionAdapter adapter = new InstitutionAdapter(
				v.lv_institution_list, dataList, R.layout.institution_item);
		v.lv_institution_list.setAdapter(adapter);
	}

}