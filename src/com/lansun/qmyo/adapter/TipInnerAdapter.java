package com.lansun.qmyo.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.pc.ioc.adapter.LazyAdapter;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectView;
import com.lansun.qmyo.adapter.TipInnerAdapter.ViewHolder;
import com.lansun.qmyo.utils.LogUtils;

public class TipInnerAdapter extends
		LazyAdapter<HashMap<String, String>, ViewHolder> {

	public ArrayList<HashMap<String, String>> mDataList;
	public TipInnerAdapter(ListView listView,
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
		
		/*if (position + 1 == dataList.size()) {
			viewHold.line.setVisibility(View.GONE);
		} else {
			viewHold.line.setVisibility(View.VISIBLE);
		}*/
		
		viewHold.left_point.setVisibility(View.VISIBLE);
		viewHold.tv_activity_content_mx_content.setText(mDataList.get(position).get("contentId"+position));
		
		LogUtils.toDebugLog("insertInfoList", "展示效果为："+mDataList.get(position).get("contentId"+position));
		
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
				tv_activity_content_mx_content;
		@InjectView
		private View line;*/
		
		@InjectView
		private TextView left_point;
		
		@InjectView
		private TextView tv_activity_content_mx_content;
	}
}
