package com.lansun.qmyo.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

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
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.adapter.MineSecretaryAdapter;
import com.lansun.qmyo.adapter.QuestionDetailAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.QuestionDetail;
import com.lansun.qmyo.domain.QuestionDetailItem;
import com.lansun.qmyo.domain.Secretary;
import com.lansun.qmyo.domain.SecretaryQuestions;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.ExpandTabView;
import com.lansun.qmyo.view.MyListView;
import com.lansun.qmyo.R;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 新品曝光
 * 
 * @author bhxx
 * 
 */
public class QuestionDetailFragment extends BaseFragment {
	@InjectAll
	Views v;
	private String question_id;

	private QuestionDetailAdapter adapter;

	@InjectView(down = true, pull = false)
	private MyListView lv_mine_secretary_quetions_detail;
	private QuestionDetail list;
	private String currentType;
	private ProgressDialog pd;
	private ArrayList<HashMap<String, String>> dataList;

	class Views {
		private View fl_comments_right_iv, tv_activity_shared;
		private TextView tv_activity_title, tv_mine_secretary_type;
		@InjectBinder(listeners = { OnClick.class }, method = "commit")
		private Button btn_secretary_question_commit;
		private EditText et_secretary_question;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(
				R.layout.activity_secretary_question_detail, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		v.fl_comments_right_iv.setVisibility(View.GONE);
		initTitle(v.tv_activity_title, R.string.mine_secretary, null, 0);
		if (getArguments() != null) {
			question_id = getArguments().getString("question_id");
		}
		refreshUrl = GlobalValue.URL_SECRETARY_QUESTION_DETAIL + question_id;
		refreshKey = 0;
		refreshCurrentList(refreshUrl, null, refreshKey,
				lv_mine_secretary_quetions_detail);
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			endProgress();
			switch (r.getKey()) {
			case 0:
				list = Handler_Json.JsonToBean(QuestionDetail.class,
						r.getContentAsString());
				dataList = new ArrayList<HashMap<String, String>>();

				HashMap<String, String> map = new HashMap<String, String>();
				map.put("tv_user_question", list.getContent());
				if (!TextUtils.isEmpty(list.getAnswer())) {
					map.put("tv_secretary_answer", list.getAnswer());
					map.put("iv_secretary_head",
							GlobalValue.secretary.getAvatar());
				}
				map.put("iv_user_head", GlobalValue.user.getAvatar());
				dataList.add(map);

				v.tv_mine_secretary_type.setText(switchType(list.getType()));

				if (list.getItems() != null) {
					for (QuestionDetailItem item : list.getItems()) {
						HashMap<String, String> itemMap = new HashMap<String, String>();
						itemMap.put("iv_user_head",
								GlobalValue.user.getAvatar());
						itemMap.put("tv_user_question", item.getContent());
						if (!TextUtils.isEmpty(item.getAnswer())) {
							itemMap.put("iv_secretary_head",
									GlobalValue.secretary.getAvatar());
							itemMap.put("tv_secretary_answer", item.getAnswer());
						}
						dataList.add(itemMap);
					}
				}
				if (adapter == null) {
					adapter = new QuestionDetailAdapter(lv_mine_secretary_quetions_detail, dataList,
							R.layout.activity_secretary_question_detail_item);
					lv_mine_secretary_quetions_detail.setAdapter(adapter);
				} else {
					PullToRefreshManager.getInstance().onHeaderRefreshComplete();
					PullToRefreshManager.getInstance().onFooterRefreshComplete();
					adapter.notifyDataSetChanged();
				}
				break;
			case 1:
				if ("true".equals(r.getContentAsString())) {
					pd.dismiss();
					adapter = null;
					refreshCurrentList(refreshUrl, null, 0,lv_mine_secretary_quetions_detail);
				}

				break;
			}
		} else {
			
			progress_text.setText(R.string.net_error_refresh);
		}
		PullToRefreshManager.getInstance().onHeaderRefreshComplete();
			PullToRefreshManager.getInstance().onFooterRefreshComplete();
	}

	private int switchType(String type) {
		currentType = type;
		int resId = R.string.travel_holiday;
		switch (type) {
		case "travel":
			resId = R.string.travel_holiday;
			break;
		case "shopping":
			resId = R.string.new_shopping;
			break;
		case "party":
			resId = R.string.shengyan_part;
			break;
		case "life":
			resId = R.string.life_service;
			break;
		case "student":
			resId = R.string.studybroad;
			break;
		case "investment":
			resId = R.string.investment;
			break;
		case "card":
			resId = R.string.handlecard;
			break;
		}
		return resId;
	}

	/**
	 * 追加提问
	 */
	private void commit(View view) {
		if (TextUtils.isEmpty(v.et_secretary_question.getText().toString()
				.trim())) {
			CustomToast.show(activity, R.string.tip,
					R.string.please_enter_content);
			return;
		}
		InternetConfig config = new InternetConfig();
		config.setKey(1);
		HashMap<String, Object> head = new HashMap<>();
		head.put("Authorization", "Bearer " + App.app.getData("access_token"));
		config.setHead(head);
		LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
		params.put("content", v.et_secretary_question.getText().toString()
				.trim());
		params.put("type", currentType);
		params.put("principal", question_id);
		FastHttpHander.ajaxForm(GlobalValue.URL_SECRETARY_QUESTION, params,
				null, config, this);
		pd = new ProgressDialog(activity);
		pd.setMessage(getString(R.string.up_dataing));
		pd.show();
	}

	@InjectPullRefresh
	private void call(int type) {
		switch (type) {
		case InjectView.DOWN:
			if (list != null) {
				refreshCurrentList(refreshUrl, null, 0,
						lv_mine_secretary_quetions_detail);
			}
			break;
		}
	}
}
