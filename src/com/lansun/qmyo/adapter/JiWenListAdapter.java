package com.lansun.qmyo.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.services.core.v;
import com.android.pc.ioc.adapter.LazyAdapter;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectView;
import com.lansun.qmyo.adapter.JiWenListAdapter.ViewHolder;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.event.entity.RefreshJiWenEntity;
import com.lansun.qmyo.fragment.JiWenListFragment2;
import com.lansun.qmyo.R;

public class JiWenListAdapter extends
		LazyAdapter<HashMap<String, Object>, ViewHolder> {

	public JiWenListAdapter(ListView listView,
			ArrayList<HashMap<String, Object>> dataList, int layout_id) {
		super(listView, dataList, layout_id);
	}

	/**
	 * 如果比较复杂的 则需要重写 这里实现的是类似getview的逻辑 但是记得不要调用super.deal(data, viewHold,
	 * position); 这里是为了能够显示所以调用
	 */
	@Override
	public void deal(HashMap<String, Object> data, ViewHolder viewHold,
			int position) {
		String photo = data.get("iv_jiwen_item_head").toString().trim();
		final ArrayList<String> tags = (ArrayList<String>) data.get("iv_jiwen_item_type");
		String tag = data.get("tv_jiwen_item_type_desc").toString().trim();
		String desc = data.get("tv_jiwen_item_desc").toString().trim();
		String time = data.get("tv_jiwen_item_time").toString().trim();
		if (!TextUtils.isEmpty(tag)) {
			viewHold.tv_jiwen_item_type_desc.setText(tag);
		}
		if (!TextUtils.isEmpty(desc)) {
			viewHold.tv_jiwen_item_desc.setText(desc);
		}
		if (!TextUtils.isEmpty(photo)) {
			download(viewHold.iv_jiwen_item_head, photo);
		}
		if (!TextUtils.isEmpty(time)) {
			viewHold.tv_jiwen_item_time.setText(time);
		}
		int resId = R.drawable.article_shopping;
		
		switch (tags.get(0)) {
		case "100000":
			resId = R.drawable.article_shopping_1;
			break;
		case "200000":
			resId = R.drawable.article_holiday_1;
			break;
		case "300000":
			resId = R.drawable.article_food_1;
			break;
		case "400000":
			resId = R.drawable.article_car_1;
			break;
		case "600000":
			resId = R.drawable.article_life_1;
			break;
		case "500000":
			resId = R.drawable.article_play_1;
			break;
		case "700000":
			resId = R.drawable.article_integral_1;
			break;
		case "800000":
			resId = R.drawable.article_investment_1;
			break;
		}
		viewHold.iv_jiwen_item_type.setBackgroundResource(resId);

		viewHold.iv_jiwen_item_type.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				JiWenListFragment2 fragment = new JiWenListFragment2();
				Bundle args = new Bundle();
				args.putString("service", tags.get(0));
				fragment.setArguments(args);
				FragmentEntity event = new FragmentEntity();
				event.setFragment(fragment);
				EventBus.getDefault().post(event);
			}
		});
	}

	/**
	 * 这里实现的是图片下载 如果不重写则使用框架中的图片下载
	 */
	@Override
	public void download(ImageView view, String url) {
		// DisplayMetrics dm = context.getResources().getDisplayMetrics();
		// int screenWidth = dm.widthPixels;
		// int screenHeight = dm.heightPixels;
		// int cueentHeight = (int) (((double) screenWidth / 720) * view
		// .getLayoutParams().height);
		// LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		// screenWidth, cueentHeight);
		// view.setLayoutParams(params);
		super.download(view, url);
	}

	public class ViewHolder {
		@InjectView
		private RecyclingImageView iv_jiwen_item_head, iv_jiwen_item_type;
		@InjectView
		private TextView tv_jiwen_item_type_desc, tv_jiwen_item_time,
				tv_jiwen_item_desc;
	}
}
