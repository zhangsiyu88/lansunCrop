package com.lansun.qmyo.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 通同viewpage适配器
 */
public class CommonPagerAdapter extends PagerAdapter {

	protected LayoutInflater inflater;
	protected Context context;
	protected HashMap<Integer, View> pageMap = new HashMap<Integer, View>();

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public void destroyItem(ViewGroup view, int position, Object object) {
		if (pageMap.containsKey(position)) {
			pageMap.remove(position);
			view.removeView(pageMap.get(position));
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}

}
