package com.lansun.qmyo.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.pc.ioc.adapter.LazyAdapter;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectView;
import com.lansun.qmyo.adapter.SecretaryDetailAdapter.ViewHolder;
import com.lansun.qmyo.fragment.SecretaryDetailFragment;
import com.lansun.qmyo.R;

public class SecretaryDetailAdapter extends
		LazyAdapter<HashMap<String, Integer>, ViewHolder> {

	public SecretaryDetailAdapter(ListView listView,
			ArrayList<HashMap<String, Integer>> dataList, int layout_id,
			SecretaryDetailFragment fragment) {
		super(listView, dataList, layout_id);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		return super.getView(position, convertView, arg2);
	}

	/**
	 * 如果比较复杂的 则需要重写 这里实现的是类似getview的逻辑 但是记得不要调用super.deal(data, viewHold,
	 * position); 这里是为了能够显示所以调用
	 */
	@Override
	public void deal(HashMap<String, Integer> data, ViewHolder viewHold,
			int position) {
		if (position + 1 == dataList.size()) {
			viewHold.line.setVisibility(View.GONE);
		} else {
			viewHold.line.setVisibility(View.VISIBLE);
		}
		Integer is = data.get("iv_secretary_pic");
		viewHold.iv_secretary_pic.setImageResource(is);
	}

	/**
	 * 这里实现的是图片下载 如果不重写则使用框架中的图片下载
	 */
	@Override
	public void download(ImageView view, String url) {
		super.download(view, url);
	}

	public class ViewHolder {
		@InjectView
		private RecyclingImageView iv_secretary_pic;

		@InjectView
		private View line;

		@InjectView
		private TextView tv_secretary_detail_title;
	}
}
