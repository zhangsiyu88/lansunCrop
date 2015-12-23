package com.lansun.qmyo.adapter;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.pc.ioc.event.EventBus;
import com.lansun.qmyo.event.entity.CloseEntity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.lansun.qmyo.R;

/**
 * 大图滚动pagerAdapter
 * 
 * @author bhxx
 * 
 */
public class DetailHeaderPagerAdapter extends CommonPagerAdapter implements
		Serializable {
	protected LayoutInflater inflater;
	private ArrayList<String> photos;
	private DisplayImageOptions options;

	public DetailHeaderPagerAdapter(Context context, ArrayList<String> photos) {
		this.inflater = LayoutInflater.from(context);
		this.context = context;
		this.photos = photos;
		options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true)
				.displayer(new FadeInBitmapDisplayer(300)).build();
	}

	@Override
	public int getCount() {
		if (photos != null) {
			return photos.size();
		}
		return 0;
	}

	public Object instantiateItem(ViewGroup view, int position) {
		
		// view.removeAllViews();
		View convertView = inflater.inflate(R.layout.store_detail_head_item,null);
		
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		if (photos.get(position).contains("http")) {
			ImageLoader.getInstance().displayImage(
					photos.get(position) + "?imageView2/4/w/" + dm.widthPixels
							+ "/format/jpg", (ImageView) convertView, options);
		} else {
			ImageLoader.getInstance().displayImage(photos.get(position),
					(ImageView) convertView, options);
		}
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				CloseEntity event = new CloseEntity();
				event.setClose(true);
				EventBus.getDefault().post(event);
			}
		});
		pageMap.put(position, convertView);
		
		view.addView(convertView);
		
		return convertView;
	}
	private int mChildCount = 0;

	@Override
	public void notifyDataSetChanged() {
		mChildCount = getCount();
		super.notifyDataSetChanged();
	}

	@Override
	public int getItemPosition(Object object) {
		if (mChildCount > 0) {
			mChildCount--;
			return POSITION_NONE;
		}
		return super.getItemPosition(object);
	}

	@Override
	public void destroyItem(View collection, int position, Object o) {
		((ViewGroup) collection).removeView((View) o);
	}
	
	
}
