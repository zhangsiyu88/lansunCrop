package com.lansun.qmyo.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ToggleButton;

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
import com.lansun.qmyo.adapter.BankCardAdapter;
import com.lansun.qmyo.adapter.MineSecretaryAdapter;
import com.lansun.qmyo.adapter.TextAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.Data;
import com.lansun.qmyo.domain.Intelligent;
import com.lansun.qmyo.domain.QuestionDetailItem;
import com.lansun.qmyo.domain.Secretary;
import com.lansun.qmyo.domain.SecretaryQuestions;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ExpandTabView;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.view.ViewRight;
import com.lansun.qmyo.R;

/**
 * 我的私人秘书 和私人秘书不是同一个界面
 * 
 * @author bhxx
 * 
 */
public class MineSecretaryFragment extends BaseFragment {

	@InjectAll
	Views v;
	private String owner_name;
	private MineSecretaryAdapter adapter;
	private String[] types = new String[] { "", "travel", "shopping", "party",
			"life", "student", "investment", "card" };

	@InjectView(binders = { @InjectBinder(listeners = { OnItemClick.class }, method = "itemClick") }, down = true, pull = true)
	private MyListView lv_secretary_list;
	private SecretaryQuestions list;

	class Views {
		private View fl_comments_right_iv;
		private TextView tv_activity_title;
		private ExpandTabView exp_mine_secretary;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_mine_secretary, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	private HashMap<Integer, String> holder_button = new HashMap<Integer, String>();
	private HashMap<Integer, View> mViewArray = new HashMap<Integer, View>();
	
	private String currentType = ""; //当下这个currentType值为空字符串

	@InjectInit
	private void init() {
		v.fl_comments_right_iv.setVisibility(View.GONE);
		initTitle(v.tv_activity_title, R.string.mine_secretary, null, 0);

		viewRight = new ViewRight(activity);
		v.exp_mine_secretary.removeAllViews();
		ArrayList<String> sxGroup = new ArrayList<String>();
		sxGroup.add(getString(R.string.all));
		sxGroup.add(getString(R.string.travel_holiday));
		sxGroup.add(getString(R.string.new_shopping));
		sxGroup.add(getString(R.string.shengyan_part));
		sxGroup.add(getString(R.string.gaozhi_life));
		sxGroup.add(getString(R.string.studybroad));
		sxGroup.add(getString(R.string.licai_touzi));
		sxGroup.add(getString(R.string.handlecard));
		viewRight.setItems(sxGroup);
		holder_button.put(0, getString(R.string.all));
		mViewArray.put(0, viewRight);

		v.exp_mine_secretary.setValue(holder_button, mViewArray);
		
		refreshUrl = GlobalValue.URL_SECRETARY;
		refreshKey = 0;
		refreshCurrentList(refreshUrl, null, refreshKey, lv_secretary_list);
		
		viewRight.setOnSelectListener(new ViewRight.OnSelectListener() {

			@Override
			public void getValue(String distance, String showText, int position) {
				adapter = null;
				currentType = types[position];
				refreshParams.put("type", currentType);
				refreshCurrentList(refreshUrl, null, refreshKey,lv_secretary_list);
				onRefresh(viewRight, showText);
			}
		});
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			endProgress();
			switch (r.getKey()) {
			case 0:
				GlobalValue.secretary = Handler_Json.JsonToBean(
						Secretary.class, r.getContentAsString());
				// 获取信息列表
				refreshParams = new LinkedHashMap<>();
				refreshParams.put("type", currentType);
				refreshCurrentList(GlobalValue.URL_SECRETARY_QUESTIONS, refreshParams, 1, lv_secretary_list);
				break;
			case 1:
				list = Handler_Json.JsonToBean(SecretaryQuestions.class,
						r.getContentAsString());
				dataList = new ArrayList<HashMap<String, String>>();

				if (list.getData() != null) {
					for (QuestionDetailItem data : list.getData()) {
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("iv_mine_secretary_head",
								GlobalValue.secretary.getAvatar());
						map.put("tv_mine_secratary_item_name",
								GlobalValue.secretary.getName());
						map.put("tv_mine_secratary_item_desc",
								data.getContent());

						String questionTime = "";
						if ((System.currentTimeMillis() - Handler_Time
								.getInstance(data.getTime()).getTimeInMillis())
								/ (1000 * 24 * 60 * 60) > 1) {
							Handler_Time instance = Handler_Time
									.getInstance(data.getTime());
							questionTime = instance.getYear() + "-"
									+ instance.getMonth() + "-"
									+ instance.getDay();
						} else {
							questionTime = getHistoryTime(System
									.currentTimeMillis()
									- Handler_Time.getInstance(data.getTime())
											.getTimeInMillis());
						}

						map.put("tv_mine_secratary_item_time", questionTime);
						dataList.add(map);
					}
					if (adapter == null) {
						adapter = new MineSecretaryAdapter(lv_secretary_list,
								dataList, R.layout.activity_mine_secretary_item);
						lv_secretary_list.setAdapter(adapter);
					} else {
						adapter.notifyDataSetChanged();
					}
					PullToRefreshManager.getInstance()
					.onHeaderRefreshComplete();
					PullToRefreshManager.getInstance()
					.onFooterRefreshComplete();

				} else {
					lv_secretary_list.setAdapter(null);
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

	boolean isGreen = false;
	private ViewRight viewRight;
	private ArrayList<HashMap<String, String>> dataList;

	@Override
	public void onPause() {
		adapter = null;
		v.exp_mine_secretary.onPressBack();
		adapter = null;
		super.onPause();
	}

	public void itemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		// 秘书提问详情界面
		QuestionDetailFragment fragment = new QuestionDetailFragment();
		FragmentEntity event = new FragmentEntity();
		Bundle args = new Bundle();
		args.putString("question_id", list.getData().get(position).getId() + "");
		fragment.setArguments(args);
		event.setFragment(fragment);
		EventBus.getDefault().post(event);
	}

	@InjectPullRefresh
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
							lv_secretary_list);
				}
			}
			break;
		case InjectView.DOWN:
			if (list != null) {
				refreshCurrentList(refreshUrl, null, 0, lv_secretary_list);
			}
			break;
		}
	}

	private void onRefresh(View view, String showText) {
		v.exp_mine_secretary.onPressBack();
		int position = getPositon(view);
		if (position >= 0
				&& !v.exp_mine_secretary.getTitle(position).equals(showText)) {
			v.exp_mine_secretary.setTitle(showText, position);
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

}
