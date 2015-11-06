package com.lansun.qmyo.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.zip.Inflater;

import com.android.pc.ioc.event.EventBus;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.ActivityFragment;
import com.lansun.qmyo.fragment.ActivityFragment1;
import com.lansun.qmyo.R;

import android.R.raw;
import android.content.Context;
import android.os.Bundle;
import android.renderscript.Type;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/**
 * 主页的菜单滑动
 * 
 * @author bhxx
 * 
 */
public class HomePagerAdapter extends CommonPagerAdapter implements
		OnClickListener {
	protected LayoutInflater inflater;

	public HomePagerAdapter(Context context) {
		this.inflater = LayoutInflater.from(context);
		this.context = context;
	}

	@Override
	public int getCount() {
		if (isChina) {
			return 2;
		}
		return 1;
	}

	public Object instantiateItem(ViewGroup view, int position) {
		View item = null;
		//不是China的话，则只有四个页面的
		if (!isChina) {
			item = inflater.inflate(R.layout.activity_home_pager_item3, null);
			item.findViewById(R.id.ll_shopping_carnival).setOnClickListener(this);
			item.findViewById(R.id.ll_travel_holiday).setOnClickListener(this);
			item.findViewById(R.id.ll_dining).setOnClickListener(this);
			item.findViewById(R.id.ll_entertainment).setOnClickListener(this);
			view.addView(item);
			pageMap.put(position, item);
			return item;
		}
		
		if (position == 0) {
			item = inflater.inflate(R.layout.activity_home_pager_item, null);
			item.findViewById(R.id.ll_shopping_carnival).setOnClickListener(this);
			item.findViewById(R.id.ll_travel_holiday).setOnClickListener(this);
			item.findViewById(R.id.ll_dining).setOnClickListener(this);
			/*item.findViewById(R.id.ll_courtesy_car).setOnClickListener(this);汽车礼遇 改为 休闲娱乐*/
			item.findViewById(R.id.ll_entertainment).setOnClickListener(this);
		}
		if (position == 1) {
			item = inflater.inflate(R.layout.activity_home_pager_item2, null);
			item.findViewById(R.id.ll_lift_service).setOnClickListener(this);
			/*item.findViewById(R.id.ll_entertainment).setOnClickListener(this);休闲娱乐 改为 汽车礼遇*/
			item.findViewById(R.id.ll_courtesy_car).setOnClickListener(this);
			item.findViewById(R.id.ll_integral).setOnClickListener(this);
			item.findViewById(R.id.ll_investment).setOnClickListener(this);
		}

		pageMap.put(position, item);
		view.addView(item);
		return item;
	}

	@Override
	public void onClick(View v) {
		int type = 0;
		switch (v.getId()) {
		case R.id.ll_shopping_carnival:
			type = R.string.shopping_carnival;
			break;
		case R.id.ll_travel_holiday:
			type = R.string.travel_holiday;
			break;
		case R.id.ll_dining:
			type = R.string.dining;
			break;
		case R.id.ll_courtesy_car:
			type = R.string.courtesy_car;
			break;
		case R.id.ll_lift_service:
			type = R.string.life_service;
			break;
		case R.id.ll_entertainment:
			type = R.string.entertainment;
			break;
		case R.id.ll_integral:
			type = R.string.integral;
			break;
		case R.id.ll_investment:
			type = R.string.investment;
			break;
		}
		EventBus bus = EventBus.getDefault();
		FragmentEntity entity = new FragmentEntity();
		
		//NB的ActivityFragment带上需要的类型参数，去生成自己的造型去了
		
		
		ActivityFragment fragment = new ActivityFragment();//TODO
		
		/*ActivityFragment1 fragment = new ActivityFragment1();*/
		
		Bundle args = new Bundle();
		args.putInt("type", type);
		fragment.setArguments(args);
		entity.setFragment(fragment);
		bus.post(entity);
	}

	boolean isChina = true;

	public void setIsChina(boolean isChina) {
		this.isChina = isChina;
	}
}
