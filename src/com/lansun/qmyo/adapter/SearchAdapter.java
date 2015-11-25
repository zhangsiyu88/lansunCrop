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
import com.lansun.qmyo.R;
import com.lansun.qmyo.adapter.SearchAdapter.ViewHolder;

/**
 * 搜索adapter
 * 
 * @author bhxx
 * 
 */
public class SearchAdapter extends
		LazyAdapter<HashMap<String, Object>, ViewHolder> {

	public SearchAdapter(ListView listView,
			ArrayList<HashMap<String, Object>> dataList, int layout_id) {
		super(listView, dataList, layout_id);
	}

	/**
	 * 如果比较复杂的 则需要重写 这里实现的是类似getview的逻辑 但是记得不要调用super.deal(data, viewHold,
	 * position); 这里是为了能够显示所以调用
	 */
	@Override
	public void deal(HashMap<String, Object> data, ViewHolder viewHold,int position) {

		if (position+1 == dataList.size()) {//当获取到的数据列表的size()正好为view的位置position+1时,那么最后一个viewHold中line的可见性设置为GONE掉
			viewHold.line.setVisibility(View.GONE);
		}else {
			viewHold.line.setVisibility(View.VISIBLE);
		}
		
		if (data.get("tv_search_activity_name") != null) {
			/*String name = data.get("tv_search_activity_name").toString();
			viewHold.tv_search_activity_name.setText(name);*/
			/*
			 * 暂时将上端数据屏掉，为了调换活动名和描述内容
			 */
			String name = data.get("tv_search_activity_desc").toString();
			viewHold.tv_search_activity_name.setText(name);
			
		}
		if (data.get("tv_search_activity_distance") != null) {
			String distance = data.get("tv_search_activity_distance")
					.toString();
			viewHold.tv_search_activity_distance.setText(distance);
		}
		if (data.get("tv_search_activity_desc") != null) {
			/*String desc = data.get("tv_search_activity_desc").toString();
			viewHold.tv_search_activity_desc.setText(desc);*/
			/*
			 * 暂时将上端数据屏掉，为了调换活动名和描述内容
			 */
			String desc = data.get("tv_search_activity_name").toString();
			viewHold.tv_search_activity_desc.setText(desc);
		}
		if (data.get("iv_search_activity_head") != null) {
			String head = data.get("iv_search_activity_head").toString();
			/*download(viewHold.iv_search_activity_head, null);*/
			viewHold.iv_search_activity_head.setImageResource(R.drawable.default_list);
			download(viewHold.iv_search_activity_head, head);//传过来的图片
		}
		if (data.get("tv_search_tag") != null) {
			String tag = data.get("tv_search_tag").toString();
			viewHold.tv_search_tag.setText(tag);//标签，例如某银行卡或迈界
		}
		
		
		//标签的列表展示出来
		ArrayList<String> iconArray = (ArrayList<String>) data.get("icons");

		/*if (iconArray != null) {
			for (String icon : iconArray) {
				if ("discount".equals(icon)) {
					viewHold.iv_search_activity_discount.setVisibility(View.VISIBLE);
				} else if ("new".equals(icon)) {
					viewHold.iv_search_activity_new.setVisibility(View.VISIBLE);
				} else if ("point".equals(icon)) {
					viewHold.iv_search_activity_point.setVisibility(View.VISIBLE);
				} else if ("staging".equals(icon)) {
					viewHold.iv_search_activity_staging.setVisibility(View.VISIBLE);
				} else if ("coupon".equals(icon)) {
					viewHold.iv_search_activity_coupon.setVisibility(View.VISIBLE);
				}
			}
		}*/
		
		
//		探测首页的标签消失是否和这里的标签移除有关
		viewHold.iv_search_activity_discount.setVisibility(View.GONE);
		viewHold.iv_search_activity_new.setVisibility(View.GONE);
		viewHold.iv_search_activity_point.setVisibility(View.GONE);
		viewHold.iv_search_activity_staging.setVisibility(View.GONE);
		viewHold.iv_search_activity_coupon.setVisibility(View.GONE);
		//因为ListViewItem的复用造成了数据的错误显示，所以在这种情况下需要将前面复用的部件全部清除掉
		if (iconArray != null) {
			for (String icon : iconArray) {
				if ("discount".equals(icon)) {
					viewHold.iv_search_activity_discount.setVisibility(View.VISIBLE);
				} else if ("new".equals(icon)) {
					viewHold.iv_search_activity_new.setVisibility(View.VISIBLE);
				} else if ("point".equals(icon)) {
					viewHold.iv_search_activity_point.setVisibility(View.VISIBLE);
				} else if ("staging".equals(icon)) {
					viewHold.iv_search_activity_staging.setVisibility(View.VISIBLE);
				} else if ("coupon".equals(icon)) {
					viewHold.iv_search_activity_coupon.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	/**
	 * 这里实现的是图片下载 如果不重写则使用框架中的图片下载
	 * 其实框架中的图片下载已经被修改成了UniversalImageLoader去完成的————byZdy
	 */
	@Override
	public void download(ImageView view, String url) {
		super.download(view, url);
	}

	public class ViewHolder {
		@InjectView
		private RecyclingImageView iv_search_activity_head,
				iv_search_activity_point, iv_search_activity_coupon,
				iv_search_activity_staging, iv_search_activity_new,
				iv_search_activity_discount;
		@InjectView
		private TextView tv_search_activity_name, tv_search_activity_distance,
				tv_search_activity_desc, tv_search_tag;

		@InjectView
		private View line;
	}

}
