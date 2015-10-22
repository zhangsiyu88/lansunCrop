package com.lansun.qmyo.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.services.core.v;
import com.android.pc.ioc.adapter.LazyAdapter;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectView;
import com.lansun.qmyo.adapter.QuestionDetailAdapter.ViewHolder;
import com.lansun.qmyo.view.CircularImage;

public class QuestionDetailAdapter extends
		LazyAdapter<HashMap<String, String>, ViewHolder> {

	public QuestionDetailAdapter(ListView listView,
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

		if (TextUtils.isEmpty(data.get("iv_secretary_head"))) {
			viewHold.rl_secretary_answer.setVisibility(View.GONE);
		}
		if (!TextUtils.isEmpty(data.get("tv_secretary_answer"))) {
			viewHold.rl_secretary_answer.setVisibility(View.VISIBLE);
		}
		super.deal(data, viewHold, position);
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
		private CircularImage iv_secretary_head, iv_user_head;
		@InjectView
		private View rl_secretary_answer;
		@InjectView
		private TextView tv_user_question, tv_secretary_answer;
	}
}
