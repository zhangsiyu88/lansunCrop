package com.lansun.qmyo.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class CommonAdapter extends BaseAdapter {
	protected LayoutInflater inflater;
	public ArrayList<String> data;
	public Context activity;

	public CommonAdapter() {
	}

	public CommonAdapter(Activity activity,
			ArrayList<String> data) {
		this.activity = activity;
		this.data = data;
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return view(position, convertView, parent);
	}

	public abstract View view(int position, View convertView, ViewGroup parent);
}
