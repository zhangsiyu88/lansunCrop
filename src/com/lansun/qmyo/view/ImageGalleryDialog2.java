package com.lansun.qmyo.view;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.invoker.InjectViews.Views;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.lansun.qmyo.adapter.DetailHeaderPagerAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.event.entity.DeleteEntity;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.R;

public class ImageGalleryDialog2 extends DialogFragment {
	private ViewPager dialog_gallery;
	private boolean hasTop;
	private TextView store_detail2_count;
	private TextView store_detail2_num;
	private int currentPosition;
	@InjectAll
	private Views v;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private ImageView iv_activity_back, tv_dialog_delete;
	}

	public ImageGalleryDialog2() {
		super();
	}

	public static ImageGalleryDialog2 newInstance(
			DetailHeaderPagerAdapter adapter, int position) {
		ImageGalleryDialog2 fragment = new ImageGalleryDialog2();
		Bundle args = new Bundle();
		args.putSerializable("adapter", adapter);
		args.putInt("currentPosition", position);
		fragment.setArguments(args);
		fragment.setCancelable(true);
		return fragment;
	}

	@Override
	public void onStart() {
		super.onStart();
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		getDialog().getWindow().setLayout(dm.widthPixels,
				getDialog().getWindow().getAttributes().height);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NORMAL, R.style.dialog);
		this.headAdapter = (DetailHeaderPagerAdapter) getArguments()
				.getSerializable("adapter");
		this.currentPosition = getArguments().getInt("currentPosition");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().setCanceledOnTouchOutside(true);
		View view = inflater.inflate(R.layout.dialog_gallery2, container);
		dialog_gallery = (ViewPager) view.findViewById(R.id.dialog_gallery);
		store_detail2_count = (TextView) view
				.findViewById(R.id.store_detail2_count);
		store_detail2_num = (TextView) view
				.findViewById(R.id.store_detail2_num);

		dialog_gallery.setOnPageChangeListener(listener);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				screenWidth, RelativeLayout.LayoutParams.WRAP_CONTENT);
		dialog_gallery.setLayoutParams(params);
		dialog_gallery.setAdapter(headAdapter);
		store_detail2_num.setText((currentPosition + 1) + "");
		dialog_gallery.setCurrentItem(currentPosition);
		store_detail2_count.setText(headAdapter.getCount() + "");
		Handler_Inject.injectFragment(this, view);
		return view;
	}

	OnPageChangeListener listener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int arg0) {
			store_detail2_num.setText(arg0 + 1 + "");
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int position) {
		}

	};
	private static DetailHeaderPagerAdapter headAdapter;

	private void click(View view) {
		switch (view.getId()) {
		case R.id.iv_activity_back:
			dismiss();
			break;
		case R.id.tv_dialog_delete:
			DialogUtil.createTipAlertDialog(getActivity(), R.string.delete,
					new TipAlertDialogCallBack() {

						@Override
						public void onPositiveButtonClick(
								DialogInterface dialog, int which) {
							dialog.dismiss();
							DeleteEntity event = new DeleteEntity();
							int currentItem = dialog_gallery.getCurrentItem();
							event.setPosition(currentItem);
							dialog_gallery.removeViewAt(currentItem);
							if (currentItem <= dialog_gallery.getChildCount()) {
								dialog_gallery.setCurrentItem(currentItem);
							} else {
								dialog_gallery.setCurrentItem(currentItem - 1);
							}
							headAdapter.notifyDataSetChanged();
							EventBus.getDefault().post(event);
							store_detail2_count.setText(headAdapter.getCount()
									+ "");
						}

						@Override
						public void onNegativeButtonClick(
								DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			break;
		}
	}

}
