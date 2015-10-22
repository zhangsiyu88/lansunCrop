package com.lansun.qmyo.adapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.lansun.qmyo.R;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class DetailHeaderAdapter extends CommonAdapter implements Serializable {

	private DisplayImageOptions options;

	public DetailHeaderAdapter(Context activity, ArrayList<String> data) {
		this.inflater = LayoutInflater.from(activity);
		this.activity = activity;
		this.data = data;
		options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true)
				.displayer(new FadeInBitmapDisplayer(300)).build();
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@SuppressWarnings("deprecation")
	@Override
	public View view(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.store_detail_head_item,
					null);
		}
		DisplayMetrics dm = activity.getResources().getDisplayMetrics();
		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;
		// int cueentHeight = (int)(((double)screenWidth/720) *
		// activity.getResources().getDimension(R.dimen.store_detail_head_h));
		// int cueentWidth = (int)(((double)screenHeight/1080) *
		// activity.getResources().getDimension(R.dimen.store_detail_head_w));
		ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
				(int) activity.getResources().getDimension(
						R.dimen.store_detail_head_w), (int) activity
						.getResources().getDimension(
								R.dimen.store_detail_head_h));
		((ImageView) convertView).setLayoutParams(layoutParams);
		((ImageView) convertView).setScaleType(ScaleType.CENTER);
		((ImageView) convertView).setImageResource(R.drawable.default_list);
		ImageLoader.getInstance().displayImage(data.get(position),
				(ImageView) convertView, options);
		return convertView;
	}

}
