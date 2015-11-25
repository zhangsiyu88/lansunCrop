package com.lansun.qmyo.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.pc.ioc.adapter.LazyAdapter;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectView;
import com.lansun.qmyo.R;
import com.lansun.qmyo.adapter.TipAdapter.ViewHolder;
import com.lansun.qmyo.utils.LogUtils;

public class TipAdapter extends
		LazyAdapter<HashMap<String, String>, ViewHolder> {

	private String contentStr;
	private ArrayList<HashMap<String, String>> insertInfoList;
	private HashMap<String, String> hashMap;
	private ArrayList<HashMap<String, String>> mDataList;
	private Activity mActivity;

	public TipAdapter(ListView listView,
			ArrayList<HashMap<String, String>> dataList, int layout_id,Activity activity) {
		super(listView, dataList, layout_id);
		mDataList = dataList;
		mActivity = activity;
	}
	public TipAdapter(ListView listView,
			ArrayList<HashMap<String, String>> dataList, int layout_id) {
		super(listView, dataList, layout_id);
		mDataList = dataList;
	}

	/**
	 * 如果比较复杂的 则需要重写 这里实现的是类似getview的逻辑 但是记得不要调用super.deal(data, viewHold,
	 * position); 这里是为了能够显示所以调用
	 */
	@Override
	public void deal(HashMap<String, String> data, ViewHolder viewHold,
			int position) {
		//super.deal(data, viewHold, position);
		if (position + 1 == dataList.size()) {
			viewHold.line.setVisibility(View.GONE);
		} else {
			viewHold.line.setVisibility(View.VISIBLE);
		}
		
		//拿到第position位置的map中的标题
		viewHold.tv_activity_content_mx_title.setText(mDataList.get(position).get("tv_activity_content_mx_title"));
		
		
		/**
		 * 当前的ListView是单独的每个标题下的内容，此中的dataList是要传给这个ListView展示对应的每个标题下面的所有content
		 */
		int mapNum = dataList.get(position).size()-1;
		insertInfoList = new ArrayList<HashMap<String, String>>();
		hashMap = new HashMap<String, String>();
		
		for(int j= 0;j<mapNum;j++){
			contentStr = mDataList.get(position).get("tv_activity_content_mx_content"+j);
			hashMap.put("contentId"+j, contentStr);
			insertInfoList.add(j, hashMap);
		}
		
		LogUtils.toDebugLog("insertInfoList", insertInfoList.toString());
		
		/*TipInnerAdapter tipInnerAdapter = new TipInnerAdapter(viewHold.lv_singleItemInnerListView, insertInfoList, R.layout.activity_activity_tip_item_inner_item);
		viewHold.lv_singleItemInnerListView.setAdapter(tipInnerAdapter);*/
		
		TipInnerBaseAdapter tipInnerBaseAdapter = new TipInnerBaseAdapter(insertInfoList, mActivity);
		viewHold.lv_singleItemInnerListView.setAdapter(tipInnerBaseAdapter);
	}

	/**
	 * 这里实现的是图片下载 如果不重写则使用框架中的图片下载
	 */
	@Override
	public void download(ImageView view, String url) {
		super.download(view, url);
	}

	public class ViewHolder {
		/*@InjectView
		private TextView tv_activity_content_mx_title,
				tv_activity_content_mx_content;*/
		
		@InjectView
		private View line;
		
		@InjectView
		private  ListView lv_singleItemInnerListView;
		
		@InjectView
		private TextView tv_activity_content_mx_title;
	}
	
}
