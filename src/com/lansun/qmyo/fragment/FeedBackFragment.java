package com.lansun.qmyo.fragment;

import java.util.HashMap;
import java.util.LinkedHashMap;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectView;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.R;

public class FeedBackFragment extends BaseFragment {
	@InjectView
	private TextView tv_feedback_title;
	@InjectView(binders = { @InjectBinder(listeners = { OnClick.class }, method = "click") })
	private View btn_feedback_commit;
	@InjectView
	private EditText et_feedback_content;
	private String content;
	private String type;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_feedback, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		String title = getArguments().getString("title");
		type = getArguments().getString("type");
		tv_feedback_title.setText(title);
	}

	private void click(View view) {
		switch (view.getId()) {
		case R.id.btn_feedback_commit:
			content = et_feedback_content.getText().toString().trim();
			if (TextUtils.isEmpty(content)) {
				CustomToast.show(activity, getString(R.string.tip), "请填写内容");
				return;
			}
			InternetConfig config = new InternetConfig();
			config.setKey(0);
			HashMap<String, Object> head = new HashMap<>();
			head.put("Authorization",
					"Bearer " + App.app.getData("access_token"));
			config.setHead(head);
			LinkedHashMap<String, String> params = new LinkedHashMap<>();
			params.put("content", content);
			params.put("type", type);
			FastHttpHander.ajaxForm(GlobalValue.URL_USER_FEEDBACK, params,
					null, config, this);
			break;
		}
	}

	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 0:
				if ("true".equals(r.getContentAsString())) {
					CustomToast.show(activity, getString(R.string.tip),
							"意见提交成功，请耐心等待");
				}
				break;
			}
		} else {
			CustomToast.show(activity, "网络故障", "网络错误");
		}

	}

}
