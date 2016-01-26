package com.lansun.qmyo.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectPullRefresh;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.PullToRefreshManager;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.android.pc.util.Handler_Time;
import com.lansun.qmyo.adapter.MessageAdapter;
import com.lansun.qmyo.domain.MessageData;
import com.lansun.qmyo.domain.MessageList;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.utils.swipe.SwipeListMineMessageAdapter;
import com.lansun.qmyo.utils.swipe.SwipeListMineMessageAdapter.ItemClickCallback;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ListViewSwipeGesture;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.view.TestMyListView;
import com.lansun.qmyo.view.TestMyListView.OnRefreshListener;
import com.lansun.qmyo.R;

public class MessageCenterFragment extends BaseFragment implements ItemClickCallback {
	private MessageList list;
	
	/*private MessageAdapter adapter;*/
	private SwipeListMineMessageAdapter adapter;
	private int times = 0;
	
	private ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
	private int index;
	@InjectAll
	Views v;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView tv_message_activity, tv_message_maijie;
		private View no_data;
		
	}

//	@InjectView(down = true, pull = true)
//	@InjectView(pull = true)
//	private MyListView lv_message_list;
	
	@InjectView
	private TestMyListView lv_message_list;	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater
				.inflate(R.layout.activity_message_center, null);
		Handler_Inject.injectFragment(this, rootView);
		
		//一旦进入进来后的，就发送广播将小绿点给取消掉
		activity.sendBroadcast(new Intent("com.lansun.qmyo.DeleteTheLGPStatus"));
		LogUtils.toDebugLog("infos", "消息中心一打开，广播通知小绿点消失");
		
		lv_message_list.setNoHeader(true);
		lv_message_list.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefreshing() {
				//NO-OP
			}
			
			@Override
			public void onLoadingMore() {
				if (list != null) {
					if (TextUtils.isEmpty(list.getNext_page_url())||list.getData()==null||list.getNext_page_url()=="null") {
//						PullToRefreshManager.getInstance().onFooterRefreshComplete();
						
						if(times ==0){
							CustomToast.show(activity, "到底啦", "您目前只有这么多消息");
							lv_message_list.onLoadMoreOverFished();
							times++;
						}else{
							lv_message_list.onLoadMoreOverFished();
							
						}
						
					} else {
						refreshCurrentList(list.getNext_page_url(), null, 0, lv_message_list);
						lv_message_list.onLoadMoreFished();//需要将isLoadMore置为false
					}
				}else{
					CustomToast.show(activity, "到底啦", "您目前只有这么多消息");
					lv_message_list.onLoadMoreOverFished();
				}
			}
		});
		
		return rootView;
	}

	@InjectInit
	private void init() {
//		ListViewSwipeGesture touchListener = new ListViewSwipeGesture(
//				lv_message_list, swipeListener, getActivity());
//		
//		touchListener.SwipeType = ListViewSwipeGesture.Dismiss;
		
		/* 这个listView暂时关闭，不进行横向的滑移
		 * lv_message_list.setOnTouchListener(touchListener);*/

		refreshUrl = GlobalValue.URL_USER_MESSAGE_LIST+ GlobalValue.MESSAGE.activity;
		refreshKey = 0;
		refreshCurrentList(refreshUrl, null, refreshKey, lv_message_list);
	}

//	ListViewSwipeGesture.TouchCallbacks swipeListener = new ListViewSwipeGesture.TouchCallbacks() {
//		@Override
//		public void FullSwipeListView(int position) {
//		}
//		@Override
//		public void HalfSwipeListView(final int position) {
//		}
//		@Override
//		public void OnClickListView(int position) {
//			if (index==0) {
//				FragmentEntity entity=new FragmentEntity();
//				Fragment fragment=new ActivityDetailFragment();
//				Bundle bundle=new Bundle();
//				bundle.putString("shopId", list.getData().get(position).getShop_id()+"");
//				bundle.putString("activityId", list.getData().get(position).getActivity_id()+"");
//				fragment.setArguments(bundle);
//				entity.setFragment(fragment);
//				EventBus.getDefault().post(entity);
//			}else {
//				
//			}
//		}
//		@Override
//		public void LoadDataForScroll(int count) {
//
//		}
//		@Override
//		public void onDismiss(ListView listView, int[] reverseSortedPositions) {
//
//		}
//
//	};
	
	private void click(View view) {
		dataList.clear();
		adapter = null;
		times = 0;
		first_enter = 0;
		switch (view.getId()) {
		case R.id.tv_message_activity:// TODO 活动
			refreshUrl = GlobalValue.URL_USER_MESSAGE_LIST+ GlobalValue.MESSAGE.activity;
			changeTextColor(v.tv_message_activity);
			index=0;
			break;
		case R.id.tv_message_maijie:// TODO 迈界
			refreshUrl = GlobalValue.URL_USER_MESSAGE_LIST+ GlobalValue.MESSAGE.maijie;
			changeTextColor(v.tv_message_maijie);
			index=1;
			break;

		}
		setProgress(lv_message_list);
		startProgress();
		refreshCurrentList(refreshUrl, null, refreshKey, lv_message_list);
	}

	private void changeTextColor(TextView tv) {
		v.tv_message_maijie.setTextColor(getResources().getColor(
				R.color.app_grey5));
		v.tv_message_activity.setTextColor(getResources().getColor(
				R.color.app_grey5));
		tv.setTextColor(getResources().getColor(R.color.app_green2));
	};

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			endProgress();
			switch (r.getKey()) {
			case 0:
				endProgress();
				lv_message_list.setVisibility(View.VISIBLE);
				lv_message_list.onLoadMoreFished();
				
				list = Handler_Json.JsonToBean(MessageList.class,
						r.getContentAsString());
				if(list!=null){
					if (list.getData() != null) {
						v.no_data.setVisibility(View.GONE);
						HashMap<String, String> map = new HashMap<String, String>();
						
						for (MessageData data : list.getData()) {
							map = new HashMap<String, String>();
							map.put("activity_id", data.getActivity_id()+"");
							map.put("shop_id", data.getShop_id()+"");
							map.put("tv_message_item_name", data.getTitle());
							map.put("tv_message_item_desc", data.getContent());
							long time = Handler_Time.getInstance(data.getTime())
									.getTimeInMillis();
							String historyTime = "";
							if ((System.currentTimeMillis() - time)
									/ (1000 * 24 * 60 * 60) > 1) {
								Handler_Time instance = Handler_Time
										.getInstance(data.getTime());
								historyTime = instance.getYear() + "-"
										+ instance.getMonth() + "-"
										+ instance.getDay();
							} else {
								historyTime = getHistoryTime(System.currentTimeMillis() - time);
							}
							map.put("tv_search_activity_time", historyTime);
							map.put("tv_message_item_count", data.getIs_read() + "");
							dataList.add(map);
						}
						
						if (adapter == null) {
							/*adapter = new MessageAdapter(lv_message_list, dataList,R.layout.manager_group_list_item_parent);*/
							adapter = new SwipeListMineMessageAdapter(activity, dataList);
							adapter.setItemClickCallback(this);
							lv_message_list.setAdapter(adapter);
						} else {
							adapter.notifyDataSetChanged();
						}
						if(list.getData().size()<10){
							if(first_enter == 0){//数据少于10条，且是第一次进来刷的就少于10条，将尾部去除，且不弹出吐司
					            lv_message_list.onLoadMoreOverFished();
					          }else{
					            //DO-OP
					          }
//							times++;
//							lv_message_list.onLoadMoreOverFished();
//							CustomToast.show(activity, "到底啦", "您目前只有这么多消息");
						}
						this.first_enter = Integer.MAX_VALUE;
						
					} else {//list.getData() == null
						lv_message_list.setAdapter(null);
						lv_message_list.onLoadMoreOverFished();
						lv_message_list.setVisibility(View.INVISIBLE);
						v.no_data.setVisibility(View.VISIBLE);
					}
				}else{//list==null
					v.no_data.setVisibility(View.VISIBLE);
					lv_message_list.onLoadMoreOverFished();
				}
				
				break;
			}
		}
//		PullToRefreshManager.getInstance().onHeaderRefreshComplete();
//		PullToRefreshManager.getInstance().onFooterRefreshComplete();
	}

//	@InjectPullRefresh
//	private void call(int type) {
//		// 这里的type来判断是否是下拉还是上拉
//		switch (type) {
//		case InjectView.PULL:
//			if (list != null) {
//				if (TextUtils.isEmpty(list.getNext_page_url())) {
////					PullToRefreshManager.getInstance()
////							.onFooterRefreshComplete();
//					CustomToast.show(activity, "加载进度", "目前所有内容都已经加载完成");
//				} else {
//					refreshCurrentList(list.getNext_page_url(), null, 0,
//							lv_message_list);
//				}
//			}
//			break;
//		}
//	}
	
	private String getHistoryTime(long mss) {
		long days = mss / (1000 * 60 * 60 * 24);
		long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
		long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
		String time = "";
		if (minutes == 0) {
			time = "刚刚";
			return time;
		}
		if (minutes != 0) {
			time = minutes + "分" + time;
		}
		if (hours != 0) {
			time = hours + "小时";
		}
		return time + "前";
	}

	/**
	 * 重写在Adapter中设置的点击产生的实际操作，再次回调中实现
	 * @param position
	 */
	@Override
	public void listItemClick(int position) {
		if (index==0) {//活动消息
			FragmentEntity entity=new FragmentEntity();
			Fragment fragment=new ActivityDetailFragment();
			Bundle bundle=new Bundle();
			/*bundle.putString("shopId", list.getData().get(position).getShop_id()+"");
			bundle.putString("activityId", list.getData().get(position).getActivity_id()+"");*/
			
			bundle.putString("shopId", dataList.get(position).get("shop_id"));
			bundle.putString("activityId", dataList.get(position).get("activity_id"));
			fragment.setArguments(bundle);
			entity.setFragment(fragment);
			EventBus.getDefault().post(entity);
		}else {//迈界消息
			
		}
	}

}
