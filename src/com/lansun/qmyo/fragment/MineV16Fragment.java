package com.lansun.qmyo.fragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.amap.api.mapcore2d.ev;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
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
import com.android.pc.util.Handler_Ui;
import com.lansun.qmyo.adapter.CommentActivityAdapter;
import com.lansun.qmyo.adapter.JiWenListAdapter;
import com.lansun.qmyo.adapter.MineCommentAdapter;
import com.lansun.qmyo.adapter.MineV16Adapter;
import com.lansun.qmyo.adapter.SearchAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.Activity;
import com.lansun.qmyo.domain.CommentList;
import com.lansun.qmyo.domain.CommentListData;
import com.lansun.qmyo.domain.HistoryActivity;
import com.lansun.qmyo.domain.HomePromote;
import com.lansun.qmyo.domain.HomePromoteData;
import com.lansun.qmyo.domain.StoreInfo;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.event.entity.ReplyEntity;
import com.lansun.qmyo.fragment.MessageCenterFragment.Views;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ListViewSwipeGesture;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.R;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

/**
 * 我的V16收藏
 * 
 * @author bhxx
 * 
 */
public class MineV16Fragment extends BaseFragment {

	private String currentUrl = GlobalValue.URL_USER_ARTICLE;

	@InjectAll
	Views v;

	private HomePromote list;

	private MineV16Adapter adapter;
	@InjectView(down = true, pull = true)
	private MyListView lv_mine_v16;

	private ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

	private int replyId;

	private int replyUserId;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View fl_comments_right_iv, tv_activity_shared;
		private TextView tv_activity_title;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView tv_mine_v16_jiwen, tv_mine_v16_bd;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_mine_v16, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		v.fl_comments_right_iv.setVisibility(View.GONE);
		initTitle(v.tv_activity_title, R.string.v16_collection, null, 0);

		ListViewSwipeGesture touchListener = new ListViewSwipeGesture(
				lv_mine_v16, swipeListener, getActivity());
		touchListener.SwipeType = ListViewSwipeGesture.Dismiss;
		lv_mine_v16.setOnTouchListener(touchListener);

		refreshCurrentList(currentUrl, null, 0, lv_mine_v16);
		EventBus.getDefault().register(this);
	}

	ListViewSwipeGesture.TouchCallbacks swipeListener = new ListViewSwipeGesture.TouchCallbacks() {
		@Override
		public void FullSwipeListView(int position) {
		}

		@Override
		public void HalfSwipeListView(final int position) {// 删除操作
			HashMap<String, String> map = dataList.get(position);
			// String id = map.get("id");
			dataList.remove(position);
			InternetConfig config = new InternetConfig();
			config.setKey(1);
			HashMap<String, Object> head = new HashMap<>();
			head.put("Authorization",
					"Bearer " + App.app.getData("access_token"));
			config.setHead(head);
			config.setRequest_type(InternetConfig.request_delete);
			config.setMethod("DELETE");
			// FastHttpHander.ajax(GlobalValue.URL_BANKCARD_DELETE + id, config,
			// MineBankcardFragment.this);
			adapter.notifyDataSetChanged();
		}

		@Override
		public void OnClickListView(int position) {
			PromoteDetailFragment fragment = new PromoteDetailFragment();
			Bundle args = new Bundle();
			args.putSerializable("promote", list.getData().get(position));
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

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			endProgress();
			switch (r.getKey()) {
			case 0:
				list = Handler_Json.JsonToBean(HomePromote.class,
						r.getContentAsString());
				if (list.getData() != null) {
					for (HomePromoteData data : list.getData()) {
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("iv_mine_v16_head", data.getPhoto());
						map.put("tv_mine_v16_name", data.getName());
						map.put("tv_mine_v16_tag", data.getTags().get(0));
						String historyTime = "";
						if ((System.currentTimeMillis() - Handler_Time
								.getInstance(data.getTime()).getTimeInMillis())
								/ (1000 * 24 * 60 * 60) > 1) {
							Handler_Time instance = Handler_Time
									.getInstance(data.getTime());
							historyTime = instance.getYear() + "-"
									+ instance.getMonth() + "-"
									+ instance.getDay();
						} else {
							historyTime = getHistoryTime(System
									.currentTimeMillis()
									- Handler_Time.getInstance(data.getTime())
											.getTimeInMillis());
						}
						map.put("tv_mine_v16_time", historyTime);
						map.put("url", data.getUrl());
						dataList.add(map);
					}
				} else {
					lv_mine_v16.setAdapter(null);
				}
				if (adapter == null) {
					adapter = new MineV16Adapter(lv_mine_v16, dataList,
							R.layout.mine_comments_v16_item);
					lv_mine_v16.setAdapter(adapter);
				} else {
					adapter.notifyDataSetChanged();
				}

				break;
			}
		} else {

			progress_text.setText(R.string.net_error_refresh);
		}
		PullToRefreshManager.getInstance().onHeaderRefreshComplete();
		PullToRefreshManager.getInstance().onFooterRefreshComplete();
	}

	private void changeTextColor(TextView tv) {
		v.tv_mine_v16_bd.setTextColor(getResources()
				.getColor(R.color.app_grey5));
		v.tv_mine_v16_jiwen.setTextColor(getResources().getColor(
				R.color.app_grey5));
		tv.setTextColor(getResources().getColor(R.color.app_green2));
	};

	private void click(View view) {
		switch (view.getId()) {
		case R.id.tv_mine_v16_jiwen:// 极文列表
			list = null;
			currentUrl = GlobalValue.URL_USER_COMMENTS;
			changeTextColor(v.tv_mine_v16_jiwen);
			refreshCurrentList(currentUrl, null, 0, lv_mine_v16);
			break;
		case R.id.tv_mine_v16_bd:
			// list = null;
			// currentUrl = GlobalValue.URL_USER_RESPONDS;
			// changeTextColor(v.tv_mine_v16_bd);
			// refreshCurrentList(currentUrl, null, 0, lv_mine_v16);
			break;
		}
	}

	/**
	 * 刷新
	 * 
	 * @param type
	 */
	@InjectPullRefresh
	private void call(int type) {
		switch (type) {
		case InjectView.PULL:
			if (list != null) {
				if (TextUtils.isEmpty(list.getNext_page_url())) {
					PullToRefreshManager.getInstance()
							.onFooterRefreshComplete();
					CustomToast.show(activity, "加载进度", "目前所有内容都已经加载完成");
				} else {
					refreshCurrentList(list.getNext_page_url(), null, 0,
							lv_mine_v16);
				}
			}
			break;
		case InjectView.DOWN:
			refreshCurrentList(currentUrl, null, 0, lv_mine_v16);
			break;
		}
	}

	@Override
	public void onPause() {
		adapter = null;
		dataList.clear();
		EventBus.getDefault().unregister(this);
		super.onPause();
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
}
