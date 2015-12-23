package com.lansun.qmyo.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import cn.jpush.android.util.ac;

import com.android.pc.ioc.image.RecyclingImageView;
import com.lansun.qmyo.view.MyGridView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.lansun.qmyo.R;

/**
 * 迈友评论列表（没有回复的）
 * 
 * @author bhxx
 * 
 */
public class MaiCommentGVAdapter extends CommonAdapter {

	private ArrayList<String> photos;
	private DisplayImageOptions options;

	public MaiCommentGVAdapter(Context activity, ArrayList<String> photos) {
		this.inflater = LayoutInflater.from(activity);
		this.activity = activity;
		this.photos = photos;
		options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true)
				.displayer(new FadeInBitmapDisplayer(300)).build();
	}

	@Override
	public int getCount() {
		if (photos == null) {
			return 0;
		}
		if (photos.size() >= 4) {
			return 4;
		}
		return photos.size();
	}

	@Override
	public View view(int position, View convertView, ViewGroup parent) {
		convertView = inflater.inflate(R.layout.comments_gv_item, null);
		RecyclingImageView iv_upload_photo = (RecyclingImageView) convertView
				.findViewById(R.id.iv_comments_photo);
		iv_upload_photo.setImageResource(R.drawable.default_comments);
		ImageLoader.getInstance().displayImage(
				photos.get(position) + "?imageView2/1/w/192/h/192/format/jpg",
				iv_upload_photo, options);
		iv_upload_photo.setScaleType(ScaleType.FIT_XY);
		return convertView;
	}
}
