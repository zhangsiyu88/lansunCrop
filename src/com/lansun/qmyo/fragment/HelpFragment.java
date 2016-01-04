package com.lansun.qmyo.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.lansun.qmyo.adapter.HelpAdapter;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.R;

/**
 * 帮助界面
 * 
 * @author bhxx
 * 
 */
public class HelpFragment extends BaseFragment {

	@InjectAll
	Views v;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View fl_comments_right_iv, tv_activity_shared;
		private TextView tv_activity_title;
		private ExpandableListView elv_help;
		private LinearLayout ll_click_userprotcol;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_help, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		v.fl_comments_right_iv.setVisibility(View.GONE);
//		initTitle(v.tv_activity_title, R.string.help_center, null, 0);
		initTitle(v.tv_activity_title, R.string.common_problem, null, 0);
		List<String> parents = new ArrayList<String>();
		
		parents = Arrays.asList(getResources().getStringArray(
				R.array.help_title));
		
		HelpAdapter adapter = new HelpAdapter(activity, parents,
				R.array.help_q_a);
		v.elv_help.setAdapter(adapter);
		
		
		v.ll_click_userprotcol.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				UserProtocolFragment fragment = new UserProtocolFragment();
				FragmentEntity fEntity = new FragmentEntity();
				fEntity.setFragment(fragment);
				EventBus.getDefault().post(fEntity);
			}
		});
	}

}
