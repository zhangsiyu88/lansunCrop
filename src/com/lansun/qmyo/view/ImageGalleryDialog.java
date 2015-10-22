package com.lansun.qmyo.view;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.mapcore2d.di;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.util.Handler_Ui;
import com.lansun.qmyo.adapter.DetailHeaderPagerAdapter;
import com.lansun.qmyo.event.entity.CloseEntity;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.R;

public class ImageGalleryDialog extends DialogFragment {
	private ViewPager dialog_gallery;
	private DetailHeaderPagerAdapter adapter;
	private TextView store_detail_count;
	private TextView store_detail_num;
	private int currentPosition;
	private FrameLayout rl_dialog_gallery;

	public ImageGalleryDialog() {
		super();
	}

	@Override
	public void onStart() {
		super.onStart();
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		getDialog().getWindow().setLayout(dm.widthPixels,
				getDialog().getWindow().getAttributes().height);
	}

	public static ImageGalleryDialog newInstance(
			DetailHeaderPagerAdapter adapter, int position) {
		ImageGalleryDialog fragment = new ImageGalleryDialog();
		Bundle args = new Bundle();
		args.putSerializable("adapter", adapter);
		args.putInt("currentPosition", position);
		fragment.setArguments(args);
		fragment.setCancelable(true);
		return fragment;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.adapter = (DetailHeaderPagerAdapter) getArguments()
				.getSerializable("adapter");
		this.currentPosition = getArguments().getInt("currentPosition");

		EventBus.getDefault().register(this);
	}

	@Override
	public void onPause() {
		EventBus.getDefault().unregister(this);
		super.onPause();
	}

	public void onEventMainThread(CloseEntity event) {
		if (event.isClose()) {
			dismiss();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		getDialog().getWindow().setBackgroundDrawable(
				new ColorDrawable(Color.TRANSPARENT));
		getDialog().setCanceledOnTouchOutside(true);
		View view = inflater.inflate(R.layout.dialog_gallery, container);
		dialog_gallery = (ViewPager) view.findViewById(R.id.dialog_gallery);
		rl_dialog_gallery = (FrameLayout) view
				.findViewById(R.id.rl_dialog_gallery);
		store_detail_count = (TextView) view
				.findViewById(R.id.store_detail_count);
		store_detail_num = (TextView) view.findViewById(R.id.store_detail_num);
		dialog_gallery.setOnPageChangeListener(listener);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				screenWidth, FrameLayout.LayoutParams.WRAP_CONTENT);
		dialog_gallery.setLayoutParams(params);
		dialog_gallery.setAdapter(adapter);
		store_detail_num.setText((currentPosition + 1) + "");
		dialog_gallery.setCurrentItem(currentPosition);
		store_detail_count.setText(adapter.getCount() + "");
		return view;
	}

	OnPageChangeListener listener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int arg0) {
			store_detail_num.setText(arg0 + 1 + "");
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int position) {
		}
	};
}
