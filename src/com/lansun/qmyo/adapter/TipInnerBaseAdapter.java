package com.lansun.qmyo.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.lansun.qmyo.R;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class TipInnerBaseAdapter extends BaseAdapter{

	private ArrayList<HashMap<String, String>> mDataList;
	private Activity ctx;

	public TipInnerBaseAdapter(ArrayList<HashMap<String, String>> dataList,Activity activity){
		this.mDataList = dataList;
		this.ctx = activity;
	}
	
	@Override
	public int getCount() {
		return mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	ViewHolder	holder = null;
		if (convertView == null) {
			convertView = View.inflate(ctx, R.layout.activity_activity_tip_item_inner_item, null);
			holder = new ViewHolder();
			holder.left_point = (TextView) convertView.findViewById(R.id.left_point);
			holder.tv_activity_content_mx_content = (TextView) convertView.findViewById(R.id.tv_activity_content_mx_content);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.left_point.setVisibility(View.VISIBLE);
		holder.tv_activity_content_mx_content.setText(mDataList.get(position).get("contentId"+position));
		
		return convertView;
	}
	
	class ViewHolder{
		public TextView tv_activity_content_mx_content;
		private TextView left_point;
	}
	
}
