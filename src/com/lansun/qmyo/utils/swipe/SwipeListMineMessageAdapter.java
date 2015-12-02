package com.lansun.qmyo.utils.swipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
import com.android.pc.util.Handler_Time;
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
public class SwipeListMineMessageAdapter extends BaseAdapter {
	
	
	private Context mContext;
	private ArrayList<HashMap<String, String>> mDataList;
	private DisplayImageOptions options = new DisplayImageOptions.Builder()
	    .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
	    .displayer(new FadeInBitmapDisplayer(300)).build();
	
	private int deletePosition;
	private LayoutInflater mInflater;
	HashSet<Integer> mRemoved = new HashSet<Integer>();
	HashSet<SwipeLayout> mUnClosedLayouts = new HashSet<SwipeLayout>();
	private int mPosition;
	public ItemClickCallback itemClickcallback ;

	public SwipeListMineMessageAdapter(Context ctx , ArrayList<HashMap<String, String>> dataList){
		
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
		
		HashMap<String, String> data = mDataList.get(position);
		mPosition = position;
		
		ViewHolder viewHold;
		if (convertView != null) {
			viewHold = (ViewHolder) convertView.getTag();
		}else {
			convertView = mInflater.inflate(R.layout.activity_message_item, null);
			viewHold = ViewHolder.fromValues(convertView);
			convertView.setTag(viewHold);
		}
		
		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				itemClickcallback.listItemClick(position);
			}
		});
		
		
		viewHold.tv_search_activity_time.setText(data.get("tv_search_activity_time"));
		viewHold.tv_message_item_desc.setText(data.get("tv_message_item_desc"));
		viewHold.tv_message_item_name.setText(data.get("tv_message_item_name"));
		
		
		if (Integer.parseInt(data.get("tv_message_item_count")) > 0) {
			viewHold.tv_message_item_count.setVisibility(View.GONE);
			viewHold.tv_message_item_count.setText("");
		}
		
		if (position + 1 == mDataList.size()) {
			viewHold.line.setVisibility(View.GONE);
		} else {
			viewHold.line.setVisibility(View.VISIBLE);
		}

		
		
		
		/**
		 * 点击按钮,
		 */
//		viewHold.mButtonCall.setTag(position);
//		viewHold.mButtonCall.setOnClickListener(onActionClick);
//
//		viewHold.mButtonDel.setTag(position);
//		viewHold.mButtonDel.setOnClickListener(onActionClick);
		
		/*return View;*/
		return convertView;
	}
	
	
//	注解掉下面的三个方法，在非侧滑条目中暂时使用不到
//	
//	OnClickListener onActionClick = new OnClickListener() {
//
//		@Override
//		public void onClick(View v) {
//			Integer p = (Integer) v.getTag();//这个View对应的tag是绑定的position的值
//			int id = v.getId();
//			if (id == R.id.bt_call) {
//				closeAllLayout();
//				//暂时不提供实际操作的方法
//				
//			} else if (id == R.id.bt_delete) {
//				closeAllLayout();
//				//Utils.showToast(mContext, "删除该活动");
//				deletePosition = p;
//				//活动删除代码：
//				HttpUtils httpUtils = new HttpUtils();
//				RequestCallBack<String> requestCallBack = new RequestCallBack<String>() {
//	
//					@Override
//					public void onFailure(HttpException arg0, String arg1) {
//						
//					}
//	
//					@Override
//					public void onSuccess(ResponseInfo<String> arg0) {
//						if ("true".equals(arg0.result.toString())) {
//								mDataList.remove(deletePosition);
//								notifyDataSetChanged();
//								if(mDataList.size()==0){//删除后数据列表为零
//									
//									
////									  v.rl_no_postdelay_store.setVisibility(View.VISIBLE);
//									
//									
//									
//								}
//								CustomToast.show(mContext, mContext.getString(R.string.tip),
//										mContext.getString(R.string.delete_success));
//							} else {
//								CustomToast.show(mContext, mContext.getString(R.string.tip),
//										mContext.getString(R.string.delete_faild));
//							}
//					}
//	
//					
//				};
//				RequestParams requestParams = new RequestParams();
//				requestParams.addHeader("Authorization", "Bearer" + App.app.getData("access_token"));
//				
//				//TODO  注意下面拼接的url中的接口域名记得替换
//				httpUtils.send(HttpMethod.DELETE, 
//						GlobalValue.URL_QX_GZ_SHOP+ mDataList.get(deletePosition).get("shop_id"),
//						requestParams,requestCallBack );
//			}
//		}
//	};
//	
//	
//	SwipeListener mSwipeListener = new SwipeListener() {
//		@Override
//		public void onOpen(SwipeLayout swipeLayout) {
////			Utils.showToast(mContext, "onOpen");
//			mUnClosedLayouts.add(swipeLayout);
//		}
//
//		@Override
//		public void onClose(SwipeLayout swipeLayout) {
////			Utils.showToast(mContext, "onClose");
//			mUnClosedLayouts.remove(swipeLayout);
//		}
//
//		@Override
//		public void onStartClose(SwipeLayout swipeLayout) {
////			Utils.showToast(mContext, "onStartClose");
//		}
//
//		@Override
//		public void onStartOpen(SwipeLayout swipeLayout) {
////			Utils.showToast(mContext, "onStartOpen");
//			closeAllLayout();
//			mUnClosedLayouts.add(swipeLayout);
//		}
//
//	};
//	
//	
//	private int mPosition;
//	private HashMap<String, String> data;
//	
//	public int getUnClosedCount(){
//		return mUnClosedLayouts.size();
//	}
//	
//	public void closeAllLayout() {
//		if(mUnClosedLayouts.size() == 0)
//			return;
//		
//		for (SwipeLayout l : mUnClosedLayouts) {
//			l.close(true, false);
//		}
//		mUnClosedLayouts.clear();
//	}
	
	
	
	public static class ViewHolder {

		private TextView tv_message_item_desc, tv_message_item_name,
				tv_search_activity_time, tv_message_item_count;
		private View line;
		
		
		private ViewHolder(
				TextView tv_message_item_desc,TextView tv_message_item_name,
				TextView tv_search_activity_time,TextView tv_message_item_count,View line){
					super();
					this.tv_message_item_desc = tv_message_item_desc;
					this.tv_message_item_name = tv_message_item_name;
					this.tv_search_activity_time = tv_search_activity_time;
					this.tv_message_item_count = tv_message_item_count;
					this.line = line;
			}
		
		public static ViewHolder fromValues(View view) {//找到view中的各个控件对象
			return new ViewHolder(
			    (TextView) view.findViewById(R.id.tv_message_item_desc),
			    (TextView) view.findViewById(R.id.tv_message_item_name),
			    (TextView) view.findViewById(R.id.tv_search_activity_time),
			    (TextView) view.findViewById(R.id.tv_message_item_count),
			    (View) view.findViewById(R.id.line));
		}
	}
	
	public void download(ImageView view, String url) {
		ImageLoader.getInstance().displayImage(url, view, this.options);
	}

	public interface ItemClickCallback{
		void listItemClick(int position);
	}
	
	/*
	 * 将ItemClickCallback对象(即实现接口的对象去完成)带入到Adapter的类中，供对象调用
	 */
	public void setItemClickCallback(ItemClickCallback itemClickcallback){
		this.itemClickcallback = itemClickcallback;
	}
}
