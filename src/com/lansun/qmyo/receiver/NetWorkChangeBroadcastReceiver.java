package com.lansun.qmyo.receiver;

import java.util.HashMap;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.FastHttpHander;
import com.android.pc.ioc.internet.InternetConfig;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.lansun.qmyo.MainActivity;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.Token;
import com.lansun.qmyo.domain.User;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.BaseFragment;
import com.lansun.qmyo.fragment.ExperienceSearchFragment;
import com.lansun.qmyo.service.AccessTokenService;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CustomToast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
/**
 * 接收网络发生变化的系统广播，记住是： 发生改变时，不会一进来就给你弹出提醒，只有你所处网络环境发生变化
 * @author Yeun.Zhang
 *
 */
public class NetWorkChangeBroadcastReceiver extends BroadcastReceiver {

	public int i;
	@Override
	public void onReceive(Context context, Intent intent) {
		
		//拿到系统的CONNECTIVITY_SERVICE,去获取ConnectivityManager
		ConnectivityManager connectivityManager = (ConnectivityManager) context.
				getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if (connectivityManager != null) {
			NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
	
			/*	Log.i("第一种网络",networkInfos[0].toString());
			Log.i("第二种网络",networkInfos[1].toString());
			Log.i("第三种网络",networkInfos[1].toString());
			Log.i("第四种网络",networkInfos[1].toString());
			Log.i("第五种网络",networkInfos[1].toString());
			Log.i("第六种网络",networkInfos[1].toString());*/
			
			
			/*Log.i("网络1的Type",networkInfos[0].getType()+"");
			Log.i("网络2的Type",networkInfos[1].getType()+"");*/
			
			/*Log.i("networkInfos.length的长度：",networkInfos.length+"");*/ //真是奇葩！！！ networkInfos.length = 11
			
			for (i = 0; i<2; i++) {//两种网络中任一网络连接都可以前往获取token
				
				if (NetworkInfo.State.CONNECTED == networkInfos[i].getState()) {//任一网络的状态码为 连接状态，那就可以前去拿到token，后面再拿到user信息，再存入本地
					
					InternetConfig config = new InternetConfig();
					config.setKey(0);
					//当网络为连接状态时，程序将带着secret前往服务器拿到需要的token
					if (!TextUtils.isEmpty(App.app.getData("exp_secret"))) {
						FastHttpHander.ajaxGet(GlobalValue.URL_GET_ACCESS_TOKEN
								+ App.app.getData("exp_secret"), config, this);
					} else if (!TextUtils.isEmpty(App.app.getData("secret"))) {
						FastHttpHander.ajaxGet(GlobalValue.URL_GET_ACCESS_TOKEN
								+ App.app.getData("secret"), config, this);
					}
					Handler_Inject.injectFragment(this, null);
					System.out.println(i+"NetworkChangeBroadcast中友情提醒你网络为可用状态，燥起来！");
					
					if(networkInfos[i].getType()==0){
						/*Toast.makeText(context, "已接入移动网络！", 2000).show();*/
					}
					if(networkInfos[i].getType()==1){
						Toast.makeText(context, "已接入无线WiFi网络！", 2000).show();
					}
					/*Toast.makeText(context, "已接入网络！", 2000).show();*/
					/*return;*/
				
				}else if(NetworkInfo.State.DISCONNECTED == networkInfos[i].getState()){
					Handler_Inject.injectFragment(this, null);
					System.out.println(i+"NetworkChangeBroadcast中友情提醒你网络   不可用！！！赶快找无线啦！");
					
					if(networkInfos[i].getType()==0){
						/*Toast.makeText(context, "移动网络断开！", 2000).show();*/
					}
					if(networkInfos[i].getType()==1){
						/*Toast.makeText(context, "无线WiFi网络断开！", 2000).show();*/
					}
					/*Toast.makeText(context, "目前暂无网络！", 2000).show();*/
					/*return;*/
				}
			}
		}else{
			System.out.println("connectivityManager居然为空！！！");
		}
	}

	/**
	 * 由访问服务的结果去获取token,并且写入到本地的token中,再去拿到user对象的信息
	 * @param r
	 */
	@InjectHttp
	private void getAccesstoken(ResponseEntity r) {
		
		Log.i("NetWorkChangeBroadcast中的r返回回来的值为： ",r.getContentAsString());
		if(r.getContentAsString().contains("false") ||r.getContentAsString().equals(false)||r.getContentAsString()=="false"){
			Toast.makeText(App.app,"来自网络监听端的提示 ：您的临时用户身份已被清掉！请重新体验，或注册登录！", Toast.LENGTH_LONG).show();
			
			/**
			 * 一旦发现临时用户的secret去访问获取token失败，那么需要立即将secret和token全部清除掉，以便于后面前去生成新的体验用户
			 */
			BaseFragment.clearTempTokenAndSercet();
			BaseFragment.clearTokenAndSercet();
			
			/*ExperienceSearchFragment fragment = new ExperienceSearchFragment();
			FragmentEntity entity = new FragmentEntity();
			entity.setFragment(fragment);
			EventBus.getDefault().post(entity);
			BaseFragment.clearTempTokenAndSercet();*/
			
		}
		if (r.getStatus() == FastHttp.result_ok) {

			switch (r.getKey()) {
			case 0:
				Token token = Handler_Json.JsonToBean(Token.class,
						r.getContentAsString());
				InternetConfig config = new InternetConfig();
				config.setKey(1);
				HashMap<String, Object> head = new HashMap<String, Object>();
				head.put("Authorization", "Bearer " + token.getToken());
				config.setHead(head);
				FastHttpHander.ajaxGet(GlobalValue.URL_FRESHEN_USER, config, this);
				App.app.setData("access_token", token.getToken());
				break;
			case 1:
				GlobalValue.user = Handler_Json.JsonToBean(User.class,
						r.getContentAsString());
				break;

			}

		}
	}
}