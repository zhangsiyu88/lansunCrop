package com.lansun.qmyo.fragment;

import java.util.HashMap;
import java.util.LinkedHashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.inject.InjectLayer;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.MainActivity;
import com.lansun.qmyo.MainFragment;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.User;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.view.CustomToast;
import com.lansun.qmyo.R;

/**
 * 编辑用户信息的Fragment
 * 
 * @author bhxx
 * 
 */
public class ExperienceSearchFragment extends BaseFragment {

	@InjectAll
	Views v;
	private String name;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View  ll_experience_login,
				rl_experience_search_card;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView btn_experience_jump;

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View view = inflater.inflate(R.layout.activity_experience_search, null);
		if (GlobalValue.user != null) {
			back();
		}
		Handler_Inject.injectFragment(this, view);
		return view;
	}

	@InjectInit
	private void init() {
		if (GlobalValue.user != null) {
			v.ll_experience_login.setVisibility(View.GONE);
		}
	}

	@SuppressLint("ResourceAsColor") 
	private void click(View view) {
		Fragment fragment = null;
		FragmentEntity event = new FragmentEntity();
		switch (view.getId()) {
		case R.id.btn_experience_jump:
			GlobalValue.isFirst = false;//走的时候已将isFirst设置为false
			v.btn_experience_jump.setTextColor(Color.WHITE);
			v.btn_experience_jump.setBackgroundResource(R.drawable.main_btn_shape_pressed);
			/*fragment = new HomeFragment();*/
			fragment = new MainFragment(0);
			break;
		case R.id.ll_experience_login:
			fragment = new RegisterFragment();
			Boolean isJustLogin = true;
			Bundle bundle = new Bundle();
			bundle.putBoolean("isJustLogin", isJustLogin);
			fragment.setArguments(bundle);
			break;
		case R.id.rl_experience_search_card:
			fragment = new SearchBankCardFragment();
			break;
		}
		if (fragment != null) {
			event.setFragment(fragment);
			EventBus.getDefault().post(event);
		}
	}

}
