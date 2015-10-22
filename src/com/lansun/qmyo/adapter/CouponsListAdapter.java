package com.lansun.qmyo.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.pc.ioc.adapter.LazyAdapter;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectView;
import com.lansun.qmyo.adapter.CouponsListAdapter.ViewHolder;

public class CouponsListAdapter extends LazyAdapter<HashMap<String, String>, ViewHolder> {

	public CouponsListAdapter(ListView listView,ArrayList<HashMap<String, String>> dataList, int layout_id) {
		super(listView, dataList, layout_id);
	}
	
	/**
	 * 如果比较复杂的 则需要重写 这里实现的是类似getview的逻辑
	 * 但是记得不要调用super.deal(data, viewHold, position);
	 * 这里是为了能够显示所以调用
	 */
	@Override
	public void deal(HashMap<String, String> data, ViewHolder viewHold, int position) {
		super.deal(data, viewHold, position);
	}
	/**
	 * 这里实现的是图片下载 如果不重写则使用框架中的图片下载
	 */
	@Override
	public void download(ImageView view, String url) {
		super.download(view, url);
	}	
	
	
	public class ViewHolder{
		@InjectView
		private RecyclingImageView iv_coupons_bg,iv_coupons_qr_code;
		@InjectView
		private TextView tv_coupons_money,tv_coupons_time,tv_coupons_shengyu_day_num;
	}
}
