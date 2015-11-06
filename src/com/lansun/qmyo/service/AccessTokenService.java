package com.lansun.qmyo.service;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.mapcore2d.ev;
import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.internet.AjaxCallBack;
import com.android.pc.ioc.internet.AjaxTimeCallBack;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.Token;
import com.lansun.qmyo.domain.User;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.event.entity.RefreshTokenEntity;
import com.lansun.qmyo.fragment.BaseFragment;
import com.lansun.qmyo.fragment.ExperienceSearchFragment;
import com.lansun.qmyo.fragment.MineFragment;
import com.lansun.qmyo.utils.GlobalValue;

/**
 * 获取Accesstoken
 * 
 * @author bhxx
 * 
 */
public class AccessTokenService extends Service {

	private int delay = 1000 * 60 * 30;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	/*
	 *  
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		InternetConfig config = new InternetConfig();
		config.setKey(0);
		if (!TextUtils.isEmpty(App.app.getData("exp_secret"))) {
			FastHttpHander.ajaxGet(GlobalValue.URL_GET_ACCESS_TOKEN+ App.app.getData("exp_secret"), config, this);
			
		} else if (!TextUtils.isEmpty(App.app.getData("secret"))) {
			FastHttpHander.ajaxGet(GlobalValue.URL_GET_ACCESS_TOKEN+ App.app.getData("secret"), config, this);
		}
		Handler_Inject.injectFragment(this, null);
		return START_STICKY;
	}

	private void refreshToken() {
		InternetConfig config = new InternetConfig();
		config.setKey(0);
		FastHttpHander.ajaxGet(GlobalValue.URL_GET_ACCESS_TOKEN + App.app.getData("secret"),config, this);
	}

	@Override
	public void onCreate() {
		EventBus.getDefault().register(this);
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				try {
					RefreshTokenEntity event = new RefreshTokenEntity();
					event.setRefresh(true);
					EventBus.getDefault().post(event);
				} catch (Exception ie) {
				}
			}
		}, delay, delay);
		super.onCreate();
	}

	private void onEventMainThread(RefreshTokenEntity event) {
		if (event.isRefresh()) {
			refreshToken();
		}
	}

	@Override
	public void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	@InjectHttp
	private void getAccesstoken(ResponseEntity r) {
		Log.i("AccessTokenService中的r返回回来的值为： ",r.getContentAsString());
		
		if(r.getContentAsString().contains("false") ||r.getContentAsString().equals(false)||r.getContentAsString()=="false"){
			Toast.makeText(App.app,"来自启动自服务的提示 ：您的临时用户身份已被清掉！请重新体验，或注册登录！", Toast.LENGTH_LONG).show();
			
			/**
			 * 一旦发现临时用户的secret去访问获取token失败，那么需要立即将secret和token全部清除掉，以便于后面前去生成新的体验用户
			 */
			BaseFragment.clearTempTokenAndSercet();
			BaseFragment.clearTokenAndSercet();
			
			
			ExperienceSearchFragment fragment = new ExperienceSearchFragment();
			FragmentEntity entity = new FragmentEntity();
			entity.setFragment(fragment);
			EventBus.getDefault().post(entity);
			BaseFragment.clearTempTokenAndSercet();
		}
		if (r.getStatus() == FastHttp.result_ok) {

			switch (r.getKey()) {
			case 0:
				Token token = Handler_Json.JsonToBean(Token.class,
						r.getContentAsString());
				App.app.setData("access_token", token.getToken());
				InternetConfig config = new InternetConfig();
				config.setKey(1);
				HashMap<String, Object> head = new HashMap<>();
				head.put("Authorization", "Bearer " + token.getToken());
				config.setHead(head);
				FastHttpHander.ajaxGet(GlobalValue.URL_FRESHEN_USER, config,this);
				
				break;
			case 1:
				//拿到access_token就可以去拿到用户信息，并且将其存在本地的静态变量GlobalValue.user中，供后面使用
				GlobalValue.user = Handler_Json.JsonToBean(User.class,r.getContentAsString());
				break;

			}
		}
	}
}
