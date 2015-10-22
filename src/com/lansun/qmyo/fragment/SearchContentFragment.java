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
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
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
import com.android.pc.ioc.view.listener.OnTextChanged;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.adapter.SearchAdapter;
import com.lansun.qmyo.domain.ActivityList;
import com.lansun.qmyo.domain.ActivityListData;
import com.lansun.qmyo.domain.Data;
import com.lansun.qmyo.domain.HomePromote;
import com.lansun.qmyo.domain.Intelligent;
import com.lansun.qmyo.domain.Service;
import com.lansun.qmyo.domain.ServiceDataItem;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.AnimUtils;
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
public class SearchContentFragment extends BaseFragment {

	private String HODLER_TYPE = "000000";
	/**
	 * 全部服务信息
	 */
	private Service nearService;

	/**
	 * 智能排序
	 */
	private Intelligent intelligent;

	/**
	 * 全部的信息
	 */
	private Service AllService;

	/**
	 * 筛选
	 */
	private Intelligent sxintelligent;

	private String position = "nearby";

	private String intelligentStr;
	private String type;

	private ViewLeft viewLeft2;
	private ViewMiddle viewMiddle;
	private ViewLeft viewLeft;
	private ViewRight viewRight;

	private HashMap<Integer, View> mViewArray = new HashMap<Integer, View>();
	private HashMap<Integer, String> holder_button = new HashMap<Integer, String>();
	@InjectAll
	Views v;

	@InjectView(binders = { @InjectBinder(listeners = { OnItemClick.class }, method = "contentClick") }, down = true, pull = true)
	private MyListView lv_search_content;
	private ActivityList list;
	private ArrayList<HashMap<String, Object>> dataList = new ArrayList<>();
	private SearchAdapter adapter;
	private String query;

	private TextView tv_jiwen_about;
	private TextView tv_look_now;
	private HomePromote promote;
	private StringBuffer typeSb = new StringBuffer();
	private View emptyView;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView search_tv_cancle;
		@InjectBinder(listeners = { OnTextChanged.class }, method = "changeText")
		private EditText et_home_search;
		private ExpandTabView expandtab_view;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater
				.inflate(R.layout.activity_search_content, null);
		activity.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
						| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		v.et_home_search.setHint(R.string.please_enter_search_brand);
		holder_button.clear();
		mViewArray.clear();
		
		v.expandtab_view.removeAllViews();//ExpandTab终于上场了！
		
		View head = inflater.inflate(R.layout.activity_search_banner, null);
		lv_search_content.addHeaderView(head, null, true);
		tv_jiwen_about = (TextView) head.findViewById(R.id.tv_jiwen_about);
		tv_look_now = (TextView) head.findViewById(R.id.tv_look_now);
		tv_look_now.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				SearchJiWenFragment fragment = new SearchJiWenFragment();
				Bundle args = new Bundle();
				args.putSerializable("promote", promote);
				args.putString("HODLER_TYPE", HODLER_TYPE);
				args.putString("query", query);
				fragment.setArguments(args);
				FragmentEntity event = new FragmentEntity();
				event.setFragment(fragment);
				EventBus.getDefault().post(event);
			}
		});

		if (getArguments() != null) {
			query = getArguments().getString("query");
			v.et_home_search.setText(query);
		}

		initData();
		emptyView = inflater.inflate(R.layout.activity_search_empty, null);
		emptyView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		emptyView.setVisibility(View.GONE);
		((ViewGroup) lv_search_content.getParent()).addView(emptyView);
		AnimUtils.startTopInAnim(activity, v.expandtab_view, 800, null);
		AnimUtils.right2Left(activity, lv_search_content);
	}

	private void contentClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3){
		HashMap<String, Object> data = dataList.get(arg2 - 1);
		String activityId = data.get("activityId").toString();
		String shopId = data.get("shopId").toString();
		ActivityDetailFragment fragment = new ActivityDetailFragment();
		Bundle args = new Bundle();
		args.putString("activityId", activityId);
		args.putString("shopId", shopId);
		fragment.setArguments(args);
		FragmentEntity event = new FragmentEntity();
		event.setFragment(fragment);
		EventBus.getDefault().post(event);
	}

	private void click(View view) {
		switch (view.getId()) {
		case R.id.search_tv_cancle:
			if (getString(R.string.cancle).equals(v.search_tv_cancle.getText())) {
				// startSearchOutAnim(activity, this.v.ll_search_top_menu, 500);
				
				
				InputMethodManager imm = (InputMethodManager) activity
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0); // 强制隐藏键盘
				
				
				back();
			} else {
				adapter = null;
				dataList.clear();
				query = v.et_home_search.getText().toString().trim();
				startSearch();
			}
			break;
		}
	}

	private void initData() {

		// 服务板块
		InternetConfig config = new InternetConfig();
		config.setKey(0);
		FastHttpHander.ajaxGet(GlobalValue.URL_SEARCH_HOLDER_SERVICE + "00000",
				config, this);

		// 附近 固定
		InternetConfig config1 = new InternetConfig();
		config1.setKey(1);
		FastHttpHander
				.ajaxGet(GlobalValue.URL_SEARCH_HOLDER_DISTRICT + "00000",
						config1, this);

		// 智能排序
		InternetConfig config2 = new InternetConfig();
		config2.setKey(2);
		FastHttpHander.ajaxGet(GlobalValue.URL_SEARCH_HOLDER_INTELLIGENT,
				config2, this);

		// 筛选
		InternetConfig config3 = new InternetConfig();
		config3.setKey(3);
		FastHttpHander.ajaxGet(GlobalValue.URL_SEARCH_HOLDER_SCREENING,
				config3, this);

		startSearch();

	}

	private void onRefresh(View view, String showText) {

		v.expandtab_view.onPressBack();
		int position = getPositon(view);
		if (position >= 0
				&& !v.expandtab_view.getTitle(position).equals(showText)) {
			v.expandtab_view.setTitle(showText, position);
		}
	}

	private int getPositon(View tView) {
		for (int i = 0; i < mViewArray.size(); i++) {
			if (mViewArray.get(i) == tView) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void onPause() {
		v.expandtab_view.onPressBack();
		adapter = null;
		dataList.clear();
		super.onPause();
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

	/**
	 * 开启搜索
	 * 
	 * @param string
	 */
	private void startSearch() {
		// AnimUtils.startTopInAnim(activity, v.expandtab_view, 800, null);
		// AnimUtils.right2Left(activity, lv_search_content);

		refreshParams = new LinkedHashMap<>();
		if (getCurrentCity()[0].equals(getSelectCity()[0])) {
			// refreshParams.put("location", GlobalValue.gps.toString());
			refreshParams.put("location", "31.293688,121.524448");
		}
		refreshParams.put("location", "31.293688,121.524448");
		refreshParams.put("service", HODLER_TYPE);
		// refreshParams.put("location", GlobalValue.gps.toString());
		try {
			refreshParams.put("poistion", URLEncoder.encode(position, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!TextUtils.isEmpty(intelligentStr)) {
			refreshParams.put("intelligent", intelligentStr);
		}
		refreshParams.put("site", getSelectCity()[0]);
		try {
			refreshParams.put("query", URLEncoder.encode(query, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (typeSb.length() > 1) {
			refreshParams.put("type", typeSb.substring(0, typeSb.length() - 1)
					.toString());
		}
		refreshUrl = GlobalValue.URL_ALL_ACTIVITY;
		refreshKey = 4;
		refreshCurrentList(refreshUrl, refreshParams, refreshKey,
				lv_search_content);

		LinkedHashMap<String, String> jiwenParams = new LinkedHashMap<String, String>();
		jiwenParams.put("site", getSelectCity()[0]);
		jiwenParams.put("service", HODLER_TYPE);
		try {
			jiwenParams.put("query", URLEncoder.encode(query, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		refreshCurrentList(GlobalValue.URL_ARTICLE_ALL, jiwenParams, 5, null);
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			String name;
			switch (r.getKey()) {
			case 0:// 全部
				viewLeft = new ViewLeft(activity);
				AllService = Handler_Json.JsonToBean(Service.class,
						r.getContentAsString());
				name = AllService.getName();
				if (name == null) {
					name = AllService.getData().get(0).getName();
				}
				ArrayList<String> allGroup = new ArrayList<String>();
				SparseArray<LinkedList<String>> allChild = new SparseArray<LinkedList<String>>();
				for (int j = 0; j < AllService.getData().size(); j++) {
					LinkedList<String> chind = new LinkedList<String>();
					allGroup.add(AllService.getData().get(j).getName());
					ArrayList<ServiceDataItem> items = AllService.getData()
							.get(j).getItems();
					if (items != null) {
						for (ServiceDataItem item : items) {
							chind.add(item.getName());
						}
					}
					allChild.put(j, chind);
				}
				holder_button.put(0, name);
				viewLeft.setGroups(allGroup);
				viewLeft.setChildren(allChild);
				mViewArray.put(0, viewLeft);
				break;
			case 1:// 附近
				viewLeft2 = new ViewLeft(activity);
				nearService = Handler_Json.JsonToBean(Service.class,
						r.getContentAsString());
				name = nearService.getName();
				if (name == null) {
					name = nearService.getData().get(0).getName();
				}
				ArrayList<String> nearGroup = new ArrayList<String>();
				SparseArray<LinkedList<String>> allNearChild = new SparseArray<LinkedList<String>>();
				for (int j = 0; j < nearService.getData().size(); j++) {
					LinkedList<String> chind = new LinkedList<String>();
					nearGroup.add(nearService.getData().get(j).getName());
					ArrayList<ServiceDataItem> items = nearService.getData()
							.get(j).getItems();
					if (items != null) {
						for (ServiceDataItem item : items) {
							chind.add(item.getName());
						}
					}
					allNearChild.put(j, chind);
				}
				holder_button.put(1, name);
				viewLeft2.setGroups(nearGroup);
				viewLeft2.setChildren(allNearChild);
				mViewArray.put(1, viewLeft2);
				break;
			case 2:// 智能排序
				viewMiddle = new ViewMiddle(activity);
				intelligent = Handler_Json.JsonToBean(Intelligent.class,
						r.getContentAsString());
				name = intelligent.getName();
				ArrayList<String> sortGroup = new ArrayList<String>();
				ArrayList<Data> sortData = intelligent.getData();
				for (Data d : sortData) {
					sortGroup.add(d.getName());
				}
				holder_button.put(2, name);
				viewMiddle.setItems(sortGroup);
				mViewArray.put(2, viewMiddle);
				break;

			case 3:// 筛选
				viewRight = new ViewRight(activity);
				sxintelligent = Handler_Json.JsonToBean(Intelligent.class,
						r.getContentAsString());
				name = sxintelligent.getName();
				ArrayList<String> iconGroup = new ArrayList<String>();
				ArrayList<String> sxGroup = new ArrayList<String>();
				ArrayList<Data> sxData = sxintelligent.getData();
				for (Data d : sxData) {
					sxGroup.add(d.getName());
					iconGroup.add(d.getKey());
				}
				holder_button.put(3, name);
				viewRight.setICons(iconGroup);
				viewRight.setItems(sxGroup);
				mViewArray.put(3, viewRight);
				break;

			case 4:

				list = Handler_Json.JsonToBean(ActivityList.class,
						r.getContentAsString());
				if (list.getData() != null) {
					if (adapter != null) {
						adapter.notifyDataSetChanged();
					}

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
						map.put("activityId", data.getActivity().getId());
						map.put("shopId", data.getShop().getId());
						map.put("tv_search_tag", data.getActivity().getTag());
						map.put("icons", data.getActivity().getCategory());
						dataList.add(map);
					}
					if (adapter == null) {
						adapter = new SearchAdapter(lv_search_content,
								dataList, R.layout.activity_search_item);
						lv_search_content.setAdapter(adapter);
					} else {
						adapter.notifyDataSetChanged();
					}

				} else {
					lv_search_content.setAdapter(null);
				}
				endProgress();
				break;

			case 5:
				promote = Handler_Json.JsonToBean(HomePromote.class,
						r.getContentAsString());
				tv_jiwen_about.setText(Html.fromHtml(String.format(
						getString(R.string.jiwen_about), promote.getTotal(),
						query)));
				break;

			}
			if (r.getKey() < 4) {
				if (holder_button.size() == 4) {
					v.expandtab_view.setValue(holder_button, mViewArray);
					viewMiddle
							.setOnSelectListener(new ViewMiddle.OnSelectListener() {
								@Override
								public void getValue(String distance,
										String showText, int position) {
									intelligentStr = intelligent.getData()
											.get(position).getKey();
									adapter = null;
									dataList.clear();
									startSearch();
									onRefresh(viewMiddle, showText);
								}
							});

					viewLeft.setOnSelectListener(new ViewLeft.OnSelectListener() {
						@Override
						public void getValue(String showText, int parentId,
								int position) {
							if (AllService.getData().get(parentId).getItems() == null) {
								onRefresh(viewLeft, showText);
								HODLER_TYPE = AllService.getData()
										.get(parentId).getKey()
										+ "";
							} else if (AllService.getData().get(parentId)
									.getItems().get(position) != null) {
								HODLER_TYPE = AllService.getData()
										.get(parentId).getItems().get(position)
										.getKey();
							}
							adapter = null;
							dataList.clear();
							startSearch();
							onRefresh(viewLeft, showText);
						}

					});
					viewLeft2
							.setOnSelectListener(new ViewLeft.OnSelectListener() {

								@Override
								public void getValue(String showText,
										int parentId, int position) {

									if (parentId == 0) {

										if (nearService.getData().get(parentId)
												.getItems() == null) {
											onRefresh(viewLeft, showText);
											SearchContentFragment.this.position = nearService
													.getData().get(parentId)
													.getKey()
													+ "";
										} else if (nearService.getData()
												.get(parentId).getItems()
												.get(position) != null) {
											SearchContentFragment.this.position = nearService
													.getData().get(parentId)
													.getItems().get(position)
													.getKey();
										}
									} else {
										SearchContentFragment.this.position = nearService
												.getData().get(parentId)
												.getItems().get(position)
												.getKey();
									}
									adapter = null;
									dataList.clear();
									startSearch();
									onRefresh(viewLeft2, showText);
								}
							});

					viewRight
							.setOnSelectListener(new ViewRight.OnSelectListener() {

								@Override
								public void getValue(String distance,
										String showText, int position) {
									type = sxintelligent.getData()
											.get(position).getKey();
									if ("all".equals(type)) {
										typeSb.delete(0, typeSb.length());
									}
									if (!typeSb.toString().contains(type)) {
										typeSb.append(type + ",");
									}
									adapter = null;
									dataList.clear();
									startSearch();
									onRefresh(viewRight, showText);
								}
							});
				}
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
					refreshParams.put("location", "31.293688,121.524448");
					refreshParams.put("site", getSelectCity()[0]);

					try {
						refreshParams.put("poistion", URLEncoder.encode(
								SearchContentFragment.this.position, "utf-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					try {
						refreshParams.put("query",
								URLEncoder.encode(query, "utf-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					refreshParams.put("service", HODLER_TYPE);
					refreshParams
							.put("type",
									typeSb.substring(0, typeSb.length() - 1)
											.toString());
					refreshCurrentList(list.getNext_page_url(), refreshParams,
							4, lv_search_content);
				}
			}
			break;
		case InjectView.DOWN:
			if (list != null) {
				refreshParams = new LinkedHashMap<>();
				refreshParams.put("location", "31.293688,121.524448");
				refreshParams.put("site", getSelectCity()[0]);
				try {
					refreshParams.put("poistion", URLEncoder.encode(
							SearchContentFragment.this.position, "utf-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				refreshParams.put("service", HODLER_TYPE);
				try {
					refreshParams.put("query",
							URLEncoder.encode(query, "utf-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (typeSb.length() > 1) {
					refreshParams
							.put("type",
									typeSb.substring(0, typeSb.length() - 1)
											.toString());
				}
				refreshCurrentList(refreshUrl, refreshParams, refreshKey,
						lv_search_content);
			}
			break;
		}
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
