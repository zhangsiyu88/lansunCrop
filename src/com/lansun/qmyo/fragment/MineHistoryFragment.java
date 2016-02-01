package com.lansun.qmyo.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.amap.api.mapcore2d.ev;
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
import com.lansun.qmyo.adapter.HomeListAdapter;
import com.lansun.qmyo.adapter.MineV16Adapter;
import com.lansun.qmyo.adapter.SearchAdapter;
import com.lansun.qmyo.adapter.StoreAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.Activity;
import com.lansun.qmyo.domain.BrowseData;
import com.lansun.qmyo.domain.HistoryActivity;
import com.lansun.qmyo.domain.HistoryActivityData;
import com.lansun.qmyo.domain.HistoryPromote;
import com.lansun.qmyo.domain.HomePromote;
import com.lansun.qmyo.domain.HomePromoteData;
import com.lansun.qmyo.domain.StoreInfo;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.MessageCenterFragment.Views;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.utils.swipe.SwipeListMineActivityAdapter;
import com.lansun.qmyo.utils.swipe.SwipeListMineHistoryActivityAdapter;
import com.lansun.qmyo.utils.swipe.SwipeListMineHistoryStoreAdapter;
import com.lansun.qmyo.utils.swipe.SwipeListMineStoreAdapter;
import com.lansun.qmyo.utils.swipe.Utils;
import com.lansun.qmyo.view.CustomDialogProgress;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ListViewSwipeGesture;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.view.TestMyListView;
import com.lansun.qmyo.view.TestMyListView.OnRefreshListener;
import com.lansun.qmyo.MainFragment;
import com.lansun.qmyo.R;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.database.CursorJoiner.Result;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 我的浏览记录
 * 
 * @author bhxx
 * 
 */
@SuppressLint("ResourceAsColor") public class MineHistoryFragment extends BaseFragment {

	@InjectAll
	Views v;
//	@InjectView(binders = { @InjectBinder(listeners = { OnItemClick.class }, method = "itemClick") }, pull = true)
//	private MyListView lv_mine_history_list;
	
	
	private HistoryActivity activityList;
	private HistoryActivity storeList;
	private ArrayList<HashMap<String, String>> storeDataList = new ArrayList<HashMap<String, String>>();
	private ArrayList<HashMap<String, Object>> activityDataList = new ArrayList<>();
	private ArrayList<HashMap<String, String>> v16DataList = new ArrayList<>();
	
	@InjectView
	private TestMyListView lv_mine_history_list;
	
	/*private StoreAdapter storeAdapter;
	private SearchAdapter activityAdapter;*/
	
	private SwipeListMineStoreAdapter storeAdapter;
	private SwipeListMineActivityAdapter activityAdapter;
	
	private CustomDialogProgress cPd = null;
	private boolean isShowDialog = false;
	
	
	private HistoryActivity v16list;
	private MineV16Adapter v16adapter;
	private boolean isPull = false;
	private View emptyView;
	private int times = 0;



	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView tv_mine_history_edit, tv_mine_history_activity,
				tv_mine_history_store, tv_mine_history_v16, tv_activity_shared,
				tv_activity_title;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View fl_comments_right_iv;
		private RelativeLayout  rl_new_emptyview;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_mine_history, null);
		Handler_Inject.injectFragment(this, rootView);
		
		lv_mine_history_list.setNoHeader(true);
		lv_mine_history_list.setOnRefreshListener(new OnRefreshListener(){
			
			private String currentRequestUrl;

			@Override
			public void onRefreshing() {
				//NO-OP
			}
			
			@Override
			public void onLoadingMore() {//在list.size()小于10时 ，就已经不会再走下面的方法操作了
				if (activityList != null || storeDataList != null) {
					if (refreshKey == 0) {
						if (TextUtils.isEmpty(activityList.getNext_page_url())||activityList.getNext_page_url().contains("[]")
								||activityList.getNext_page_url().contains("null")) {
//							PullToRefreshManager.getInstance().onFooterRefreshComplete();
//							PullToRefreshManager.getInstance().footerUnable();
							//CustomToast.show(activity, "到底啦！", "您最近仅浏览了这么多的活动");
							 if(times == 0){
					              lv_mine_history_list.onLoadMoreOverFished();
					              CustomToast.show(activity,  R.string.reach_bottom,R.string.just_watch_acts);
					              times++;
					            }else{
					              lv_mine_history_list.onLoadMoreOverFished();
					            }
							return;
						}
						refreshUrl = activityList.getNext_page_url();
					} else if (refreshKey == 1) {
						if (TextUtils.isEmpty(storeList.getNext_page_url())||storeList.getNext_page_url().contains("[]")
								||storeList.getNext_page_url().contains("null")) {
//							PullToRefreshManager.getInstance().onFooterRefreshComplete();
//							PullToRefreshManager.getInstance().footerUnable();
							//CustomToast.show(activity, "到底啦！", "您最近仅浏览了这么多的门店");
							 if(times == 0){
					              lv_mine_history_list.onLoadMoreOverFished();
					              CustomToast.show(activity,  R.string.reach_bottom,R.string.just_watch_store);
					              times++;
					            }else{
					              lv_mine_history_list.onLoadMoreOverFished();
					            }
							return;
						}
						refreshUrl = storeList.getNext_page_url();
					} else {
						if (TextUtils.isEmpty(v16list.getNext_page_url())) {
//							PullToRefreshManager.getInstance().onFooterRefreshComplete();
							CustomToast.show(activity, "加载进度", "目前所有内容都已经加载完成");
							return;
						}
						refreshUrl = v16list.getNext_page_url();
					}
					
					
					if(currentRequestUrl == refreshUrl){
						//DO-OP
						//CustomToast.show(activity, "提示", "准备重复加载");
					}else{
						refreshCurrentList(refreshUrl, null, refreshKey,lv_mine_history_list);
						currentRequestUrl = refreshUrl;
					}
					
					lv_mine_history_list.onLoadMoreFished();
					isPull  = true;
				} 
			}
		});
		
		
		emptyView = rootView.findViewById(R.id.rl_new_emptyview);
		View tv_gotoHomeFrag = emptyView.findViewById(R.id.tv_gotoHomeFrag);
		tv_gotoHomeFrag.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				/*HomeFragment fragment = new HomeFragment();*/
				MainFragment fragment = new MainFragment(0);
				FragmentEntity entity = new FragmentEntity();
				entity.setFragment(fragment);
				EventBus.getDefault().post(entity);
			}
		});
		
		return rootView;
	}

	@InjectInit
	private void init() {
		v.fl_comments_right_iv.setVisibility(View.GONE);
		
		//v.tv_activity_shared.setText(R.string.all_clear);
		
		initTitle(v.tv_activity_title, R.string.mine_history, null, 0);

		refreshUrl = GlobalValue.URL_USER_ACTIVITYBROWSES;
		refreshKey = 0;
		refreshCurrentList(refreshUrl, null, refreshKey, lv_mine_history_list);
	}

	private void click(View view){
		/**
		 * 点击前将所有数据清除掉
		 */
		/*activityAdapter = null;
		storeAdapter = null;
		v16adapter = null;
		activityDataList.clear();
		storeDataList.clear();
		v16DataList.clear();*/
		
		this.times  = 0;
		this.first_enter =0;
		
		

		switch (view.getId()) {
		case R.id.tv_mine_history_activity:// 活动
			this.times  = 0;
			this.first_enter =0;
			lv_mine_history_list.onLoadMoreFished();//需将OnLoadingMore的参数值重新置为 false,供后面的测试使用
			
			
			lv_mine_history_list.setVisibility(View.INVISIBLE);//防止脚步局在切换时显示出来
			emptyView.setVisibility(View.INVISIBLE);
			v.rl_new_emptyview.setVisibility(View.INVISIBLE);
			refreshKey = 0;
			
			activityAdapter = null;
			activityDataList.clear();
			
			lv_mine_history_list.setAdapter(null);
			changeTextColor(v.tv_mine_history_activity);
			refreshUrl = GlobalValue.URL_USER_ACTIVITYBROWSES;
			refreshCurrentList(refreshUrl, null, refreshKey,
					lv_mine_history_list);
			isShowDialog  = true;
			break;
		case R.id.tv_mine_history_store:// 门店
			this.times  = 0;
			this.first_enter =0;
			lv_mine_history_list.onLoadMoreFished();//需将OnLoadingMore的参数值重新置为 false,供后面的测试使用
			
			lv_mine_history_list.setVisibility(View.INVISIBLE);//防止脚步局在切换时显示出来
			emptyView.setVisibility(View.INVISIBLE);
			v.rl_new_emptyview.setVisibility(View.INVISIBLE);
			
			refreshKey = 1;
			
			storeAdapter = null;
			storeDataList.clear();
			
			lv_mine_history_list.setAdapter(null);
			refreshUrl = GlobalValue.URL_USER_SHOPBROWSES;
			changeTextColor(v.tv_mine_history_store);
			refreshCurrentList(refreshUrl, null, refreshKey,
					lv_mine_history_list);
			isShowDialog = true;
			break;
			
		/*case R.id.tv_mine_history_v16:// V16

			refreshKey = 3;
			refreshUrl = GlobalValue.URL_USER_ARTICLEBROWSES;
			changeTextColor(v.tv_mine_history_v16);
			refreshCurrentList(refreshUrl, null, refreshKey,
					lv_mine_history_list);
			break;*/
			
		case R.id.tv_activity_shared:// 全部清除的按钮
			DialogUtil.createTipAlertDialog(activity, R.string.all_clear_tip,
					new TipAlertDialogCallBack() {

						@Override
						public void onPositiveButtonClick(
								DialogInterface dialog, int which) {
							dialog.dismiss();
							/*InternetConfig config = new InternetConfig();
							config.setKey(2);
							HashMap<String, Object> head = new HashMap<>();
							head.put("Authorization",
									"Bearer " + App.app.getData("access_token"));
							config.setHead(head);
							config.setRequest_type(InternetConfig.request_delete);
							config.setMethod("DELETE");
							FastHttpHander.ajax(
									GlobalValue.URL_USER_DELETE_BROWSES,
									config, MineHistoryFragment.this);*/
							HttpUtils httpUtils = new HttpUtils();
							RequestCallBack<String> requestCallBack = new RequestCallBack<String>() {

								@Override
								public void onFailure(HttpException arg0, String arg1) {
									
								}

								@Override
								public void onSuccess(ResponseInfo<String> arg0) {
									if ("true".equals(arg0.result.toString())) {
										
										
										/*	CustomToast.show(activity, R.string.tip,R.string.delete_success);*/
										Utils.showToast(activity, "删除成功");
										
											activityDataList.clear();
											storeDataList.clear();
											/*storeAdapter.notifyDataSetChanged();
											activityAdapter.notifyDataSetChanged();*/
											
											lv_mine_history_list.setAdapter(null);
											
											setTagColorAndPressed();
											
											
											/* NullPointException
											 * activityAdapter.notify();
											storeAdapter.notify();*/
											
											/*refreshCurrentList(refreshUrl, null, refreshKey,
													lv_mine_history_list);*/
											/*v16DataList.clear();
											v16adapter.notifyDataSetChanged();*/
											
											
											/**最新需求: 删除后，不要跳转至上一页（我的页面），而是停留在当前页，并显示emptyView内容
											 * back();
											 */
											lv_mine_history_list.setVisibility(View.GONE);
											v.rl_new_emptyview.setVisibility(View.VISIBLE);
											
											//当全部清除成功的时候，才会弹出dialog
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
											
//											PullToRefreshManager.getInstance().footerUnable();
											
										} else {
											CustomToast.show(activity, getString(R.string.tip),
													getString(R.string.delete_faild));
										}
								}
								
							};
							
							
							RequestParams requestParams = new RequestParams();
							requestParams.addHeader("Authorization", "Bearer" + App.app.getData("access_token"));
							
							//TODO  注意下面拼接的url中的接口域名记得替换
							httpUtils.send(HttpMethod.DELETE, GlobalValue.URL_USER_DELETE_BROWSES,requestParams,requestCallBack );
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

	private void changeTextColor(TextView tv) {
		/*v.tv_mine_history_v16.setTextColor(getResources().getColor(
				R.color.app_grey5));*/
		v.tv_mine_history_activity.setTextColor(getResources().getColor(
				R.color.app_grey5));
		v.tv_mine_history_store.setTextColor(getResources().getColor(
				R.color.app_grey5));
		tv.setTextColor(getResources().getColor(R.color.app_green2));
	};

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			endProgress();
			
			if(cPd!=null){
				cPd.dismiss();
				cPd = null;
			}
			
			//lv_mine_history_list.setVisibility(View.VISIBLE);
			
			v16DataList.clear();
			switch (r.getKey()) {
			case 0:
				lv_mine_history_list.onLoadMoreFished();
				
				activityList = Handler_Json.JsonToBean(HistoryActivity.class,
						r.getContentAsString());
				if(activityList.getData()!=null){
					lv_mine_history_list.setVisibility(View.VISIBLE);
					v.rl_new_emptyview.setVisibility(View.GONE);
					if(isPull){
						isPull =false;
					}else{
						activityDataList.clear();
					}
					for (BrowseData data : activityList.getData()) {
						HashMap<String, Object> map = new HashMap<>();
						map.put("tv_search_activity_name", data.getShop().getName());
						long time = Handler_Time.getInstance(data.getBrowse().getTime()).getTimeInMillis();
						
						String historyTime = "";
						if ((System.currentTimeMillis() - time)
								/ (1000 * 24 * 60 * 60) > 1) {
							Handler_Time instance = Handler_Time.getInstance(data
									.getBrowse().getTime());
							historyTime = instance.getYear() + "-"
									+ instance.getMonth() + "-" + instance.getDay();
						} else {
							historyTime = getHistoryTime(System.currentTimeMillis() - time);
						}
						map.put("activityId", data.getActivity().getId());
						map.put("shopId", data.getShop().getId());
						map.put("tv_search_activity_distance", historyTime);
						map.put("tv_search_activity_desc", data.getActivity().getName());
						map.put("iv_search_activity_head", data.getActivity().getPhoto());
						map.put("tv_search_tag", data.getActivity().getTag());
						map.put("icons", data.getActivity().getCategory());
						activityDataList.add(map);
						}
					
					     setTagColorAndPressed();
					if (activityAdapter == null) {
//						activityAdapter = new SearchAdapter(lv_mine_history_list,
//								activityDataList, R.layout.activity_search_item);
						
						activityAdapter = new SwipeListMineActivityAdapter(activity, activityDataList ,false);
						
						lv_mine_history_list.setAdapter(activityAdapter);
//						PullToRefreshManager.getInstance().footerEnable();
//						PullToRefreshManager.getInstance().onFooterRefreshComplete();
					} else {
						activityAdapter.notifyDataSetChanged();
//						PullToRefreshManager.getInstance().footerEnable();
//						PullToRefreshManager.getInstance().onFooterRefreshComplete();
					}
					if(activityList.getData().size()<10){
//						times++;
//						lv_mine_history_list.onLoadMoreOverFished();//此时根本不会进行onLoadingMore的操作了，所以onLoadingMore的操作是无效的
//						CustomToast.show(activity, "到底啦!", "您最近仅浏览了这么多的活动");
						if(first_enter == 0){//数据少于10条，且是第一次进来刷的就少于10条，将尾部去除，且不弹出吐司
				            lv_mine_history_list.onLoadMoreOverFished();
				          }else{
				            //DO-OP
				          }
					}
					this.first_enter = Integer.MAX_VALUE;
					
			}else{
				lv_mine_history_list.setVisibility(View.GONE);
				v.rl_new_emptyview.setVisibility(View.VISIBLE);
				
//				PullToRefreshManager.getInstance().footerUnable();
			}
				break;
				
			case 1:
				lv_mine_history_list.onLoadMoreFished();
				
				storeList = Handler_Json.JsonToBean(HistoryActivity.class,
						r.getContentAsString());
				
				if(storeList.getData()!=null){
					lv_mine_history_list.setVisibility(View.VISIBLE);
					v.rl_new_emptyview.setVisibility(View.GONE);
					
					if(isPull){
						isPull =false;
					}else{
						storeDataList.clear();
					}
				
					for (BrowseData data : storeList.getData()) {
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("shop_id", data.getShop().getId()+"");
						map.put("tv_store_item_name", data.getShop().getName());
						map.put("tv_store_item_num", data.getShop().getAttention()
								+ "");
						long time = Handler_Time.getInstance(
								data.getBrowse().getTime()).getTimeInMillis();
						String historyTime = "";
						if ((System.currentTimeMillis() - time)
								/ (1000 * 3600 * 24) > 1) {
							Handler_Time instance = Handler_Time.getInstance(data
									.getBrowse().getTime());
							historyTime = instance.getYear() + "-"
									+ instance.getMonth() + "-" + instance.getDay();
						} else {
							historyTime = getHistoryTime(System.currentTimeMillis()
									- time);
						}
						map.put("tv_store_item_distance", historyTime);
						map.put("rb_store_item", data.getShop().getEvaluate() + "");
	
						storeDataList.add(map);
					}
					
					setTagColorAndPressed();
					
					if (storeAdapter == null) {
						/*storeAdapter = new StoreAdapter(lv_mine_history_list,
								storeDataList, R.layout.activity_store_item);*/
						storeAdapter = new SwipeListMineStoreAdapter(activity, storeDataList,false);
						lv_mine_history_list.setAdapter(storeAdapter);
						
						
//						PullToRefreshManager.getInstance().footerEnable();
//						PullToRefreshManager.getInstance().onFooterRefreshComplete();
					} else {
						storeAdapter.notifyDataSetChanged();
//						PullToRefreshManager.getInstance().footerEnable();
//						PullToRefreshManager.getInstance().onFooterRefreshComplete();
					}
					if(storeList.getData().size()<10){
//						times++;
//						lv_mine_history_list.onLoadMoreOverFished();//此时根本不会进行onLoadingMore的操作了，所以onLoadingMore的操作是无效的
//						CustomToast.show(activity, "到底啦！", "您最近仅浏览了这么多的门店");
						
						if(first_enter == 0){//数据少于10条，且是第一次进来刷的就少于10条，将尾部去除，且不弹出吐司
				            lv_mine_history_list.onLoadMoreOverFished();
				          }else{
				            //DO-OP
				          }
					}
					this.first_enter = Integer.MAX_VALUE;
			}else{
				lv_mine_history_list.setVisibility(View.GONE);//数据列表不可见
				v.rl_new_emptyview.setVisibility(View.VISIBLE);//空提示插画可见
//				PullToRefreshManager.getInstance().footerUnable();
			}
				break;
		   /*case 2:
				if ("true".equals(r.getContentAsString())) {
					CustomToast.show(activity, R.string.tip,
							R.string.delete_success);
					activityDataList.clear();
					storeDataList.clear();
					activityAdapter.notifyDataSetChanged();
					storeAdapter.notifyDataSetChanged();
					v16DataList.clear();
					v16adapter.notifyDataSetChanged();
				}
				break;*/
			case 3:
				v16list = Handler_Json.JsonToBean(HistoryActivity.class,
						r.getContentAsString());
				if (v16list.getData() != null) {
					for (BrowseData data : v16list.getData()) {
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("iv_mine_v16_head", data.getArticle()
								.getPhoto());
						map.put("tv_mine_v16_name", data.getArticle().getName());
						map.put("tv_mine_v16_tag", data.getArticle().getTags()
								.get(0));
						String historyTime = "";
						if ((System.currentTimeMillis() - Handler_Time
								.getInstance(data.getBrowse().getTime())
								.getTimeInMillis())
								/ (1000 * 24 * 60 * 60) > 1) {
							Handler_Time instance = Handler_Time
									.getInstance(data.getBrowse().getTime());
							historyTime = instance.getYear() + "-"
									+ instance.getMonth() + "-"
									+ instance.getDay();
						} else {
							historyTime = getHistoryTime(System
									.currentTimeMillis()
									- Handler_Time.getInstance(
											data.getBrowse().getTime())
											.getTimeInMillis());
						}
						map.put("tv_mine_v16_time", historyTime);
						map.put("url", data.getArticle().getUrl());
						v16DataList.add(map);
					}
				} else {
					lv_mine_history_list.setAdapter(null);
				}
				if (v16adapter == null) {
					v16adapter = new MineV16Adapter(lv_mine_history_list,
							v16DataList, R.layout.mine_comments_v16_item);
					lv_mine_history_list.setAdapter(v16adapter);
				} else {
					v16adapter.notifyDataSetChanged();
				}
				break;
			}
		} else {

			progress_text.setText(R.string.net_error_refresh);
		}
		/*PullToRefreshManager.getInstance().onHeaderRefreshComplete();
		PullToRefreshManager.getInstance().onFooterRefreshComplete();*/
	}

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
	 * 
	 * @param type
	 */
	@InjectPullRefresh
	private void myCall(int type) {
		switch (type) {
		case InjectView.PULL:
			if (activityList != null || storeDataList != null) {
				if (refreshKey == 0) {
					if (TextUtils.isEmpty(activityList.getNext_page_url())||activityList.getNext_page_url().contains("[]")
							||activityList.getNext_page_url().contains("null")) {
//						PullToRefreshManager.getInstance().onFooterRefreshComplete();
//						PullToRefreshManager.getInstance().footerUnable();
						CustomToast.show(activity, R.string.reach_bottom,R.string.just_watch_acts);
						return;
					}
					refreshUrl = activityList.getNext_page_url();
				} else if (refreshKey == 1) {
					if (TextUtils.isEmpty(storeList.getNext_page_url())||storeList.getNext_page_url().contains("[]")
							||storeList.getNext_page_url().contains("null")) {
//						PullToRefreshManager.getInstance().onFooterRefreshComplete();
//						PullToRefreshManager.getInstance().footerUnable();
						CustomToast.show(activity,  R.string.reach_bottom,R.string.just_watch_acts);
						return;
					}
					refreshUrl = storeList.getNext_page_url();
				} else {
					if (TextUtils.isEmpty(v16list.getNext_page_url())) {
//						PullToRefreshManager.getInstance().onFooterRefreshComplete();
						CustomToast.show(activity, "加载进度", "目前所有内容都已经加载完成");
						return;
					}
					refreshUrl = v16list.getNext_page_url();
				}
				refreshCurrentList(refreshUrl, null, refreshKey,lv_mine_history_list);
				isPull  = true;
			} 
			break;
		case InjectView.DOWN://拒绝下拉操作
			/*if (refreshKey == 0) {
				refreshUrl = GlobalValue.URL_USER_ACTIVITYBROWSES;
			} else if (refreshKey == 1) {
				refreshUrl = GlobalValue.URL_USER_SHOPBROWSES;
			} else {
				refreshUrl = GlobalValue.URL_USER_ARTICLEBROWSES;
			}
			refreshCurrentList(refreshUrl, null, refreshKey,
					lv_mine_history_list);*/
			break;
		}
	}

	/**
	 * 进入活动或者门店界面
	 * 
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	private void itemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Fragment fragment = null;
		Bundle args = new Bundle();
		
		//根据refreshKey 来进行设置活动详情页
		if (refreshKey == 0) {
			fragment = new ActivityDetailFragment();
			
			/*map.put("activityId", data.getActivity().getId());
			map.put("shopId", data.getShop().getId());*/
			
			args.putString("shopId", activityDataList.get(arg2).get("shopId")+ "");
			args.putString("activityId", activityDataList.get(arg2).get("activityId")+ "");
			
		}
		if (refreshKey == 1) {
			fragment = new StoreDetailFragment();
			args.putString("shopId", storeDataList.get(arg2).get("shopId")+ "");
		} /*else {
			fragment = new PromoteDetailFragment();
			args.putSerializable("promote", v16list.getData().get(arg2)
					.getArticle());
		}*/
		
		if (fragment != null) {
			fragment.setArguments(args);
			FragmentEntity event = new FragmentEntity();
			event.setFragment(fragment);
			EventBus.getDefault().post(event);
		}
	}

	@Override
	public void onPause() {
		storeAdapter = null;
		activityAdapter = null;
		super.onPause();
	}
	
	
	private void setTagColorAndPressed() {
		if(activityDataList.size()==0 && storeDataList.size()==0){
			v.tv_activity_shared.setText("");
		}else {
			v.tv_activity_shared.setText(R.string.all_clear);
			/*v.tv_activity_shared.setTextColor(R.color.app_green2);*/
			v.tv_activity_shared.setTextColor(Color.rgb(157, 193, 79));
		}
	}
}
