package com.lansun.qmyo.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.pc.ioc.adapter.LazyAdapter;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectView;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.SearchBankCardFragment;
import com.lansun.qmyo.fragment.UserProtocolFragment;
import com.lansun.qmyo.view.MySubListView;
import com.lansun.qmyo.R;

public class HelpAdapter extends BaseExpandableListAdapter {

	private List<String> parents;
	private Context ctx;
	private int childsId;

	public HelpAdapter(Context ctx, List<String> parents, int childsId) {
		this.ctx = ctx;
		this.parents = parents;
		this.childsId = childsId;
	}

	@Override
	public Object getChild(int arg0, int arg1) {
		return arg0;
	}

	@Override
	public long getChildId(int arg0, int arg1) {
		return arg1;
	}

	@Override
	public View getChildView(int arg0, int arg1, boolean arg2, View arg3,
			ViewGroup arg4) {
		
		View view = LayoutInflater.from(ctx).inflate(R.layout.help_q_a, null);
		TextView tv_help_q_a = (TextView) view.findViewById(R.id.tv_help_q_a);
		
//		if(arg0 == 5 && view.getVisibility()==View.INVISIBLE){
//			UserProtocolFragment fragment = new UserProtocolFragment();
//			FragmentEntity fEntity = new FragmentEntity();
//			fEntity.setFragment(fragment);
//			EventBus.getDefault().post(fEntity);
//			return view;
//		}
		
		tv_help_q_a.setText(Html.fromHtml(ctx.getResources().getStringArray(childsId)[arg0]));
		
		return view;
	}

	@Override
	public int getChildrenCount(int arg0) {
		return 1;
	}

	@Override
	public Object getGroup(int arg0) {
		return parents.get(arg0);
	}

	@Override
	public int getGroupCount() {
		return parents.size();
	}

	@Override
	public long getGroupId(int arg0) {
		return arg0;
	}

	@Override
	public View getGroupView(int arg0, boolean arg1, View arg2, ViewGroup arg3) {
		TextView tv_help_title = (TextView) LayoutInflater.from(ctx).inflate(
				R.layout.help_title, null);
		tv_help_title.setText(parents.get(arg0));
		tv_help_title.setHeight((int) ctx.getResources().getDimension(
				R.dimen.item_h_90));
		tv_help_title.setPadding(
				(int) ctx.getResources().getDimension(R.dimen.l_r_35), 0, 0, 0);
		
		return tv_help_title;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		return false;
	}
}
