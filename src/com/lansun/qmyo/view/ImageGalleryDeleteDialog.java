package com.lansun.qmyo.view;

import java.util.ArrayList;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.mapcore2d.di;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.util.Handler_Ui;
import com.google.zxing.common.detector.MonochromeRectangleDetector;
import com.lansun.qmyo.adapter.DetailHeaderDeletePagerAdapter;
import com.lansun.qmyo.adapter.DetailHeaderPagerAdapter;
import com.lansun.qmyo.event.entity.CloseEntity;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.R;

public class ImageGalleryDeleteDialog extends DialogFragment {
	private static ArrayList<String> mPhotoList;
	private ViewPager dialog_gallery;
	private DetailHeaderDeletePagerAdapter adapter;
	private TextView store_detail_count;
	private TextView store_detail_num;
	private int currentPosition;
	private RelativeLayout rl_dialog_gallery;
	private int currentPagerNum;

	public ImageGalleryDeleteDialog() {
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

	public static ImageGalleryDeleteDialog newInstance(
			DetailHeaderDeletePagerAdapter adapter, int position) {
		ImageGalleryDeleteDialog fragment = new ImageGalleryDeleteDialog();
		Bundle args = new Bundle();
		args.putSerializable("adapter", adapter);
		args.putInt("currentPosition", position);
		fragment.setArguments(args);
		fragment.setCancelable(true);
		return fragment;
	}
	
	public static ImageGalleryDeleteDialog newInstance(
			DetailHeaderDeletePagerAdapter headPagerAdapter, int position,
			ArrayList<String> files) {
		ImageGalleryDeleteDialog fragment = new ImageGalleryDeleteDialog();
		Bundle args = new Bundle();
		args.putSerializable("adapter", headPagerAdapter);
		args.putInt("currentPosition", position);
	    mPhotoList = files;
		fragment.setArguments(args);
		fragment.setCancelable(true);
		return fragment;
	}


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.adapter = (DetailHeaderDeletePagerAdapter) getArguments()
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
		
		View view = inflater.inflate(R.layout.dialog_gallery_delete, container);
		
		dialog_gallery = (ViewPager) view.findViewById(R.id.dialog_gallery);
		iv_delete_currentphoto = (ImageView) view.findViewById(R.id.iv_delete_currentphoto);
		
		upload_photo_current_count = (TextView)view.findViewById(R.id.upload_photo_current_count);
		upload_photo_total_count =  (TextView)view.findViewById(R.id.upload_photo_total_count);
		
		iv_delete_currentphoto.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//1.删除掉ViewPager中对应的当前页在  数据源中 对应的图片数据
				//mOnDeleteCurrentPhotoListener.deleteCurrentPhotoFromSource(currentPosition);
				
				if(mPhotoList.size()>1){
					mPhotoList.remove(currentPosition);
					upload_photo_total_count.setText(String.valueOf(mPhotoList.size()));
					//2.要求当前展示的数据进行刷新,此adapter是 Viewpager 的 adapter
					adapter.notifyDataSetChanged();
					upload_photo_current_count.setText(String.valueOf(dialog_gallery.getCurrentItem()+1));
					//3.通知发表评论页，更新GridView的展示效果
					mOnNotifyGridViewListener.notifyGridView();
				}else{
					mPhotoList.remove(currentPosition);
					//adapter.notifyDataSetChanged();
					mOnNotifyGridViewListener.notifyGridView(-1);
				}
			}
		});
		
		
//		rl_dialog_gallery = (FrameLayout) view
//				.findViewById(R.id.rl_dialog_gallery);
		
		rl_dialog_gallery = (RelativeLayout) view
				.findViewById(R.id.rl_dialog_gallery);
		
//		store_detail_count = (TextView) view.findViewById(R.id.store_detail_count);
//		store_detail_num = (TextView) view.findViewById(R.id.store_detail_num);
		
		dialog_gallery.setOnPageChangeListener(listener);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;
		
//		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
//				screenWidth, FrameLayout.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				screenWidth, RelativeLayout.LayoutParams.WRAP_CONTENT);
		
		dialog_gallery.setLayoutParams(params);
		dialog_gallery.setAdapter(adapter);
		
		dialog_gallery.setCurrentItem(currentPosition);
		
		upload_photo_current_count.setText((currentPosition + 1) + "");
		upload_photo_total_count.setText(adapter.getCount() + "");
//		store_detail_num.setText((currentPosition + 1) + "");
//		store_detail_count.setText(adapter.getCount() + "");
		
		return view;
	}

	OnPageChangeListener listener = new OnPageChangeListener() {
		
		@Override
		public void onPageSelected(int arg0) {
//			store_detail_num.setText(arg0 + 1 + "");
			upload_photo_current_count.setText(arg0 + 1 + "");
			currentPosition = arg0;
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int position) {
		}
	};
	private ImageView iv_delete_currentphoto;
	private OnDeleteCurrentPhotoListener mOnDeleteCurrentPhotoListener;
	private OnNotifyGridViewListener mOnNotifyGridViewListener;
	private TextView upload_photo_current_count;
	private TextView upload_photo_total_count;
	
	
	public interface OnDeleteCurrentPhotoListener{
		public void deleteCurrentPhotoFromSource(int currentPagerNum);
	}
	
	public void setOnDeleteCurrentPhotoListener(OnDeleteCurrentPhotoListener lis){
		this.mOnDeleteCurrentPhotoListener = lis;
	}
	public interface OnNotifyGridViewListener{
		public void notifyGridView();
		public void notifyGridView(int i);
	}
	
	public void setOnNotifyGridViewListener(OnNotifyGridViewListener lis){
		this.mOnNotifyGridViewListener = lis;
	}

	
}
