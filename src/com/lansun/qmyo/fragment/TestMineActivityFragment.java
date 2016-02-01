package com.lansun.qmyo.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectPullRefresh;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.PullToRefreshManager;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.ioc.view.listener.OnItemClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.android.pc.util.Handler_Time;
import com.lansun.qmyo.adapter.SearchAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.ActivityList;
import com.lansun.qmyo.domain.ActivityListData;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.utils.swipe.SwipeListMineActivityAdapter;
import com.lansun.qmyo.utils.swipe.Utils;
import com.lansun.qmyo.view.CustomDialogProgress;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ListViewSwipeGesture;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.view.TestMyListView;
import com.lansun.qmyo.view.TestMyListView.OnRefreshListener;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lansun.qmyo.R;

/**
 * 我收藏的活动
 * 
 * @author bhxx
 * 
 */
public class TestMineActivityFragment extends BaseFragment {

	private String underway = "1";
	public boolean isPullChanged = false;
	private int deletePosition;
	private int times = 0;
	private boolean isShowDialog = false;
	private CustomDialogProgress cPd = null;

	@InjectAll
	Views v;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View fl_comments_right_iv, tv_activity_shared;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView tv_activity_title, tv_activity_doing,
				tv_activity_expired;
		
		private RelativeLayout rl_no_postdelay_activity;
	}

	/*@InjectView(binders = { @InjectBinder(listeners = { OnItemClick.class }, method = "itemClick") }, down = true, pull = true)
	private MyListView lv_mine_activity;*/
	
	@InjectView(binders = { @InjectBinder(listeners = { OnItemClick.class }, method = "itemClick") }, pull = true)
	private TestMyListView lv_mine_activity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_mine_activity, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	/**
	 * 刷新门店信息
	 * 
	 * @param type
	 */
	@InjectInit
	private void init() {
		refreshParams = new LinkedHashMap<String, String>();
		v.fl_comments_right_iv.setVisibility(View.GONE);
		initTitle(v.tv_activity_title, R.string.activity_collection, null, 0);
//		PullToRefreshManager.getInstance().footerUnable();
//		PullToRefreshManager.getInstance().headerUnable();
		
		refreshParams.put("underway", underway);
		refreshUrl = GlobalValue.URL_USER_ACTIVITY + "?";
		//去拿刷新出来的数据
		refreshCurrentList(refreshUrl, refreshParams, 0, lv_mine_activity);
		
		lv_mine_activity.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefreshing() {
				if (list != null) {
					refreshParams.put("underway", underway);
					isPullChanged = false;
					times = 0;
					first_enter = 0;
					//再次刷新
					refreshCurrentList(refreshUrl, refreshParams, 0,lv_mine_activity);
				}
			}

			
			@Override
			public void onLoadingMore() {
				if (list != null) {
					/* 从代码习惯上来说，服务器返回的是"null"字符串，我认为是不应该的，空的话就应该是空串 ("") */
					 
					if (list.getNext_page_url()=="null"){
						if(times == 0){
							CustomToast.show(activity,R.string.reach_bottom,R.string.just_collect_acts);
							lv_mine_activity.onLoadMoreOverFished();
							times++;
						}else{
							lv_mine_activity.onLoadMoreOverFished();
						}
					} else {
						refreshParams.put("underway", underway);
						refreshCurrentList(list.getNext_page_url(), refreshParams,0, lv_mine_activity);
						isPullChanged = true;
					}
				}
			}

		});
	}
	
	private ArrayList<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();
	private SearchAdapter adapter;
	private ActivityList list;
	private SwipeListMineActivityAdapter swipeListAdapter;

	private void click(View view) {
		dataList.clear();
		swipeListAdapter = null;
		//adapter = null;
		v.rl_no_postdelay_activity.setVisibility(View.GONE);
		lv_mine_activity.setVisibility(View.GONE);
		
	    this.first_enter =0;//保证了每次新的关键字搜索时都拥有 是否为第一次加载的 判断标签
	    this.times = 0;
		
		switch (view.getId()) {
		case R.id.tv_activity_doing:
			
			underway = "1";
			changeTextColor(v.tv_activity_doing);
			refreshParams.put("underway", underway);
			refreshCurrentList(refreshUrl, refreshParams, 0, lv_mine_activity);
			times = 0;
			isShowDialog = true;
			break;
		case R.id.tv_activity_expired:
			underway = "0";
			changeTextColor(v.tv_activity_expired);
			refreshParams.put("underway", underway);
			refreshCurrentList(refreshUrl, refreshParams, 0, lv_mine_activity);
			times = 0;
			isShowDialog = true;
			break;
		}
		
		if (isShowDialog){
			if(cPd == null ){
				Log.d("dialog","生成新的dialog！");
				cPd = CustomDialogProgress.createDialog(activity);
				cPd.setCanceledOnTouchOutside(false);
				cPd.show();
			}else{
				cPd.show();
			}
		}
		
		//lv_mine_activity.setVisibility(View.INVISIBLE);
		
	}

	
	private void changeTextColor(TextView tv) {
		v.tv_activity_doing.setTextColor(getResources().getColor(
				R.color.app_grey5));
		v.tv_activity_expired.setTextColor(getResources().getColor(
				R.color.app_grey5));
		tv.setTextColor(getResources().getColor(R.color.app_green2));
	};

	@Override
	public void onPause() {
		/*dataList.clear();
		adapter = null;*/
		super.onPause();
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			if(cPd!=null){
				cPd.dismiss();
				cPd = null;
			}
			
			lv_mine_activity.setVisibility(View.INVISIBLE);
			
			endProgress();
			switch (r.getKey()) {
			case 0:
				lv_mine_activity.onLoadMoreFished();
				lv_mine_activity.onRefreshFinshed(true);
				
				if(isPullChanged){//上拉加载不清掉list
					isPullChanged = false;
				}else{
					dataList.clear();//dataList 需要在这里清理一下，不然会重复添加内容_byZdy
					//isPullChanged = true;
				}
				
				list = Handler_Json.JsonToBean(ActivityList.class,r.getContentAsString());
				
				if (list.getData() != null) {
					for (ActivityListData data : list.getData()) {
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("tv_search_activity_name", data.getShop()
								.getName());
						map.put("tv_search_activity_distance", data.getShop()
								.getDistance());
						map.put("tv_search_activity_desc", data.getActivity()
								.getName());
						map.put("iv_search_activity_head", data.getActivity()
								.getPhoto());
						
						//活动被收藏的时间
						long time = Handler_Time.getInstance(data.getActivity().getCollection_time()).getTimeInMillis();
						
						String historyTime = "";
						
						if ((System.currentTimeMillis() - time)/ (1000 * 24 * 60 * 60) > 1) {
							Handler_Time instance = Handler_Time.getInstance(data.getActivity().getCollection_time());
							Log.i("走的时间点", "超过1天了");
							historyTime = instance.getYear() + "-"+ instance.getMonth() + "-"+ instance.getDay();
						} else {
							Log.i("走的时间点", "24小时之类");
							long deltaTime = System.currentTimeMillis() - time;
							Log.i("相差的时间值",String.valueOf(System.currentTimeMillis()));
							Log.i("相差的时间值",String.valueOf(time));
							Log.i("相差的时间值",String.valueOf(deltaTime));
							
							historyTime = getHistoryTime(System.currentTimeMillis() - time);
						}

						map.put("tv_search_activity_distance", historyTime);
						map.put("activityId", data.getActivity().getId());
						map.put("shopId", data.getShop().getId());
						map.put("tv_search_tag", data.getActivity().getTag());
						map.put("icons", data.getActivity().getCategory());
						
						dataList.add(map);
					}
					
//					if (adapter == null) {
//						adapter = new SearchAdapter(lv_mine_activity, dataList,
//								R.layout.activity_search_item_usual);
//						lv_mine_activity.setAdapter(adapter);
//					} else {
//						adapter.notifyDataSetChanged();
//					}
					if (swipeListAdapter == null) {
						swipeListAdapter = new SwipeListMineActivityAdapter(activity,dataList);
						lv_mine_activity.setAdapter(swipeListAdapter);
					} else {
						swipeListAdapter.notifyDataSetChanged();
					}
					
					/*
					 * 当刷新到最后一页，且此页数据少于10条时，需要弹出吐司表示到底，并且将尾布局去除
					 */
					if(list.getData().size()<10){
						if(first_enter == 0){//数据少于10条，且是第一次进来刷的就少于10条，将尾部去除，且不弹出吐司
				            lv_mine_activity.onLoadMoreOverFished();
				          }else{
				            //DO-OP
				          }
						//CustomToast.show(activity, "到底啦！", "您只收藏了以上活动哦");
						//lv_mine_activity.onLoadMoreOverFished();
					}
					this.first_enter = Integer.MAX_VALUE;
					
					
//					PullToRefreshManager.getInstance().footerUnable();
//					PullToRefreshManager.getInstance().headerUnable();
					
//					PullToRefreshManager.getInstance().footerEnable();
//					PullToRefreshManager.getInstance().onHeaderRefreshComplete();
//					PullToRefreshManager.getInstance().onFooterRefreshComplete();
					
				} else {
					v.rl_no_postdelay_activity.setVisibility(View.VISIBLE);
					lv_mine_activity.setAdapter(null);
				}
				break;
				
			case 1:
				if ("true".equals(r.getContentAsString())) {
					dataList.remove(deletePosition);
					adapter.notifyDataSetChanged();
					/*CustomToast.show(activity, getString(R.string.tip),
							getString(R.string.delete_success));*/
					Utils.showToast(activity, "删除成功");
				} else {
					CustomToast.show(activity, getString(R.string.tip),
							getString(R.string.delete_faild));
				}
				break;
			}
		} else {
			progress_text.setText(R.string.net_error_check_net);
			lv_mine_activity.onLoadMoreFished();
			lv_mine_activity.onRefreshFinshed(true);
			
		}
//		PullToRefreshManager.getInstance().footerEnable();
//		PullToRefreshManager.getInstance().onHeaderRefreshComplete();
//		PullToRefreshManager.getInstance().onFooterRefreshComplete();
		
		PullToRefreshManager.getInstance().footerUnable();
		PullToRefreshManager.getInstance().headerUnable();
	}

	
	
//	@InjectPullRefresh
//	private void call(int type) {
//		// 这里的type来判断是否是下拉还是上拉
//		switch (type) {
//		case InjectView.PULL:
//			if (list != null) {
//				/*
//				 * 从代码习惯上来说，服务器返回的是"null"字符串，我认为是不应该的，空的话就应该是空串 ("")
//				 */
//				if (list.getNext_page_url()=="null"){
//					CustomToast.show(activity, "到底啦！", "您只收藏了以上活动哦");
////					PullToRefreshManager.getInstance().onFooterRefreshComplete();
////					PullToRefreshManager.getInstance().footerUnable();
//				} else {
//					refreshParams.put("underway", underway);
//					refreshCurrentList(list.getNext_page_url(), refreshParams,0, lv_mine_activity);
//					isPullChanged = true;
//				}
//			}
//			break;
//		case InjectView.DOWN:
//			if (list != null) {
//				refreshParams.put("underway", underway);
//				isPullChanged = false;
//				//再次刷新
//				refreshCurrentList(refreshUrl, refreshParams, 0,lv_mine_activity);
//			}
//			break;
//		}
//	}
	/**
	 * 由毫秒值转化为需要的历史时间样式
	 * @param mss
	 * @return
	 */
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
			time = minutes + "分" ;
		}
		if (hours != 0) {
			time = hours + "小时"+ time;
		}
		return time + "前";
	}
	
	@Override
	public void onDestroy() {
		lv_mine_activity = null;
		super.onDestroy();
	}
}
