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
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ListViewSwipeGesture;
import com.lansun.qmyo.view.MyListView;
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
public class MineActivityFragment extends BaseFragment {

	private String underway = "1";
	public boolean isPullChanged = true;

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
	private MyListView lv_mine_activity;

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
		ListViewSwipeGesture touchListener = new ListViewSwipeGesture(
				lv_mine_activity, swipeListener, getActivity());
		refreshParams.put("underway", underway);
		touchListener.SwipeType = ListViewSwipeGesture.Dismiss;
		lv_mine_activity.setOnTouchListener(touchListener);
		
		refreshUrl = GlobalValue.URL_USER_ACTIVITY + "?";
		//去拿刷新出来的数据
		refreshCurrentList(refreshUrl, refreshParams, 0, lv_mine_activity);
	}

	private int deletePosition;
	ListViewSwipeGesture.TouchCallbacks swipeListener = new ListViewSwipeGesture.TouchCallbacks() {

		@Override
		public void FullSwipeListView(int position) {
		}

		@Override
		public void HalfSwipeListView(final int position) {
			deletePosition = position;
			InternetConfig config = new InternetConfig();
			config.setKey(1);
			HashMap<String, Object> head = new HashMap<>();
			
			head.put("Authorization","Bearer " + App.app.getData("access_token"));
			
			config.setHead(head);
			config.setRequest_type(InternetConfig.request_delete);
			config.setMethod("DELETE");
			
			/*FastHttpHander.ajax(String.format(
					GlobalValue.URL_USER_ACTIVITY_DELETE, dataList
							.get(position).get("activityId").toString(),
					dataList.get(position).get("shopId").toString()), config,
					MineActivityFragment.this);*/
			
			
			//TODO  注意下面拼接的url中的接口域名记得替换
		/*	FastHttpHander.ajax("http://appapi.qmyo.org/activity/"+dataList
					.get(position).get("activityId").toString()+"?shop_id="+dataList.get(position).get("shopId").toString(),
			config, this);*/
			
			HttpUtils httpUtils = new HttpUtils();
			RequestCallBack<String> requestCallBack = new RequestCallBack<String>() {

				@Override
				public void onFailure(HttpException arg0, String arg1) {
					
				}

				@Override
				public void onSuccess(ResponseInfo<String> arg0) {
					if ("true".equals(arg0.result.toString())) {
							dataList.remove(deletePosition);
							adapter.notifyDataSetChanged();
							CustomToast.show(activity, getString(R.string.tip),
									getString(R.string.delete_success));
						} else {
							CustomToast.show(activity, getString(R.string.tip),
									getString(R.string.delete_faild));
						}
				}

				
			};
			RequestParams requestParams = new RequestParams();
			requestParams.addHeader("Authorization", "Bearer" + App.app.getData("access_token"));
			
			//TODO  注意下面拼接的url中的接口域名记得替换
			httpUtils.send(HttpMethod.DELETE, 
					"http://appapi.qmyo.org/activity/"+dataList
					.get(position).get("activityId").toString()+"?shop_id="+dataList.get(position).get("shopId").toString(),
					requestParams,requestCallBack );
			
			
		}

		@Override
		public void OnClickListView(int position) {
			ActivityDetailFragment fragment = new ActivityDetailFragment();
			Bundle args = new Bundle();
			args.putString("activityId",
					dataList.get(position).get("activityId").toString());
			args.putString("shopId", dataList.get(position).get("shopId")
					.toString());
			fragment.setArguments(args);
			FragmentEntity event = new FragmentEntity();
			event.setFragment(fragment);
			EventBus.getDefault().post(event);
		}

		@Override
		public void LoadDataForScroll(int count) {

		}

		@Override
		public void onDismiss(ListView listView, int[] reverseSortedPositions) {

		}

	};

	private ArrayList<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();

	private SearchAdapter adapter;

	private ActivityList list;

	private void click(View view) {
		dataList.clear();
		adapter = null;
		v.rl_no_postdelay_activity.setVisibility(View.GONE);
		switch (view.getId()) {
		case R.id.tv_activity_doing:
			underway = "1";
			changeTextColor(v.tv_activity_doing);
			refreshParams.put("underway", underway);
			refreshCurrentList(refreshUrl, refreshParams, 0, lv_mine_activity);
			break;
		case R.id.tv_activity_expired:
			underway = "0";
			changeTextColor(v.tv_activity_expired);
			refreshParams.put("underway", underway);
			refreshCurrentList(refreshUrl, refreshParams, 0, lv_mine_activity);
			break;
		}
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
		dataList.clear();
		adapter = null;
		super.onPause();
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			endProgress();
			switch (r.getKey()) {
			case 0:
				if(isPullChanged){//上拉加载不清掉list
					
				}else{
					dataList.clear();//dataList 需要在这里清理一下，不然会重复添加内容_byZdy
					isPullChanged = true;
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
					if (adapter == null) {
						adapter = new SearchAdapter(lv_mine_activity, dataList,
								R.layout.activity_search_item);
						lv_mine_activity.setAdapter(adapter);
					} else {
						adapter.notifyDataSetChanged();
					}
					PullToRefreshManager.getInstance().footerEnable();
					PullToRefreshManager.getInstance().onHeaderRefreshComplete();
					PullToRefreshManager.getInstance().onFooterRefreshComplete();
					
				} else {
					v.rl_no_postdelay_activity.setVisibility(View.VISIBLE);
					
					lv_mine_activity.setAdapter(null);
				}
				break;
				
			case 1:
				if ("true".equals(r.getContentAsString())) {
					dataList.remove(deletePosition);
					adapter.notifyDataSetChanged();
					CustomToast.show(activity, getString(R.string.tip),
							getString(R.string.delete_success));
				} else {
					CustomToast.show(activity, getString(R.string.tip),
							getString(R.string.delete_faild));
				}
				break;
			}
		} else {

			progress_text.setText(R.string.net_error_check_net);
		}
		PullToRefreshManager.getInstance().footerEnable();
		PullToRefreshManager.getInstance().onHeaderRefreshComplete();
		PullToRefreshManager.getInstance().onFooterRefreshComplete();
	}

	@InjectPullRefresh
	private void call(int type) {
		// 这里的type来判断是否是下拉还是上拉
		switch (type) {
		case InjectView.PULL:
			if (list != null) {
				
				/*
				 * 从代码习惯上来说，服务器返回的是"null"字符串，我认为是不应该的，空的话就应该是空串 ("")
				 */
				if (list.getNext_page_url()=="null"){
					
					Log.i("Tag", "既然list.getNext_page_url()为空，走这儿啊！");
					
					CustomToast.show(activity, "到底啦！", "您只收藏了以上活动哦");
					
					PullToRefreshManager.getInstance().onFooterRefreshComplete();
					PullToRefreshManager.getInstance().footerUnable();
				} else {
					
					refreshParams.put("underway", underway);
					refreshCurrentList(list.getNext_page_url(), refreshParams,0, lv_mine_activity);
					
				}
			}
			break;
		case InjectView.DOWN:
			if (list != null) {
				refreshParams.put("underway", underway);
				isPullChanged = false;
				
				//再次刷新
				refreshCurrentList(refreshUrl, refreshParams, 0,lv_mine_activity);
			}
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
			time = minutes + "分" ;
		}
		if (hours != 0) {
			time = hours + "小时"+ time;
		}
		return time + "前";
	}
}
