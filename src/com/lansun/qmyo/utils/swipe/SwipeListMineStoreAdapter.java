package com.lansun.qmyo.utils.swipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.pc.ioc.adapter.LazyAdapter;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectView;
import com.lansun.qmyo.R;
import com.lansun.qmyo.adapter.StoreAdapter.ViewHolder;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.ActivityDetailFragment;
import com.lansun.qmyo.fragment.StoreDetailFragment;
import com.lansun.qmyo.utils.GlobalValue;
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

/**
 * 
 * @author bhxx
 * 
 */
public class SwipeListMineStoreAdapter extends BaseAdapter {
	
	
	private Context mContext;
	private ArrayList<HashMap<String, String>> mDataList;
	private DisplayImageOptions options = new DisplayImageOptions.Builder()
	    .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
	    .displayer(new FadeInBitmapDisplayer(300)).build();
	private int deletePosition;
	private LayoutInflater mInflater;
	HashSet<Integer> mRemoved = new HashSet<Integer>();
	HashSet<SwipeLayout> mUnClosedLayouts = new HashSet<SwipeLayout>();
	private boolean isSlide = true;
	

	public SwipeListMineStoreAdapter(Context ctx , ArrayList<HashMap<String, String>> dataList){
		
		this.mContext = ctx;
		this.mDataList = dataList;
		this.mInflater = LayoutInflater.from(mContext);
		
	}
	
	public SwipeListMineStoreAdapter(Context ctx,
			ArrayList<HashMap<String, String>> dataList, boolean b) {
		this.isSlide = b;
		this.mContext = ctx;
		this.mDataList = dataList;
		this.mInflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		return mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		data = mDataList.get(position);
		mPosition = position;
		ViewHolder viewHold;
		if (convertView != null) {
			viewHold = (ViewHolder) convertView.getTag();
		}else {
			convertView = (SwipeLayout) mInflater.inflate(R.layout.activity_store_item_swipe, null);
			viewHold = ViewHolder.fromValues(convertView);
			convertView.setTag(viewHold);
		}
		SwipeLayout view = (SwipeLayout) convertView;
		view.close(false, false);
		view.getFrontView().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				StoreDetailFragment fragment = new StoreDetailFragment();
				Bundle args = new Bundle();
				
				/* 居然连类型都得分开识别，这里的shopId是int类型
				 * args.putInt("shopId",Integer.parseInt(dataList.get(position).get("shop_id")));*/
				
				/*args.putString("shopId",mDataList.get(mPosition).get("shop_id"));*/
				args.putString("shopId",mDataList.get(position).get("shop_id"));
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
			/*view.setSwipeListener(mSwipeListener);*/
			view.setGestureValid(true);
			//view.setSwipeListener(mSwipeListener);
		}
		
		
		if (position + 1 == mDataList.size()) {
			viewHold.line.setVisibility(View.GONE);
		} else {
			viewHold.line.setVisibility(View.VISIBLE);
		}
		
		
		String name = data.get("tv_store_item_name");
		String num = data.get("tv_store_item_num");
		String distance = data.get("tv_store_item_distance");
		
		String rb = data.get("rb_store_item");
		Integer rbInt = Integer.valueOf(rb);
		rbInt = rbInt*2;
		
		viewHold.tv_store_item_name.setText(name);
		viewHold.tv_store_item_num.setText(num);
		viewHold.tv_store_item_distance.setText(distance);
		viewHold.rb_store_item.setProgress(rbInt);
		
		/**
		 * 点击按钮,
		 */
		viewHold.mButtonCall.setTag(position);
		viewHold.mButtonCall.setOnClickListener(onActionClick);

		viewHold.mButtonDel.setTag(position);
		viewHold.mButtonDel.setOnClickListener(onActionClick);
		
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
				
				deletePosition = p;
				//活动删除代码：
				HttpUtils httpUtils = new HttpUtils();
				RequestCallBack<String> requestCallBack = new RequestCallBack<String>() {
	
					@Override
					public void onFailure(HttpException arg0, String arg1) {
						
					}
	
					@Override
					public void onSuccess(ResponseInfo<String> arg0) {
						if ("true".equals(arg0.result.toString())) {
								mDataList.remove(deletePosition);
								notifyDataSetChanged();
								if(mDataList.size()==0){//删除后数据列表为零
									
//									  v.rl_no_postdelay_store.setVisibility(View.VISIBLE);
									
								}
								/*CustomToast.show(mContext, mContext.getString(R.string.tip),
										mContext.getString(R.string.delete_success));*/
								Utils.showToast(mContext, "删除成功");
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
						GlobalValue.URL_QX_GZ_SHOP+ mDataList.get(deletePosition).get("shop_id"),
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
	private HashMap<String, String> data;
	
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
	
	
	
	public static class ViewHolder {
		
		private RecyclingImageView iv_store_iem_gz;//, iv_shop_lv
		private TextView tv_store_item_name,   tv_store_item_num,  tv_store_item_distance;
		private RatingBar rb_store_item;
		private View line, rl_store_item_gz;
		
		public Button mButtonCall;
		public ImageButton mButtonDel;

		private ViewHolder(
				Button mButtonCall,
				ImageButton mButtonDel, 
				RecyclingImageView iv_store_iem_gz,
				
				RatingBar rb_store_item,
				TextView tv_store_item_name, TextView tv_store_item_num,
				TextView tv_store_item_distance,
				View line){//RecyclingImageView iv_shop_lv, 
			
					super();
					this.mButtonCall = mButtonCall;
					this.mButtonDel = mButtonDel;
					this.iv_store_iem_gz = iv_store_iem_gz;
					//this.iv_shop_lv = iv_shop_lv;
					this.rb_store_item = rb_store_item;
					this.tv_store_item_name = tv_store_item_name;
					this.tv_store_item_num = tv_store_item_num;
					this.tv_store_item_distance = tv_store_item_distance;
					this.line = line;
			}
		public static ViewHolder fromValues(View view) {
			return new ViewHolder(
				(Button) view.findViewById(R.id.bt_call),
				(ImageButton) view.findViewById(R.id.bt_delete),
				
				(RecyclingImageView) view.findViewById(R.id.iv_store_iem_gz),
				//(RecyclingImageView) view.findViewById(R.id.iv_shop_lv),
				(RatingBar) view.findViewById(R.id.rb_store_item),
				(TextView) view.findViewById(R.id.tv_store_item_name),
				(TextView) view.findViewById(R.id.tv_store_item_num),
				(TextView) view.findViewById(R.id.tv_store_item_distance),
				(View)view.findViewById(R.id.line));
		}
	}
	
	
	
	
	
	public void download(ImageView view, String url) {
		ImageLoader.getInstance().displayImage(url, view, this.options);
	}

}
