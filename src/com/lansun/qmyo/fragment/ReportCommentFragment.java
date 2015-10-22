package com.lansun.qmyo.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.PickerView;
import com.android.pc.ioc.view.PickerView.onSelectListener;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.ioc.view.listener.OnItemClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.adapter.CommonAdapter;
import com.lansun.qmyo.adapter.SearchHotAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.User;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.view.NestRadioGroup;
import com.lansun.qmyo.R;

/**
 * 编辑用户信息的Fragment
 * 
 * @author bhxx
 * 
 */
public class ReportCommentFragment extends BaseFragment {

	@InjectAll
	Views v;
	private String comment_id;
	private String type;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View fl_comments_right_iv, tv_activity_shared,
				btn_report_user_commit;
		private TextView tv_activity_title;

		private EditText et_report_user_content;

		private NestRadioGroup rg_report_type;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_report_user, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		v.fl_comments_right_iv.setVisibility(View.GONE);
		Bundle bundle = getArguments();
		if (bundle != null) {
			comment_id = bundle.getString("comment_id");
		}
		ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

	}

	private void click(View view) {
		ArrayList<String> data = new ArrayList<String>();
		switch (view.getId()) {

		case R.id.btn_report_user_commit:
			if (TextUtils.isEmpty(v.et_report_user_content.getText())) {
				CustomToast.show(activity, R.string.tip,
						R.string.please_enter_content);
				return;
			}
			InternetConfig config = new InternetConfig();
			config.setKey(0);
			HashMap<String, Object> head = new HashMap<>();
			head.put("Authorization",
					"Bearer " + App.app.getData("access_token"));
			config.setHead(head);
			LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
			params.put("comment_id", comment_id);
			params.put("content", v.et_report_user_content.getText().toString()
					.trim());
			switch (v.rg_report_type.getCheckedRadioButtonId()) {
			case R.id.cb1:
				type = "political";
				break;
			case R.id.cb2:
				type = "obscene";
				break;
			case R.id.cb3:
				type = "dirty";
				break;
			case R.id.cb4:
				type = "privacy";
				break;
			case R.id.cb5:
				type = "advertising";
				break;
			case R.id.cb6:
				type = "other";
				break;

			}
			params.put("type", type);
			FastHttpHander.ajaxForm(GlobalValue.URL_ACTIVITY_REPORT, params,
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
					CustomToast.show(activity, R.string.tip,
							R.string.report_success);
					back();
				}
				break;
			}
		}
	}

}
