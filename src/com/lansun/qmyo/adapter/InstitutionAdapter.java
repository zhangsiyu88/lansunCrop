package com.lansun.qmyo.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.pc.ioc.adapter.LazyAdapter;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.view.listener.OnClick;
import com.lansun.qmyo.adapter.InstitutionAdapter.ViewHolder;

public class InstitutionAdapter extends
		LazyAdapter<HashMap<String, Object>, ViewHolder> {

	public InstitutionAdapter(ListView listView,
			ArrayList<HashMap<String, Object>> dataList, int layout_id) {
		super(listView, dataList, layout_id);
	}

	/**
	 * 如果比较复杂的 则需要重写 这里实现的是类似getview的逻辑 但是记得不要调用super.deal(data, viewHold,
	 * position); 这里是为了能够显示所以调用
	 */
	@Override
	public void deal(HashMap<String, Object> data, final ViewHolder viewHold,
			int position) {
		viewHold.iv_institution_open.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (viewHold.ll_institution_content.getVisibility() == View.VISIBLE) {
					viewHold.ll_institution_content.setVisibility(View.GONE);
				} else {
					viewHold.ll_institution_content.setVisibility(View.VISIBLE);
				}
			}
		});
		super.deal(data, viewHold, position);
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
		private RecyclingImageView iv_institution_open;
		@InjectView
		private TextView tv_institution_title, tv_institution_content,
				tv_institution_title2, tv_institution_telephone,
				tv_institution_address;
		@InjectView
		private View ll_institution_content;
	}
}
