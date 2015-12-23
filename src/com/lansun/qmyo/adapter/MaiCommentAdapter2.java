package com.lansun.qmyo.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.pc.ioc.adapter.LazyAdapter;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.ioc.view.listener.OnItemClick;
import com.lansun.qmyo.adapter.MaiCommentAdapter2.ViewHolder;
import com.lansun.qmyo.event.entity.ListViewEntity;
import com.lansun.qmyo.utils.GridViewUtils;
import com.lansun.qmyo.view.CircularImage;
import com.lansun.qmyo.view.ImageGalleryDialog;
import com.lansun.qmyo.view.MyGridView;
import com.lansun.qmyo.R;
/**
 * 迈友评论列表（没有回复的）
 * 
 * @author bhxx
 * 
 */
public class MaiCommentAdapter2 extends
		LazyAdapter<HashMap<String, Object>, ViewHolder> {

	private ArrayList<HashMap<String, Object>> dataList;
	private Fragment activity;

	public void setActivity(Fragment activity) {
		this.activity = activity;
	}

	public MaiCommentAdapter2(ListView listView,
			ArrayList<HashMap<String, Object>> dataList, int layout_id) {
		super(listView, dataList, layout_id);
		this.dataList = dataList;
	}

	@Override
	public int getCount() {
		if (dataList.size() > 1) {
			return 2;
		}
		return super.getCount();
	}

	/**
	 * 如果比较复杂的 则需要重写 这里实现的是类似getview的逻辑 但是记得不要调用super.deal(data, viewHold,
	 * position); 这里是为了能够显示所以调用
	 */
	@Override
	public void deal(HashMap<String, Object> data, final ViewHolder viewHold,
			int position) {

		if (position + 1 == 2) {
			viewHold.line.setVisibility(View.GONE);
		} else {
			viewHold.line.setVisibility(View.VISIBLE);
		}
		String headUrl = data.get("iv_comment_head").toString();
		String name = data.get("tv_comment_name").toString();
		String time = data.get("tv_comment_time").toString();
		String desc = data.get("tv_comment_desc").toString();
		final ArrayList<String> photos = (ArrayList<String>) data.get("photos");
		download(viewHold.iv_comment2_head, headUrl);
		viewHold.tv_comment2_name.setText(name);
		viewHold.tv_comment2_time.setText(time);
		
		viewHold.tv_comment2_desc.setText(desc);
		viewHold.tv_comment2_desc.setMaxLines(5);
		
		/*if (desc.length() < 50) {
			viewHold.iv_comment2_more.setVisibility(View.GONE);
		} else {
			viewHold.iv_comment2_more.setVisibility(View.VISIBLE);
		}*/
		
		MaiCommentGVAdapter gvAdapter = new MaiCommentGVAdapter(context, photos);
		viewHold.gv_comment2_images.setAdapter(gvAdapter);
		/*
		 * 此处设定了GridView的布局属性
		 */
		GridViewUtils.updateGridViewLayoutParams(viewHold.gv_comment2_images,
				4, (int) context.getResources().getDimension(R.dimen.l_r_10));
		
		
		if (photos == null) {
			viewHold.tv_mai_images_count.setVisibility(View.GONE);
			viewHold.gv_comment2_images.setVisibility(View.GONE);
			viewHold.gv_comment2_images.setAdapter(null);
		}
		if (photos != null && photos.size() > 0) {
			viewHold.gv_comment2_images.setVisibility(View.VISIBLE);
			viewHold.tv_mai_images_count.setText(String.format(
					context.getString(R.string.images_count), photos.size()));
			viewHold.tv_mai_images_count.setVisibility(View.VISIBLE);
		}
		viewHold.gv_comment2_images
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						DetailHeaderPagerAdapter headPagerAdapter = new DetailHeaderPagerAdapter(
								context, photos);
						ImageGalleryDialog dialog = new ImageGalleryDialog().newInstance(headPagerAdapter, position);
						dialog.show(activity.getFragmentManager(), "gallery");
					}
				});

		/* 
		 * 暂时禁止掉评论的点击事件
		 * 
		 * viewHold.ll_comment_desc.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View arg0) {
				if (viewHold.tv_comment2_desc.getMaxLines() > 5) {
					viewHold.tv_comment2_desc.setMaxLines(5);
					viewHold.iv_comment2_more
							.setImageResource(R.drawable.arrow_down);
				} else {
					viewHold.tv_comment2_desc.setMaxLines(Integer.MAX_VALUE);
					viewHold.iv_comment2_more
							.setImageResource(R.drawable.arrow_up);
				}
			}
		});*/

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
		private RecyclingImageView iv_comment2_more;
		@InjectView
		private CircularImage iv_comment2_head;
		@InjectView
		private TextView tv_comment2_name, tv_comment2_time, tv_comment2_desc,
				tv_mai_images_count;
		@InjectView
		private MyGridView gv_comment2_images;
		@InjectView
		private View line, ll_comment_desc;
	}

}
