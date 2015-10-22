package com.lansun.qmyo.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.lansun.qmyo.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;


public class ActivityDetailPagerAdapter extends CommonPagerAdapter {
	protected LayoutInflater inflater;
	private ArrayList<String> photos;
	private DisplayImageOptions options = new DisplayImageOptions.Builder()
			.cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
			.displayer(new FadeInBitmapDisplayer(300)).build();;

	public ActivityDetailPagerAdapter(Activity context, ArrayList<String> photos) {
		this.inflater = LayoutInflater.from(context);
		this.context = context;
		this.photos = photos;
	}

	@Override
	public int getCount() {
		return photos.size();
	}

	public Object instantiateItem(ViewGroup view, int position) {
		ImageView iv = new ImageView(context);
		iv.setBackgroundResource(R.drawable.default_details);
		iv.setScaleType(ImageView.ScaleType.FIT_XY);
		ImageLoader.getInstance().displayImage(photos.get(position), iv,
				options);
		pageMap.put(position, iv);
		view.addView(iv);
		return iv;
	}
}
