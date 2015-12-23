package com.lansun.qmyo.utils.swipe;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.internet.InternetConfig;
import com.lansun.qmyo.R;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.ActivityDetailFragment;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.utils.swipe.SwipeLayout.SwipeListener;
import com.lansun.qmyo.utils.swipe.SwipeListMineActivityAdapter.ViewHolder;
import com.lansun.qmyo.view.CustomToast;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;



public class EightPartActivityAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	HashSet<Integer> mRemoved = new HashSet<Integer>();
	HashSet<SwipeLayout> mUnClosedLayouts = new HashSet<SwipeLayout>();
	private ArrayList<HashMap<String, Object>> mList;
	private int deletePosition;
	
	  private DisplayImageOptions options = new DisplayImageOptions.Builder()
	    .cacheInMemory(true)
	    .cacheOnDisk(true)
	    .considerExifParams(true)
	    .build();//.displayer(new FadeInBitmapDisplayer(300))
	  
	  public EightPartActivityAdapter(Context mContext) {
		super();
		this.mContext = mContext;
		mInflater = LayoutInflater.from(mContext);
	}

	public EightPartActivityAdapter(Activity activity,
			ArrayList<HashMap<String, Object>> dataList) {
		super();
		this.mContext = activity;
		this.mList = dataList;
		mInflater = LayoutInflater.from(activity);
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}


	
	@SuppressWarnings("unchecked")
	@Override
	public View getView(final int position,  View convertView, ViewGroup parent) {
		mPosition = position;
		HashMap<String, Object> data = mList.get(position);
		
		ViewHolder viewHold;
		if (convertView != null) {
			viewHold = (ViewHolder) convertView.getTag();
		}else {
			convertView = mInflater.inflate(R.layout.activity_search_item, null);
			viewHold = ViewHolder.fromValues(convertView);
			convertView.setTag(viewHold);
		}
		
		
		/**
		 * convertView 整体设置点击事件，会造成跳转逻辑乱套，Item整体的跳转是需要在Adapter外部使用ListView的OnItemClickListener去完成
		 */
		/*convertView.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			LogUtils.toDebugLog("click", "点击事件");
			
			//1,解决先按筛选栏，后按Item的问题
			if(mClickBackPress!=null){
				mClickBackPress.closeExpandTabView();
			}
			
			ActivityDetailFragment fragment = new ActivityDetailFragment();
			Bundle args = new Bundle();
			args.putString("activityId",
					mList.get(position).get("activityId").toString());
			args.putString("shopId", mList.get(position).get("shopId")
					.toString());
			fragment.setArguments(args);
			FragmentEntity event = new FragmentEntity();
			event.setFragment(fragment);
			EventBus.getDefault().post(event);
			
			//2,解决先按Item后按筛选栏的问题
			if(mClickBackPress!=null){
				mClickBackPress.recoverExpandTabViewTouch();
			}
			
		}
	});*/

		if (position+1 == mList.size()) {//当获取到的数据列表的size()正好为view的位置position+1时,那么最后一个viewHold中line的可见性设置为GONE掉
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
		return convertView;
	}

	private int mPosition;
	private IClickBackPress mClickBackPress;
	public static class ViewHolder {

		public Button mButtonCall;
		public ImageButton mButtonDel;
		private RecyclingImageView iv_search_activity_head,
				iv_search_activity_point, iv_search_activity_coupon,
				iv_search_activity_staging, iv_search_activity_new,
				iv_search_activity_discount;
		private TextView tv_search_activity_name, tv_search_activity_distance,
				tv_search_activity_desc, tv_search_tag;
		private View line;
		
		private ViewHolder(
				Button mButtonCall,
				ImageButton mButtonDel, 
				RecyclingImageView iv_search_activity_head,
				RecyclingImageView iv_search_activity_point, RecyclingImageView iv_search_activity_coupon,
				RecyclingImageView iv_search_activity_staging, RecyclingImageView iv_search_activity_new,
				RecyclingImageView iv_search_activity_discount,
				TextView tv_search_activity_name, TextView tv_search_activity_distance,
				TextView tv_search_activity_desc, TextView tv_search_tag,
				View line){
					super();
					this.mButtonCall = mButtonCall;
					this.mButtonDel = mButtonDel;
					this.iv_search_activity_head = iv_search_activity_head;
					this.iv_search_activity_point = iv_search_activity_point;
					this.iv_search_activity_coupon = iv_search_activity_coupon;
					this.iv_search_activity_staging = iv_search_activity_staging;
					this.iv_search_activity_new = iv_search_activity_new;
					this.iv_search_activity_discount = iv_search_activity_discount;
					this.tv_search_activity_name = tv_search_activity_name;
					this.tv_search_activity_distance = tv_search_activity_distance;
					this.tv_search_activity_desc = tv_search_activity_desc;
					this.tv_search_tag = tv_search_tag;
					this.line = line;
				
			}
			public static ViewHolder fromValues(View view) {
				return new ViewHolder(
					(Button) view.findViewById(R.id.bt_call),
					(ImageButton) view.findViewById(R.id.bt_delete),
					(RecyclingImageView) view.findViewById(R.id.iv_search_activity_head),
					(RecyclingImageView) view.findViewById(R.id.iv_search_activity_point),
					(RecyclingImageView) view.findViewById(R.id.iv_search_activity_coupon),
					(RecyclingImageView) view.findViewById(R.id.iv_search_activity_staging),
					(RecyclingImageView) view.findViewById(R.id.iv_search_activity_new),
					(RecyclingImageView) view.findViewById(R.id.iv_search_activity_discount),
					(TextView) view.findViewById(R.id.tv_search_activity_name),
					(TextView) view.findViewById(R.id.tv_search_activity_distance),
					(TextView) view.findViewById(R.id.tv_search_activity_desc),
					(TextView) view.findViewById(R.id.tv_search_tag),
					(View) view.findViewById(R.id.line));
			}
	}
	
	public void download(ImageView view, String url) {
		ImageLoader.getInstance().displayImage(url, view, this.options);
	}
	
	/**
	 * 设计click()事件中的回调函数，共ActivityFragment进行使用
	 * @author Yeun.Zhang
	 */
	public interface IClickBackPress{
		void closeExpandTabView ();
		void recoverExpandTabViewTouch();
	}
	public void setIClickBackPress(IClickBackPress clickBackPress){
		this.mClickBackPress = clickBackPress;
	}
}