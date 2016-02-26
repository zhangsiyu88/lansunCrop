package com.lansun.qmyo.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.lansun.qmyo.MainActivity;
import com.lansun.qmyo.MainFragment;
import com.lansun.qmyo.R;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;

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
	private ExperienceSearchBroadcast experienceSearchBroadcast;

	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View  ll_experience_login,
				rl_experience_search_card;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private TextView btn_experience_jump;

	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		experienceSearchBroadcast = new ExperienceSearchBroadcast();
		System.out.println("MainFragment中注册广播 ing");
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.lansun.qmyo.Click2RegisterFragment");
		activity.registerReceiver(experienceSearchBroadcast, filter);
		super.onCreate(savedInstanceState);
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
//	    v.btn_experience_jump.setTextColor(Color.WHITE);
//	    v.btn_experience_jump.setBackgroundResource(R.drawable.main_btn_shape_pressed);
			
//	    FragmentManager supportFragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
//			for (int i = 0; i < supportFragmentManager.getBackStackEntryCount(); i++) {
//				BackStackEntry entry = supportFragmentManager.getBackStackEntryAt(i);
//				if(entry.getName().equals(ExperienceSearchFragment.class.getName())){
//					supportFragmentManager.popBackStack(entry.getName(), 1);
//					LogUtils.toDebugLog("pop", "剔除ExperienceSearchFragment");
//				}
//			}
			/*fragment = new HomeFragment();*/
			fragment = new MainFragment(0);
			LogUtils.toDebugLog("times", "1: "+System.currentTimeMillis());
			
			break;
		case R.id.ll_experience_login:
			
//			 FragmentManager supportFragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
//				for (int i = 0; i < supportFragmentManager.getBackStackEntryCount(); i++) {
//					BackStackEntry entry = supportFragmentManager.getBackStackEntryAt(i);
//				LogUtils.toDebugLog("pop", ""+entry.getName());
//			}
			fragment = new RegisterFragment();
			Boolean isJustLogin = true;
			Bundle bundle = new Bundle();
			bundle.putBoolean("isJustLogin", isJustLogin);
			fragment.setArguments(bundle);
			break;
		case R.id.rl_experience_search_card:
			
//			 FragmentManager supportFragmentManager1 = ((FragmentActivity) activity).getSupportFragmentManager();
//				for (int i = 0; i < supportFragmentManager1.getBackStackEntryCount(); i++) {
//					BackStackEntry entry = supportFragmentManager1.getBackStackEntryAt(i);
//				LogUtils.toDebugLog("pop", ""+entry.getName());
//			}
			
			
			fragment = new RegisterFragment();
			Boolean _isJustLogin = true;
			Boolean _toRegister = true;
			Bundle _bundle = new Bundle();
			_bundle.putBoolean("isJustLogin", _isJustLogin);
			_bundle.putBoolean("toRegister", _toRegister);
			fragment.setArguments(_bundle);
//			fragment = new SearchBankCardFragment();
			break;
		}
		if (fragment != null) {
			event.setFragment(fragment);
			EventBus.getDefault().post(event);
		}
	}
	
	class ExperienceSearchBroadcast extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals("com.lansun.qmyo.Click2RegisterFragment")){
				Fragment fragment = new RegisterFragment();
				Boolean _isJustLogin = true;
//					Boolean _toRegister = true;
//					_bundle.putBoolean("toRegister", _toRegister);
				Bundle _bundle = new Bundle();
				_bundle.putBoolean("isJustLogin", _isJustLogin);
				fragment.setArguments(_bundle);
				((MainActivity)activity).startFragmentAdd(fragment);
			}
		}
	}

	@Override
	public void onDestroy() {
		activity.unregisterReceiver(experienceSearchBroadcast);
		super.onDestroy();
	}
}
