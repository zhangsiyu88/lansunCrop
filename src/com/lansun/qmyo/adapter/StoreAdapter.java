package com.lansun.qmyo.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.pc.ioc.adapter.LazyAdapter;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectView;
import com.lansun.qmyo.adapter.StoreAdapter.ViewHolder;

/**
 * 
 * @author bhxx
 * 
 */
public class StoreAdapter extends
		LazyAdapter<HashMap<String, String>, ViewHolder> {

	public StoreAdapter(ListView listView,
			ArrayList<HashMap<String, String>> dataList, int layout_id) {
		super(listView, dataList, layout_id);
	}

	@Override
	public void deal(HashMap<String, String> data, ViewHolder viewHold,
			int position) {
		if (position + 1 == dataList.size()) {
			viewHold.line.setVisibility(View.GONE);
		} else {
			viewHold.line.setVisibility(View.VISIBLE);
		}
		String name = data.get("tv_store_item_name");
		String num = data.get("tv_store_item_num");
		String distance = data.get("tv_store_item_distance");
		
		String rb = data.get("rb_store_item");
		Integer rbInt = Integer.valueOf(rb);
		rbInt = rbInt*2;
		
		
		viewHold.tv_store_item_name.setText(name);
		viewHold.tv_store_item_num.setText(num);
		viewHold.tv_store_item_distance.setText(distance);
		viewHold.rb_store_item.setProgress(rbInt);
		/*viewHold.rb_store_item.setProgress(Integer.parseInt(rb));*/
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
		private RecyclingImageView iv_store_iem_gz, iv_shop_lv;
		@InjectView
		private TextView tv_store_item_name, tv_store_item_num,
				tv_store_item_distance;
		@InjectView
		private RatingBar rb_store_item;
		@InjectView
		private View line, rl_store_item_gz;

	}

}
