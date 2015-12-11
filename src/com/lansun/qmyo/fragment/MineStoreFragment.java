package com.lansun.qmyo.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.lansun.qmyo.adapter.StoreAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.Shop;
import com.lansun.qmyo.domain.StoreList;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.swipe.SwipeListMineStoreAdapter;
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
public class MineStoreFragment extends BaseFragment {
	@InjectAll
	Views v;

	private String currentType = "1";
	private boolean isShowDialog = false;
	private CustomDialogProgress cPd = null;
	private int times = 0;

	class Views {
		private View fl_comments_right_iv, tv_activity_shared;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView tv_activity_title, tv_store_has, tv_store_no;
		
		private TextView tv_mine_store_empty;
		private RelativeLayout rl_no_postdelay_store;
	}

	/*@InjectView(binders = { @InjectBinder(listeners = { OnItemClick.class }, method = "itemClick") }, down = true, pull = true)
	private MyListView lv_mine_store;*/
	
	/*@InjectView(binders = { @InjectBinder(listeners = { OnItemClick.class }, method = "itemClick") }, pull = true)
	private MyListView lv_mine_store;*/
	
	@InjectView
	private TestMyListView lv_mine_store;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_mine_store, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		v.fl_comments_right_iv.setVisibility(View.GONE);
		initTitle(v.tv_activity_title, R.string.gz_store, null, 0);
		
		/*ListViewSwipeGesture touchListener = new ListViewSwipeGesture(lv_mine_store, swipeListener, getActivity());
		touchListener.SwipeType = ListViewSwipeGesture.Dismiss;
		lv_mine_store.setOnTouchListener(touchListener);*/
		
		lv_mine_store.setOnRefreshListener(new OnRefreshListener() {
			

			@Override
			public void onRefreshing() {
				if (list != null) {
				LinkedHashMap<String, String> refreshParams = new LinkedHashMap<String, String>();
				refreshParams.put("underway", currentType);
				refreshCurrentList(refreshUrl, refreshParams, refreshKey, lv_mine_store);
				times = 0;
				lv_mine_store.onLoadMoreFished();
//				PullToRefreshManager.getInstance().footerEnable();
				
				}
			}

			
			@Override
			public void onLoadingMore() {
				if (list != null) {
					//其实这才是判断的真实依据，由于账号异常所以会将判断标准一直
					if (list.getNext_page_url()== "null"||
							TextUtils.isEmpty(list.getNext_page_url())) {
						
						Log.i("第三次刷新","第二次拿到的list中的next_page_url的值为null");
						
						if(times <1){
							CustomToast.show(activity, "到底啦！", "您只关注了以上的门店哦");
							times++;
							lv_mine_store.onLoadMoreOverFished();
						}else{
							lv_mine_store.onLoadMoreOverFished();
							
						}
						
//						PullToRefreshManager.getInstance().onFooterRefreshComplete();
//						PullToRefreshManager.getInstance().footerUnable();
//						PullToRefreshManager.getInstance().headerUnable();
					} else {
						refreshParams = new LinkedHashMap<String, String>();
						refreshParams.put("underway", currentType);
						refreshCurrentList(list.getNext_page_url(), refreshParams, refreshKey, lv_mine_store);
						isPull  = true;
						/*adapter = null;*/
					}
				}else{
					lv_mine_store.onLoadMoreOverFished();
				}
			}

		});
		
		refreshParams = new LinkedHashMap<String, String>();
		refreshUrl = GlobalValue.URL_USER_GZ_SHOP + "?";
		refreshKey = 0;
		refreshParams.put("underway", currentType);
		refreshCurrentList(refreshUrl, refreshParams, refreshKey, lv_mine_store);
	}

	private int deletePosition;
	
	/**
	 * 最强大的ListView滑动监听者
	 */
//	ListViewSwipeGesture.TouchCallbacks swipeListener = new ListViewSwipeGesture.TouchCallbacks() {
//
//		@Override
//		public void FullSwipeListView(int position) {
//		}
//
//		@Override
//		public void HalfSwipeListView(final int position) {
//			deletePosition = position;
//			InternetConfig config = new InternetConfig();
//			config.setKey(3);
//			HashMap<String, Object> head = new HashMap<>();
//			head.put("Authorization","Bearer " + App.app.getData("access_token"));
//			config.setHead(head);
//			config.setMethod("DELETE");
//			
//			/* DELETE请求无响应
//			 * FastHttpHander.ajax(GlobalValue.URL_QX_GZ_SHOP+ dataList.get(position).get("shop_id"), null,
//					config, MineStoreFragment.this);*/
//			
//			//活动删除代码：
//			HttpUtils httpUtils = new HttpUtils();
//			RequestCallBack<String> requestCallBack = new RequestCallBack<String>() {
//
//				@Override
//				public void onFailure(HttpException arg0, String arg1) {
//					
//				}
//
//				@Override
//				public void onSuccess(ResponseInfo<String> arg0) {
//					if ("true".equals(arg0.result.toString())) {
//							dataList.remove(deletePosition);
//							adapter.notifyDataSetChanged();
//							if(dataList.size()==0){//删除后数据列表为零
//								  v.rl_no_postdelay_store.setVisibility(View.VISIBLE);
//							}
//							CustomToast.show(activity, getString(R.string.tip),
//									getString(R.string.delete_success));
//						} else {
//							CustomToast.show(activity, getString(R.string.tip),
//									getString(R.string.delete_faild));
//						}
//				}
//
//				
//			};
//			RequestParams requestParams = new RequestParams();
//			requestParams.addHeader("Authorization", "Bearer" + App.app.getData("access_token"));
//			
//			//TODO  注意下面拼接的url中的接口域名记得替换
//			httpUtils.send(HttpMethod.DELETE, 
//					GlobalValue.URL_QX_GZ_SHOP+ dataList.get(position).get("shop_id"),
//					requestParams,requestCallBack );
//			
//			
//		}
//
//		@Override
//		public void OnClickListView(int position) {
//			StoreDetailFragment fragment = new StoreDetailFragment();
//			Bundle args = new Bundle();
//			
//			/* 居然连类型都得分开识别，这里的shopId是int类型
//			 * args.putInt("shopId",Integer.parseInt(dataList.get(position).get("shop_id")));*/
//			
//			args.putString("shopId",dataList.get(position).get("shop_id"));
//			
//			fragment.setArguments(args);
//			FragmentEntity event = new FragmentEntity();
//			event.setFragment(fragment);
//			EventBus.getDefault().post(event);
//		}
//
//		@Override
//		public void LoadDataForScroll(int count) {
//
//		}
//
//		@Override
//		public void onDismiss(ListView listView, int[] reverseSortedPositions) {
//
//		}
//	};

	private ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();;

	/*private StoreAdapter adapter;*/
	private SwipeListMineStoreAdapter adapter;
	private StoreList list;

	private boolean isPull = false;;

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			endProgress();
			if(cPd!=null){
				cPd.dismiss();
				cPd = null;
			}
			
			lv_mine_store.setVisibility(View.VISIBLE);
			
			switch (r.getKey()) {
			case 0:
				lv_mine_store.onLoadMoreFished();
				lv_mine_store.onRefreshFinshed(true);
				
				if(isPull){//是上拉操作，便不对数据源 list对象进行操作
					isPull = false;
				}else{// 若不是上拉操作，但却访问服务器进来，很明显需要将这个list清掉，用来放入获取得到的StoreList的值
					dataList.clear();
				}
				
				list = Handler_Json.JsonToBean(StoreList.class,r.getContentAsString());
				//拿到的数据为空的话，展示出没拿到数据的界面
				if(list.getData()== null){
					Log.i("list的值为多少？", "list的值为多少？"+r.getContentAsString().toString());
				}
				
				Log.i("从服务器上获取的list",r.getContentAsString());
				HashMap<String, String> map = new HashMap<String, String>();
				
				if (list.getData() != null) {//返回数据正常，则将其数据存到dataList
					for (Shop s : list.getData()) {
						map = new HashMap<String, String>();
						map.put("tv_store_item_name", s.getName());
						map.put("shop_id", s.getId() + "");
						map.put("tv_store_item_num", s.getAttention() + "");

						long time = Handler_Time.getInstance(s.getCollection_time()).getTimeInMillis();
						
						String historyTime = "";
						if ((System.currentTimeMillis() - time)/ (1000 * 24 * 60 * 60) > 1) {
							
							Handler_Time instance = Handler_Time.getInstance(s.getCollection_time());
							historyTime = instance.getYear() + "-"
									+ instance.getMonth() + "-"
									+ instance.getDay();
						} else {
							historyTime = getHistoryTime(System.currentTimeMillis() - time);
						}

						map.put("tv_store_item_distance", historyTime);
						map.put("rb_store_item", s.getEvaluate() + "");
						
						dataList.add(map);
				     } 
					 if (adapter == null) {
							/*adapter = new StoreAdapter(lv_mine_store, dataList,R.layout.activity_store_item);*/
						 adapter = new SwipeListMineStoreAdapter(activity,dataList);
						 lv_mine_store.setAdapter(adapter);
							
					 } else {
						    adapter.notifyDataSetChanged();
						}
					/*
					 * 当刷新到最后一页，且此页数据少于10条时，需要弹出吐司表示到底，并且将尾布局去除
					 */
					if(list.getData().size()<10){
						if(first_enter == 0){//数据少于10条，且是第一次进来刷的就少于10条，将尾部去除，且不弹出吐司
				            lv_mine_store.onLoadMoreOverFished();
				          }else{
				            //DO-OP
				          }
//						CustomToast.show(activity, "到底啦！", "您只关注了以上的门店哦");
//						lv_mine_store.onLoadMoreOverFished();
					}
					if(dataList.size()==0){//删除后数据列表为零
						  v.rl_no_postdelay_store.setVisibility(View.VISIBLE);
					}
					 
//					PullToRefreshManager.getInstance().footerEnable();
					/*PullToRefreshManager.getInstance().headerEnable();
					PullToRefreshManager.getInstance().onHeaderRefreshComplete();*/
//					PullToRefreshManager.getInstance().onFooterRefreshComplete();
					this.first_enter = Integer.MAX_VALUE;
					
					
			   }else{//未拿到数据，即表明数据返回为空，那么可以在这个地方添加上 背景图片
				   Log.i("从服务器未拿到数据！","list.getData() 很可能等于 null");
				   
				   while(times <1){
					   //CustomToast.show(activity, "到底啦！", "您只关注了以上的门店哦");
					   lv_mine_store.onLoadMoreOverFished();
					   times=Integer.MAX_VALUE;
					}
				   
				   if(dataList.size()>0){
					   //DoNothing
				   }else{
					   if(adapter!=null){
						   adapter.notifyDataSetChanged();
					   }
					   v.rl_no_postdelay_store.setVisibility(View.VISIBLE);
				   }
				   
				    //adapter = null;
					//lv_mine_store.setAdapter(adapter);
				
				   /*adapter.notifyDataSetChanged();
				   TextView textView = new TextView(activity);
				   textView.setTextSize(10);
				   textView.setText("您关注的门店中暂无过期的店铺");
				   LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
				   textView.setLayoutParams(layoutParams);
				   textView.setGravity(Gravity.CENTER_VERTICAL);
				   lv_mine_store.addFooterView(textView);*/
				   
				   
				   /*lv_mine_store.setVisibility(View.GONE);
				   progress.setVisibility(View.VISIBLE);
				   progress_container.setVisibility(View.VISIBLE);
				   progress_text.setText("您关注的门店中暂无过期的店铺");*/
				   
				   /*DisplayMetrics dm = getResources().getDisplayMetrics();
					int screenWidth = dm.widthPixels;
					int screenHeight = dm.heightPixels;
					int cueentHeight = (int) (((double) screenWidth / 720) * v.vp_baner.getLayoutParams().height);
					
					FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
							screenWidth, cueentHeight);
					v.vp_baner.setLayoutParams(params);*/
					
				   //刷到
//					PullToRefreshManager.getInstance().onHeaderRefreshComplete();
//					PullToRefreshManager.getInstance().onFooterRefreshComplete();
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
		} else {//网络返回值不为 OK
			progress_text.setText(R.string.net_error_refresh);
		}
		
//		PullToRefreshManager.getInstance().onHeaderRefreshComplete();
//		PullToRefreshManager.getInstance().onFooterRefreshComplete();
	}

	
	private void click(View view) {
		
		this.first_enter =0;//保证了每次新的关键字搜索时都拥有 是否为第一次加载的 判断标签
	    this.times = 0;
	    
		v.rl_no_postdelay_store.setVisibility(View.GONE);
		lv_mine_store.setVisibility(View.GONE);
		
		switch (view.getId()) {
		case R.id.tv_store_has:
			v.rl_no_postdelay_store.setVisibility(View.GONE);
			currentType = "1";
			changeTextColor(v.tv_store_has);
			refreshParams.put("underway", currentType);
			refreshUrl = GlobalValue.URL_USER_GZ_SHOP + "?";
			refreshKey = 0;
			refreshCurrentList(refreshUrl, refreshParams, refreshKey, lv_mine_store);
			isShowDialog = true;
			break;
		case R.id.tv_store_no:
			v.rl_no_postdelay_store.setVisibility(View.GONE);
			currentType = "0";
			changeTextColor(v.tv_store_no);
			refreshUrl = GlobalValue.URL_USER_GZ_SHOP + "?";
			refreshKey = 0;
			refreshParams.put("underway", currentType);
			refreshCurrentList(refreshUrl, refreshParams, refreshKey, lv_mine_store);
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
		
		/*Log.i("接下来我们进行刷新操作","点击TextView的按钮进行刷新操作！");
		refreshParams.put("underway", currentType);
		refreshCurrentList(refreshUrl, refreshParams, refreshKey, lv_mine_store);*/
		
		/*这是活动收藏中的写法
		 * underway = "0";
		changeTextColor(v.tv_activity_expired);
		refreshParams.put("underway", underway);
		refreshCurrentList(refreshUrl, refreshParams, 0, lv_mine_activity);*/
	}

	private void changeTextColor(TextView tv) {//机智的写法
		v.tv_store_has.setTextColor(getResources().getColor(R.color.app_grey5));
		v.tv_store_no.setTextColor(getResources().getColor(R.color.app_grey5));

		tv.setTextColor(getResources().getColor(R.color.app_green2));
	};

	@InjectPullRefresh
	private void call(int type) {
		// 这里的type来判断是否是下拉还是上拉
		switch (type) {
		case InjectView.PULL:
			if (list != null) {
				/*if (TextUtils.isEmpty(list.getNext_page_url())) {*/
				if (list.getNext_page_url()== "null"||TextUtils.isEmpty(list.getNext_page_url())) {
					Log.i("第三次刷新","第二次拿到的list中的next_page_url的值为null");
//					PullToRefreshManager.getInstance().onFooterRefreshComplete();
					CustomToast.show(activity, "到底啦！", "您只关注了以上的门店哦");
//					PullToRefreshManager.getInstance().footerUnable();
//					PullToRefreshManager.getInstance().headerUnable();
				} else {
					refreshParams = new LinkedHashMap<String, String>();
					refreshParams.put("underway", currentType);
					refreshCurrentList(list.getNext_page_url(), refreshParams, refreshKey, lv_mine_store);
					isPull  = true;
					/*adapter = null;*/
				}
			}
			break;
		case InjectView.DOWN:
			/*if (list != null) {
				LinkedHashMap<String, String> refreshParams = new LinkedHashMap<String, String>();
				refreshParams.put("underway", currentType);
				refreshCurrentList(refreshUrl, refreshParams, refreshKey,lv_mine_store);
				PullToRefreshManager.getInstance().footerEnable();
			}*/
			break;
		}
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

	@Override
	public void onPause() {
		/*dataList.clear();
		adapter = null;*/
		super.onPause();
	}
}
