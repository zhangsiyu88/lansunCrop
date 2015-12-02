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



public class SwipeListMineActivityAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	HashSet<Integer> mRemoved = new HashSet<Integer>();
	HashSet<SwipeLayout> mUnClosedLayouts = new HashSet<SwipeLayout>();
	private ArrayList<HashMap<String, Object>> mList;
	private int deletePosition;
	
	  private DisplayImageOptions options = new DisplayImageOptions.Builder()
	    .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
	    .displayer(new FadeInBitmapDisplayer(300)).build();
	private boolean isSlide = true;

	
	  
	  public SwipeListMineActivityAdapter(Context mContext) {
		super();
		this.mContext = mContext;
		mInflater = LayoutInflater.from(mContext);
	}

	public SwipeListMineActivityAdapter(Activity activity,
			ArrayList<HashMap<String, Object>> dataList) {
		super();
		this.mContext = activity;
		this.mList = dataList;
		mInflater = LayoutInflater.from(activity);
	}

	public SwipeListMineActivityAdapter(Activity activity,
			ArrayList<HashMap<String, Object>> dataList, boolean b) {
		super();
		this.mContext = activity;
		this.mList = dataList;
		mInflater = LayoutInflater.from(activity);
		this.isSlide  = b;
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

//	public static final int[] HEAD_IDS = new int[]{
//		/*R.drawable.head_1,
//		R.drawable.head_2,
//		R.drawable.head_3*/
//	};
	
	@SuppressWarnings("unchecked")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		mPosition = position;
		ViewHolder mHolder;
		if (convertView != null) {
			mHolder = (ViewHolder) convertView.getTag();
		}else {
			convertView = (SwipeLayout) mInflater.inflate(R.layout.activity_search_item_usual_test, null);
			mHolder = ViewHolder.fromValues(convertView);
			convertView.setTag(mHolder);
		}
		SwipeLayout view = (SwipeLayout) convertView;
		view.close(false, false);
		view.getFrontView().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				/*Utils.showToast(mContext, "item click: " + position);*/
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
				
			}
		});

		if(isSlide){
			view.setSwipeListener(mSwipeListener);
		}else{
			/*view.setSwipeListener(null);*/
			/*view.close();*/
			view.setGestureValid(true);
		}
		
		
		//Adapter模式就是在适配器一段拿到了数据源，规则：谁有数据谁进行对应的数据初始化展示操作
		if (position+1 == mList.size()) {//当获取到的数据列表的size()正好为view的位置position+1时,那么最后一个mHolder中line的可见性设置为GONE掉
			mHolder.line.setVisibility(View.GONE);
		}else {
			mHolder.line.setVisibility(View.VISIBLE);
		}
		
		if (mList.get(position).get("tv_search_activity_name") != null) {
			/*String name = data.get("tv_search_activity_name").toString();
			mHolder.tv_search_activity_name.setText(name);*/
			/*
			 * 暂时将上端数据屏掉，为了调换活动名和描述内容
			 */
			String name = mList.get(position).get("tv_search_activity_desc").toString();
			mHolder.tv_search_activity_name.setText(name);
			
		}
		if (mList.get(position).get("tv_search_activity_distance") != null) {
			String distance = mList.get(position).get("tv_search_activity_distance")
					.toString();
			mHolder.tv_search_activity_distance.setText(distance);
		}
		if (mList.get(position).get("tv_search_activity_desc") != null) {
			/*String desc = data.get("tv_search_activity_desc").toString();
			mHolder.tv_search_activity_desc.setText(desc);*/
			/*
			 * 暂时将上端数据屏掉，为了调换活动名和描述内容
			 */
			String desc = mList.get(position).get("tv_search_activity_name").toString();
			mHolder.tv_search_activity_desc.setText(desc);
		}
		if (mList.get(position).get("iv_search_activity_head") != null) {
			String head = mList.get(position).get("iv_search_activity_head").toString();
			/*download(mHolder.iv_search_activity_head, null);*/
			mHolder.iv_search_activity_head.setImageResource(R.drawable.default_list);
			download(mHolder.iv_search_activity_head, head);//传过来的图片
		}
		if (mList.get(position).get("tv_search_tag") != null) {
			String tag = mList.get(position).get("tv_search_tag").toString();
			mHolder.tv_search_tag.setText(tag);//标签，例如某银行卡或迈界
		}
		
		/*iconArray = new ArrayList<String>();*/
		
		//滑到下一页时，便可复用iconArray
		ArrayList<String> iconArray = (ArrayList<String>) mList.get(position).get("icons");
		if(iconArray!= null){
			//LogUtils.toDebugLog("icon", "icon去哪儿了？     data.get(position)"+ iconArray.toString());
		}

//		探测首页的标签消失是否和这里的标签移除有关
		mHolder.iv_search_activity_discount.setVisibility(View.GONE);
		mHolder.iv_search_activity_new.setVisibility(View.GONE);
		mHolder.iv_search_activity_point.setVisibility(View.GONE);
		mHolder.iv_search_activity_staging.setVisibility(View.GONE);
		mHolder.iv_search_activity_coupon.setVisibility(View.GONE);
		
		
		 
		//因为ListViewItem的复用造成了数据的错误显示，所以在这种情况下需要将前面复用的部件全部清除掉
		iconArray = (ArrayList<String>) mList.get(position).get("icons");
		if (iconArray != null) {
			for (String icon : iconArray) {
				//LogUtils.toDebugLog("icon", "icon去哪儿了？   "+ icon);

				if ("discount".equals(icon)) {
					mHolder.iv_search_activity_discount.setVisibility(View.VISIBLE);
				} else if ("new".equals(icon)) {
					mHolder.iv_search_activity_new.setVisibility(View.VISIBLE);
				} else if ("point".equals(icon)) {
					mHolder.iv_search_activity_point.setVisibility(View.VISIBLE);
				} else if ("staging".equals(icon)) {
					mHolder.iv_search_activity_staging.setVisibility(View.VISIBLE);
				} else if ("coupon".equals(icon)) {
					mHolder.iv_search_activity_coupon.setVisibility(View.VISIBLE);
				}
			}
		}
	
		
		/**
		 * 点击按钮,
		 */
		mHolder.mButtonCall.setTag(position);
		mHolder.mButtonCall.setOnClickListener(onActionClick);

		mHolder.mButtonDel.setTag(position);
		mHolder.mButtonDel.setOnClickListener(onActionClick);
		
		
		
		
//		TextView mUnreadView = mHolder.mReminder;
//		boolean visiable = !mRemoved.contains(position);
//		mUnreadView.setVisibility(visiable ? View.VISIBLE : View.GONE);
//
//		if (visiable) {
//			mUnreadView.setText(String.valueOf(position));
//			mUnreadView.setTag(position);
			
			/*
			 * 初始化监听者，方便对圆形按钮进行GooViewListener
			 * 之随意在这一步就进行初始化，谁拿到数据谁进行初始化任务，另外从时效原则上来说也是必须的
			 */
		
		
//          																					  --by Yeun 11.23			
//			GooViewListener mGooListener = new GooViewListener(mContext, mUnreadView) {
//				@Override
//				public void onDisappear(PointF mDragCenter) {
//					super.onDisappear(mDragCenter);
//
//					mRemoved.add(position);
//					notifyDataSetChanged();
//					Utils.showToast(mContext,"Cheers! We have get rid of it!");
//				}
//				@Override
//				public void onReset(boolean isOutOfRange) {
//					super.onReset(isOutOfRange);
//
//					notifyDataSetChanged();
//					Utils.showToast(mContext,isOutOfRange ? "Are you regret?" : "Try again!");
//				}
//			};
// 			为所有未读的条目设置触摸监听,为了使RedCircleButton被触摸时进行属于自己的动画表示
//			
//			mUnreadView.setOnTouchListener(mGooListener);
//		}

		return view;
	}

	OnClickListener onActionClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Integer p = (Integer) v.getTag();//这个View对应的tag是绑定的position的值
			int id = v.getId();
			if (id == R.id.bt_call) {
				closeAllLayout();
				//暂时不提供实际操作的方法
				
			} else if (id == R.id.bt_delete) {
				closeAllLayout();
				//Utils.showToast(mContext, "删除该活动");
				
				deletePosition = p;
				HttpUtils httpUtils = new HttpUtils();
				RequestCallBack<String> requestCallBack = new RequestCallBack<String>() {

					@Override
					public void onFailure(HttpException arg0, String arg1) {
					}

					@Override
					public void onSuccess(ResponseInfo<String> arg0) {
						if ("true".equals(arg0.result.toString())) {
							
								mList.remove(deletePosition);
								
								CustomToast.show(mContext, mContext.getString(R.string.tip),
										mContext.getString(R.string.delete_success));
								SwipeListMineActivityAdapter.this.notifyDataSetChanged();
							} else {
								CustomToast.show(mContext, mContext.getString(R.string.tip),
										mContext.getString(R.string.delete_faild));
							}
					}

					
				};
				RequestParams requestParams = new RequestParams();
				requestParams.addHeader("Authorization", "Bearer" + App.app.getData("access_token"));
				
				//TODO  注意下面拼接的url中的接口域名记得替换
				httpUtils.send(HttpMethod.DELETE, 
						"http://appapi.qmyo.com/activity/"+mList
						.get(p).get("activityId").toString()+"?shop_id="+mList.get(p).get("shopId").toString(),
						requestParams,requestCallBack );
			}
		}
	};
	
	
	SwipeListener mSwipeListener = new SwipeListener() {
		@Override
		public void onOpen(SwipeLayout swipeLayout) {
//			Utils.showToast(mContext, "onOpen");
			mUnClosedLayouts.add(swipeLayout);
			swipeLayout.close();
		}

		@Override
		public void onClose(SwipeLayout swipeLayout) {
//			Utils.showToast(mContext, "onClose");
			mUnClosedLayouts.remove(swipeLayout);
		}

		@Override
		public void onStartClose(SwipeLayout swipeLayout) {
//			Utils.showToast(mContext, "onStartClose");
			
		}

		@Override
		public void onStartOpen(SwipeLayout swipeLayout) {
//			Utils.showToast(mContext, "onStartOpen");
			closeAllLayout();
			mUnClosedLayouts.add(swipeLayout);
			swipeLayout.close();
		}

	};
	private int mPosition;
	
	public int getUnClosedCount(){
		return mUnClosedLayouts.size();
	}
	
	public void closeAllLayout() {
		if(mUnClosedLayouts.size() == 0)
			return;
		
		for (SwipeLayout l : mUnClosedLayouts) {
			l.close(true, false);
		}
		mUnClosedLayouts.clear();
	}
	
	static class ViewHolder {

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
}