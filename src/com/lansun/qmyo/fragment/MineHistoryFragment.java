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
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ListViewSwipeGesture;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.R;

import android.content.DialogInterface;
import android.database.CursorJoiner.Result;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 我的浏览记录
 * 
 * @author bhxx
 * 
 */
public class MineHistoryFragment extends BaseFragment {

	@InjectAll
	Views v;
	@InjectView(binders = { @InjectBinder(listeners = { OnItemClick.class }, method = "itemClick") }, down = true, pull = true)
	private MyListView lv_mine_history_list;
	private HistoryActivity activityList;
	private HistoryActivity storeList;
	private ArrayList<HashMap<String, String>> storeDataList = new ArrayList<HashMap<String, String>>();
	private ArrayList<HashMap<String, Object>> activityDataList = new ArrayList<>();
	private ArrayList<HashMap<String, String>> v16DataList = new ArrayList<>();
	private StoreAdapter storeAdapter;
	private SearchAdapter activityAdapter;
	private HistoryActivity v16list;
	private MineV16Adapter v16adapter;

	class Views {

		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView tv_mine_history_edit, tv_mine_history_activity,
				tv_mine_history_store, tv_mine_history_v16, tv_activity_shared,
				tv_activity_title;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View fl_comments_right_iv;

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_mine_history, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		v.fl_comments_right_iv.setVisibility(View.GONE);
		v.tv_activity_shared.setText(R.string.all_clear);
		initTitle(v.tv_activity_title, R.string.mine_history, null, 0);

		refreshUrl = GlobalValue.URL_USER_ACTIVITYBROWSES;
		refreshKey = 0;
		refreshCurrentList(refreshUrl, null, refreshKey, lv_mine_history_list);
	}

	private void click(View view) {
		activityAdapter = null;
		storeAdapter = null;
		v16adapter = null;
		activityDataList.clear();
		storeDataList.clear();
		v16DataList.clear();

		switch (view.getId()) {
		case R.id.tv_mine_history_activity:// 活动
			refreshKey = 0;
			activityAdapter = null;
			lv_mine_history_list.setAdapter(null);
			changeTextColor(v.tv_mine_history_activity);
			refreshUrl = GlobalValue.URL_USER_ACTIVITYBROWSES;
			refreshCurrentList(refreshUrl, null, refreshKey,
					lv_mine_history_list);
			break;
		case R.id.tv_mine_history_store:// 门店

			refreshKey = 1;

			storeAdapter = null;
			lv_mine_history_list.setAdapter(null);
			refreshUrl = GlobalValue.URL_USER_SHOPBROWSES;
			changeTextColor(v.tv_mine_history_store);
			refreshCurrentList(refreshUrl, null, refreshKey,
					lv_mine_history_list);
			break;
		case R.id.tv_mine_history_v16:// V16

			refreshKey = 3;
			refreshUrl = GlobalValue.URL_USER_ARTICLEBROWSES;
			changeTextColor(v.tv_mine_history_v16);
			refreshCurrentList(refreshUrl, null, refreshKey,
					lv_mine_history_list);
			break;
		case R.id.tv_activity_shared:// V16
			DialogUtil.createTipAlertDialog(activity, R.string.all_clear_tip,
					new TipAlertDialogCallBack() {

						@Override
						public void onPositiveButtonClick(
								DialogInterface dialog, int which) {
							dialog.dismiss();
							InternetConfig config = new InternetConfig();
							config.setKey(2);
							HashMap<String, Object> head = new HashMap<>();
							head.put("Authorization",
									"Bearer " + App.app.getData("access_token"));
							config.setHead(head);
							config.setRequest_type(InternetConfig.request_delete);
							config.setMethod("DELETE");
							FastHttpHander.ajax(
									GlobalValue.URL_USER_DELETE_BROWSES,
									config, MineHistoryFragment.this);
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
		v.tv_mine_history_v16.setTextColor(getResources().getColor(
				R.color.app_grey5));
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
			activityDataList.clear();
			storeDataList.clear();
			v16DataList.clear();
			switch (r.getKey()) {
			case 0:
				activityList = Handler_Json.JsonToBean(HistoryActivity.class,
						r.getContentAsString());
				for (BrowseData data : activityList.getData()) {
					HashMap<String, Object> map = new HashMap<>();
					map.put("tv_search_activity_name", data.getShop().getName());
					long time = Handler_Time.getInstance(
							data.getBrowse().getTime()).getTimeInMillis();
					String historyTime = "";
					if ((System.currentTimeMillis() - time)
							/ (1000 * 24 * 60 * 60) > 1) {
						Handler_Time instance = Handler_Time.getInstance(data
								.getBrowse().getTime());
						historyTime = instance.getYear() + "-"
								+ instance.getMonth() + "-" + instance.getDay();
					} else {
						historyTime = getHistoryTime(System.currentTimeMillis()
								- time);
					}
					map.put("tv_search_activity_distance", historyTime);
					map.put("tv_search_activity_desc", data.getActivity()
							.getName());
					map.put("iv_search_activity_head", data.getActivity()
							.getPhoto());
					map.put("tv_search_tag", data.getActivity().getTag());
					map.put("icons", data.getActivity().getCategory());
					activityDataList.add(map);
				}
				if (activityAdapter == null) {
					activityAdapter = new SearchAdapter(lv_mine_history_list,
							activityDataList, R.layout.activity_search_item);
					lv_mine_history_list.setAdapter(activityAdapter);
				} else {
					activityAdapter.notifyDataSetChanged();
				}
				PullToRefreshManager.getInstance().onHeaderRefreshComplete();
				PullToRefreshManager.getInstance().onFooterRefreshComplete();
				break;
			case 1:
				storeList = Handler_Json.JsonToBean(HistoryActivity.class,
						r.getContentAsString());
				for (BrowseData data : storeList.getData()) {
					HashMap<String, String> map = new HashMap<String, String>();
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
				if (storeAdapter == null) {
					storeAdapter = new StoreAdapter(lv_mine_history_list,
							storeDataList, R.layout.activity_store_item);
					lv_mine_history_list.setAdapter(storeAdapter);
				} else {
					storeAdapter.notifyDataSetChanged();
				}

				break;
			case 2:
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
				break;
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
		PullToRefreshManager.getInstance().onHeaderRefreshComplete();
		PullToRefreshManager.getInstance().onFooterRefreshComplete();
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
					if (TextUtils.isEmpty(activityList.getNext_page_url())) {
						PullToRefreshManager.getInstance()
								.onFooterRefreshComplete();
						CustomToast.show(activity, "加载进度", "目前所有内容都已经加载完成");
						return;
					}
					refreshUrl = activityList.getNext_page_url();
				} else if (refreshKey == 1) {
					if (TextUtils.isEmpty(storeList.getNext_page_url())) {
						PullToRefreshManager.getInstance()
								.onFooterRefreshComplete();
						CustomToast.show(activity, "加载进度", "目前所有内容都已经加载完成");
						return;
					}
					refreshUrl = storeList.getNext_page_url();
				} else {
					if (TextUtils.isEmpty(v16list.getNext_page_url())) {
						PullToRefreshManager.getInstance()
								.onFooterRefreshComplete();
						CustomToast.show(activity, "加载进度", "目前所有内容都已经加载完成");
						return;
					}
					refreshUrl = v16list.getNext_page_url();
				}
				refreshCurrentList(refreshUrl, null, refreshKey,
						lv_mine_history_list);
			}
			break;
		case InjectView.DOWN:
			if (refreshKey == 0) {
				refreshUrl = GlobalValue.URL_USER_ACTIVITYBROWSES;
			} else if (refreshKey == 1) {
				refreshUrl = GlobalValue.URL_USER_SHOPBROWSES;
			} else {
				refreshUrl = GlobalValue.URL_USER_ARTICLEBROWSES;
			}
			refreshCurrentList(refreshUrl, null, refreshKey,
					lv_mine_history_list);
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
		if (refreshKey == 0) {
			fragment = new ActivityDetailFragment();
			args.putString("shopId", activityList.getData().get(arg2).getShop()
					.getId()
					+ "");
			args.putString("activityId", activityList.getData().get(arg2)
					.getActivity().getId()
					+ "");
		}
		if (refreshKey == 1) {
			fragment = new StoreDetailFragment();
			args.putString("shopId", activityList.getData().get(arg2).getShop()
					.getId()
					+ "");
		} else {
			fragment = new PromoteDetailFragment();
			args.putSerializable("promote", v16list.getData().get(arg2)
					.getArticle());
		}
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
}
