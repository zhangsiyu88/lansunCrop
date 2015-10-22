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
import com.lansun.qmyo.adapter.MineV16Adapter.ViewHolder;
import com.lansun.qmyo.view.CircularImage;
import com.lansun.qmyo.R;

/**
 * 我的私人秘书列表
 * 
 * @author bhxx
 * 
 */
public class MineV16Adapter extends
		LazyAdapter<HashMap<String, String>, ViewHolder> {

	public MineV16Adapter(ListView listView,
			ArrayList<HashMap<String, String>> dataList, int layout_id) {
		super(listView, dataList, layout_id);
	}

	/**
	 * 如果比较复杂的 则需要重写 这里实现的是类似getview的逻辑 但是记得不要调用super.deal(data, viewHold,
	 * position); 这里是为了能够显示所以调用
	 */
	@Override
	public void deal(HashMap<String, String> data, ViewHolder viewHold,
			int position) {
		super.deal(data, viewHold, position);

		int resId = R.drawable.article_shopping;
		switch (data.get("tv_mine_v16_tag")) {
		case "100000":
			resId = R.string.shopping_carnival;
			break;
		case "200000":
			resId = R.string.travel_holiday;
			break;
		case "300000":
			resId = R.string.dining;
			break;
		case "400000":
			resId = R.string.courtesy_car;
			break;
		case "600000":
			resId = R.string.life_service;
			break;
		case "500000":
			resId = R.string.entertainment;
			break;
		case "700000":
			resId = R.string.integral;
			break;
		case "800000":
			resId = R.string.investment;
			break;
		}
		viewHold.tv_mine_v16_tag.setText(resId);

		if (position + 1 == dataList.size()) {
			viewHold.line.setVisibility(View.GONE);
		} else {
			viewHold.line.setVisibility(View.VISIBLE);
		}
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
		private View line;
		@InjectView
		private RecyclingImageView iv_mine_v16_head;
		@InjectView
		private TextView tv_mine_v16_name, tv_mine_v16_tag, tv_mine_v16_time;
	}
}
