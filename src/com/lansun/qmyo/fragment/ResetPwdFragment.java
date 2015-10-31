package com.lansun.qmyo.fragment;

import java.util.LinkedHashMap;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.ErrorInfo;
import com.lansun.qmyo.domain.User;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.R;

/**
 * TODO 重置密码
 * 
 * @author bhxx
 * 
 */
public class ResetPwdFragment extends BaseFragment {
	@InjectAll
	Views v;
	private String code;
	private String mobile;
	private String pwd;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View  tv_reset_confirm, fl_comments_right_iv;
		private EditText et_reset_pwd, et_reset_re_pwd;
		private TextView tv_activity_title, tv_reset_pwd_mobile;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View rootView = inflater.inflate(R.layout.activity_reset_pwd, null);
		Handler_Inject.injectFragment(this, rootView);
		return rootView;
	}

	@InjectInit
	private void init() {
		v.fl_comments_right_iv.setVisibility(View.GONE);
		initTitle(v.tv_activity_title, R.string.reset_pwd, null, 0);
		code = getArguments().getString("code");
		mobile = getArguments().getString("mobile");
		v.tv_reset_pwd_mobile.setText(mobile);
	}

	private void click(View view) {
		switch (view.getId()) {
		case R.id.tv_reset_confirm:
			pwd = v.et_reset_pwd.getText().toString().trim();
			String rePwd = v.et_reset_re_pwd.getText().toString().trim();
			
			if (pwd.length() < 6) {
				CustomToast.show(activity, "密码","请按要求设置新密码");
				return;
			}
			if (TextUtils.isEmpty(pwd) && TextUtils.isEmpty(rePwd)) {
				CustomToast.show(activity, "密码", "密码必须填写");
				return;
			} else if (!pwd.equals(rePwd)) {
				CustomToast.show(activity, "密码", "两次密码不一致");
				return;
			} else {
				LinkedHashMap<String, String> params = new LinkedHashMap<>();
				params.put("mobile", mobile);
				params.put("password", pwd);
				params.put("code", code);
				InternetConfig config = new InternetConfig();
				config.setKey(0);
				/*FastHttpHander.ajaxForm(GlobalValue.URL_AUTH_RESET, params,
						null, config, this);*/
				FastHttpHander.ajax(GlobalValue.URL_AUTH_RESET, params,
						config, this);
			}

			break;
		}
	}

	@InjectHttp
	private void retult(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			ErrorInfo error = Handler_Json.JsonToBean(ErrorInfo.class,
					r.getContentAsString());
			if (!TextUtils.isEmpty(error.getError())) {
				CustomToast.show(activity, R.string.tip, error.getError());
				return;
			}
			
			User user = Handler_Json.JsonToBean(User.class,
					r.getContentAsString());
			App.app.setData("secret", user.getSecret());
			CustomToast.show(activity, "重置密码", "重置成功");
			
			RegisterFragment fragment = new RegisterFragment();
			
			Bundle args = new Bundle();
			args.putBoolean("isResetPsw", true);
			args.putString("mobile", mobile);
			args.putString("password", pwd);
			fragment.setArguments(args);
			
			FragmentEntity entity = new FragmentEntity();
			entity.setFragment(fragment);
			EventBus.getDefault().post(entity);
			
			
			
			
		} else {
			CustomToast.show(activity, "重置密码", "重置失败");
		}
	}
}
