package com.lansun.qmyo.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.ioc.view.listener.OnItemClick;
import com.android.pc.util.Handler_Inject;
import com.lansun.qmyo.adapter.FeedBackAdapter;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

public class FeedBackListFragment extends BaseFragment {

	@InjectView(binders = { @InjectBinder(listeners = { OnItemClick.class }, method = "itemClick") })
	private MyListView lv_feedback_list;
	private int[] titleRes = new int[] { R.string.hd_feedback,
			R.string.sh_feedback, R.string.yhk_feedback, R.string.js_feedback,
			R.string.qt_feedback, };
	private String[] types = new String[] { "activity", "merchant", "bankcard",
			"technology", "other" };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_feedback_list, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		ArrayList<HashMap<String, String>> dataList = new ArrayList<>();
		for (int i = 0; i < titleRes.length; i++) {
			HashMap<String, String> map = new HashMap<>();
			map.put("tv_feedback_title", activity.getString(titleRes[i]));
			dataList.add(map);
		}
		FeedBackAdapter adapter = new FeedBackAdapter(lv_feedback_list,
				dataList, R.layout.activity_feedback_item);
		lv_feedback_list.setAdapter(adapter);

	}

	private void itemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		String title = activity.getString(titleRes[position]);
		FeedBackFragment fragment = new FeedBackFragment();
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putString("type", types[position]);
		fragment.setArguments(args);
		FragmentEntity event = new FragmentEntity();
		event.setFragment(fragment);
		EventBus.getDefault().post(event);
	}

}
