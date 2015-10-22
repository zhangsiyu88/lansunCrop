package com.lansun.qmyo.fragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

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
import com.android.pc.ioc.view.listener.OnTextChanged;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.adapter.HomeListAdapter;
import com.lansun.qmyo.adapter.SearchAdapter;
import com.lansun.qmyo.domain.ActivityList;
import com.lansun.qmyo.domain.ActivityListData;
import com.lansun.qmyo.domain.Data;
import com.lansun.qmyo.domain.HomePromote;
import com.lansun.qmyo.domain.HomePromoteData;
import com.lansun.qmyo.domain.Intelligent;
import com.lansun.qmyo.domain.Service;
import com.lansun.qmyo.domain.ServiceDataItem;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ExpandTabView;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.view.ViewLeft;
import com.lansun.qmyo.view.ViewMiddle;
import com.lansun.qmyo.view.ViewRight;
import com.lansun.qmyo.R;

/**
 * 全局搜索内容界面
 * 
 * @author bhxx
 * 
 */
public class SearchJiWenFragment extends BaseFragment {

	@InjectAll
	Views v;

	@InjectView(binders = { @InjectBinder(listeners = { OnItemClick.class }, method = "contentClick") }, down = true, pull = true)
	private MyListView lv_jiwen_search_content;
	private ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
	private HomeListAdapter adapter;
	private String query;

	private HomePromote list;

	private String HODLER_TYPE;

	private View emptyView;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView search_tv_cancle;
		@InjectBinder(listeners = { OnTextChanged.class }, method = "changeText")
		private EditText et_home_search;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_search_jiwen, null);
		activity.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
						| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		v.et_home_search.setHint(R.string.please_enter_search_brand);
		refreshUrl = GlobalValue.URL_ARTICLE_ALL;
		if (getArguments() != null) {
			list = (HomePromote) getArguments().getSerializable("promote");
			query = getArguments().getString("query");
			v.et_home_search.setText(query);
			HODLER_TYPE = getArguments().getString("HODLER_TYPE");
			filldata();
		} else {
			startSearch(v.et_home_search.getText().toString().trim());
		}
		emptyView = inflater.inflate(R.layout.activity_search_empty, null);
		emptyView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		emptyView.setVisibility(View.GONE);
		((ViewGroup) lv_jiwen_search_content.getParent()).addView(emptyView);
	}

	private void filldata() {
		dataList.clear();
		if (list.getData() != null) {
			ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
			for (HomePromoteData data : list.getData()) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("iv_home_item_head", data.getPhoto());
				map.put("tv_home_item_title", data.getTag());
				map.put("tv_home_item_desc", data.getName());
				dataList.add(map);
			}
			if (adapter == null) {
				adapter = new HomeListAdapter(lv_jiwen_search_content,
						dataList, R.layout.home_item);
				lv_jiwen_search_content.setAdapter(adapter);
			} else {
				adapter.notifyDataSetChanged();
			}

		} else {
			lv_jiwen_search_content.setAdapter(null);
		}
	}

	/**
	 * 搜索内容监听
	 * 
	 * @param s
	 * @param start
	 * @param before
	 * @param count
	 */
	private void changeText(CharSequence s, int start, int before, int count) {
		if (TextUtils.isEmpty(s)) {
			v.search_tv_cancle.setText(R.string.cancle);
			v.search_tv_cancle.setTextColor(Color.parseColor("#939393"));
		} else {
			v.search_tv_cancle.setText(R.string.search);
			v.search_tv_cancle.setTextColor(getResources().getColor(
					R.color.app_green1));
		}
	}

	private void contentClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		PromoteDetailFragment fragment = new PromoteDetailFragment();
		Bundle args = new Bundle();
		args.putSerializable("promote", list.getData().get(arg2));
		fragment.setArguments(args);
		FragmentEntity event = new FragmentEntity();
		event.setFragment(fragment);
		EventBus.getDefault().post(event);
	}

	private void click(View view) {
		switch (view.getId()) {
		case R.id.search_tv_cancle:
			if (getString(R.string.cancle).equals(v.search_tv_cancle.getText())) {
				InputMethodManager imm = (InputMethodManager) activity
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0); // 强制隐藏键盘
				back();
			} else {
				adapter = null;
				dataList.clear();
				query = v.et_home_search.getText().toString();
				startSearch(v.et_home_search.getText().toString().trim());
			}
			break;
		}
	}

	/**
	 * 开启搜索
	 * 
	 * @param string
	 */
	private void startSearch(String string) {
		refreshParams = new LinkedHashMap<>();
		refreshParams.put("service", HODLER_TYPE);
		refreshParams.put("site", getSelectCity()[0]);
		try {
			refreshParams.put("query", URLEncoder.encode(string, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		refreshKey = 0;
		refreshCurrentList(refreshUrl, refreshParams, refreshKey,
				lv_jiwen_search_content);

		LinkedHashMap<String, String> jiwenParams = new LinkedHashMap<String, String>();
		jiwenParams.put("site", getSelectCity()[0]);
		jiwenParams.put("service", HODLER_TYPE);
		try {
			jiwenParams.put("query", URLEncoder.encode(query, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		refreshCurrentList(GlobalValue.URL_ARTICLE_ALL, jiwenParams,
				refreshKey, null);
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		endProgress();
		if (r.getStatus() == FastHttp.result_ok) {
			String name;
			switch (r.getKey()) {

			case 0:
				list = Handler_Json.JsonToBean(HomePromote.class,
						r.getContentAsString());
				filldata();
				endProgress();
				break;

			}
		}
		PullToRefreshManager.getInstance().onHeaderRefreshComplete();
		PullToRefreshManager.getInstance().onFooterRefreshComplete();

	}

	/**
	 * 
	 * @param type
	 */
	@InjectPullRefresh
	public void call(int type) {
		// 这里的type来判断是否是下拉还是上拉
		switch (type) {
		case InjectView.PULL:
			if (list != null) {
				if (TextUtils.isEmpty(list.getNext_page_url())) {
					PullToRefreshManager.getInstance()
							.onFooterRefreshComplete();
					CustomToast.show(activity, "加载进度", "目前所有内容都已经加载完成");
				} else {
					refreshParams = new LinkedHashMap<>();
					refreshParams.put("site", getSelectCity()[0]);
					refreshParams.put("service", HODLER_TYPE);
					try {
						refreshParams.put("query",
								URLEncoder.encode(query, "utf-8"));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					refreshCurrentList(list.getNext_page_url(), refreshParams,
							0, lv_jiwen_search_content);
				}
			}
			break;
		case InjectView.DOWN:
			if (list != null) {
				refreshParams = new LinkedHashMap<>();
				refreshParams.put("site", getSelectCity()[0]);
				refreshParams.put("service", HODLER_TYPE);
				try {
					refreshParams.put("query",
							URLEncoder.encode(query, "utf-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				refreshCurrentList(refreshUrl, refreshParams, refreshKey,
						lv_jiwen_search_content);
			}
			break;
		}

	}

}
