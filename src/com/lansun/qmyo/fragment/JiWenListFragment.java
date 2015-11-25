package com.lansun.qmyo.fragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.services.core.ad;
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
import com.android.pc.ioc.view.listener.OnTextChanged;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.adapter.BankCardAdapter;
import com.lansun.qmyo.adapter.JiWenListAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.ActivityListData;
import com.lansun.qmyo.domain.BankCardData;
import com.lansun.qmyo.domain.BankCardList;
import com.lansun.qmyo.domain.HomePromote;
import com.lansun.qmyo.domain.HomePromoteData;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.event.entity.RefreshJiWenEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ListViewSwipeGesture;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.R;

public class JiWenListFragment extends BaseFragment {

	@InjectAll
	Views v;

	class Views {
		
		private RecyclingImageView iv_jiwen_boom;
		
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View ll_jiwen_search, ll_jiwen_search_icon;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView tv_jiwen_search;
		@InjectBinder(listeners = { OnTextChanged.class }, method = "changeText")
		private EditText et_home_search;
		
		
	}

	/*@InjectView(binders = { @InjectBinder(listeners = { OnItemClick.class }, method = "itemClick") }, down = true, pull = true)*/
	private MyListView lv_jiwen_list;
	private JiWenListAdapter adapter;
	private HomePromote list;
	private String service;
	private ArrayList<HashMap<String, Object>> dataList;

	private void itemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		HomePromoteData data = list.getData().get(arg2);
		PromoteDetailFragment fragment = new PromoteDetailFragment();
		Bundle args = new Bundle();
		args.putSerializable("promote", data);
		fragment.setArguments(args);
		FragmentEntity event = new FragmentEntity();
		event.setFragment(fragment);
		EventBus.getDefault().post(event);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_jiwen_list, null);
		Handler_Inject.injectFragment(this, rootView);
		PullToRefreshManager.getInstance().footerUnable();
		return rootView;
	}

	private void changeText(CharSequence s, int start, int before, int count) {
		if (TextUtils.isEmpty(s)) {
			v.tv_jiwen_search.setText(R.string.cancle);
			v.tv_jiwen_search.setTextColor(Color.parseColor("#939393"));
		} else {
			v.tv_jiwen_search.setText(R.string.search);
			v.tv_jiwen_search.setTextColor(getResources().getColor(
					R.color.app_green1));
		}
	}

	@InjectInit
	private void init() {
		
		((AnimationDrawable)v.iv_jiwen_boom.getDrawable()).start();
		
		/*refreshUrl = GlobalValue.URL_ARTICLE_ALL;
		refreshKey = 0;
		refreshParams = new LinkedHashMap<String, String>();
		refreshParams.put("site", getCurrentCity()[0]);
		refreshCurrentList(refreshUrl, refreshParams, refreshKey, lv_jiwen_list);*/
	}

	private void click(View view) {
		switch (view.getId()) {
		/*case R.id.tv_jiwen_search:// 极文搜索
			if (v.tv_jiwen_search.getText().equals(getString(R.string.search))) {
				String content = v.et_home_search.getText().toString().trim();
				try {
					refreshParams.put("query",
							URLEncoder.encode(content, "utf-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				dataList.clear();
				adapter = null;
				refreshCurrentList(refreshUrl, refreshParams, refreshKey,
						lv_jiwen_list);
			} else {
				back();
			}
			break;
		case R.id.ll_jiwen_search_icon:
			v.ll_jiwen_search_icon.setVisibility(View.GONE);
			v.ll_jiwen_search.setVisibility(View.VISIBLE);
			v.tv_jiwen_search.setVisibility(View.VISIBLE);
			break;*/
		}
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			endProgress();
			switch (r.getKey()) {
			case 0:
				list = Handler_Json.JsonToBean(HomePromote.class,
						r.getContentAsString());
				if (list.getData() != null) {
					dataList = new ArrayList<HashMap<String, Object>>();
					for (HomePromoteData data : list.getData()) {
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("iv_jiwen_item_head", data.getPhoto());
						map.put("iv_jiwen_item_type", data.getTags());
						map.put("tv_jiwen_item_type_desc", data.getTag());
						map.put("tv_jiwen_item_desc", data.getName());
						map.put("tv_jiwen_item_time", data.getTime());
						dataList.add(map);
					}
					if (adapter == null) {
						adapter = new JiWenListAdapter(lv_jiwen_list, dataList,
								R.layout.jiwen_list_item);
						lv_jiwen_list.setAdapter(adapter);
					} else {
						adapter.notifyDataSetChanged();
					}
					PullToRefreshManager.getInstance()
							.onHeaderRefreshComplete();
					PullToRefreshManager.getInstance()
							.onFooterRefreshComplete();
				} else {
					lv_jiwen_list.setAdapter(null);
				}

				break;
			}
		} else {

			progress_text.setText(R.string.net_error_refresh);
		}
		
		/*PullToRefreshManager.getInstance().onHeaderRefreshComplete();
		PullToRefreshManager.getInstance().onFooterRefreshComplete();*/
	}

	/*@InjectPullRefresh*/
	private void call(int type) {
		// 这里的type来判断是否是下拉还是上拉
		switch (type) {
		case InjectView.PULL:
			if (list != null) {
				if (TextUtils.isEmpty(list.getNext_page_url())) {
					PullToRefreshManager.getInstance()
							.onFooterRefreshComplete();
					CustomToast.show(activity, "加载进度", "目前所有内容都已经加载完成");
				} else {
					refreshCurrentList(list.getNext_page_url(), null, 0,
							lv_jiwen_list);
				}
			}
			break;
		case InjectView.DOWN:
			if (list != null) {
				refreshCurrentList(refreshUrl, null, 0, lv_jiwen_list);
			}
			break;
		}
	}

	@Override
	public void onPause() {
		adapter = null;
		super.onPause();
	}

	@Override
	public void onResume() {
		if (adapter != null) {
			adapter = null;
			dataList.clear();
		}
		super.onResume();
	}

}