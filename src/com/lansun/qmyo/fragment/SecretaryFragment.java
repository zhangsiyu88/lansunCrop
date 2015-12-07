package com.lansun.qmyo.fragment;
import java.io.IOException;
import java.util.HashMap;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.jpush.android.util.ac;

import com.android.pc.ioc.a.demo.MainActivity;
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
import com.google.gson.Gson;
import com.lansun.qmyo.R;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.MySecretary;
import com.lansun.qmyo.domain.Secretary;
import com.lansun.qmyo.domain.Token;
import com.lansun.qmyo.domain.User;
import com.lansun.qmyo.domain.information.InformationCount;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.fragment.HomeFragment.HomeRefreshBroadCastReceiver;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryCardShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryInvestmentShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryLifeShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryPartyShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryShoppingShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryStudentShowFragment;
import com.lansun.qmyo.fragment.secretary_detail.SecretaryTravelShowFragment;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.override.CircleImageView;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.view.CustomToast;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class SecretaryFragment extends BaseFragment {
	private String secretary_size;
	private CircleImageView iv_secretary_head;
	private LinearLayout ll_secretary_setting;
	private boolean justOneTimes = true;
	
	/*
	 * 区别 由点击底部按钮进入当前页面和自动生成
	 */
	private boolean clickOpen = false;
	@InjectAll
	Views v;
	class Views {
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View bottom_home, bottom_found, bottom_mine, ll_shopping_carnival, ll_new_shopping,
		ll_shengyan_part, ll_gaozhi_life, ll_studybroad,
		ll_licai_touzi, ll_handlecard, ll_secretary_help;
		private ImageView iv_secretary_icon;
		private TextView tv_secretary_icon, tv_secretary_name,tv_secretary_tip1,have_information;
	}
	private String[] secretaryTitle;
	private String[] secretaryhint;
	private int[][] secretaryImages = new int[][] {
			{ R.drawable.details_figure01, R.drawable.details_figure02,
				R.drawable.details_figure03, R.drawable.details_figure04,
				R.drawable.details_figure05, R.drawable.details_cannot },
				{ R.drawable.details_shopping01, R.drawable.details_shopping02,
					R.drawable.details_shopping03,
					R.drawable.details_shopping04, R.drawable.details_cannot },
					{ R.drawable.details_party01, R.drawable.details_party02,
						R.drawable.details_party03, R.drawable.details_party04,
						R.drawable.details_cannot },
						{ R.drawable.details_life01, R.drawable.details_life02,
							R.drawable.details_life03, R.drawable.details_life04,
							R.drawable.details_cannot },
							{ R.drawable.details_abroad01, R.drawable.details_abroad02,
								R.drawable.details_cannot },
								{ R.drawable.details_financial01, R.drawable.details_financial02,
									R.drawable.details_cannot },
									{ R.drawable.details_card01, R.drawable.details_cannot } };
	private Fragment fragment;
	private Handler handleOk=new Handler(){
		

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				if (isExperience()) {
					setSecretaryInformation();
				}else {
					//只有因为点击私人秘书的页面，才可以弹出提示窗口
					if(clickOpen){
						
						//并无设置秘书信息时，需要将  立即登录设置私人秘书的提示界面弹出
						if ("false".equals(GlobalValue.mySecretary.getHas())) {
							final Dialog dialog=new Dialog(activity, R.style.Translucent_NoTitle);
							dialog.show();
							
							/**
							 * 提示去设置私人秘书
							 */
							dialog.setContentView(R.layout.dialog_setting_secretary);
							
							Window window = dialog.getWindow();
							window.findViewById(R.id.setting_now).setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									dialog.dismiss();
									FragmentEntity entity=new FragmentEntity();
									Fragment fragment=new SecretarySettingFragment();
									entity.setFragment(fragment);
									EventBus.getDefault().post(entity);
								}
							});
						}else {
							setSecretaryInformation();
						}
						
					}
					
					
					
					
					
				}
				break;
			case 1:
				//CustomToast.show(activity, "提示", "信息获取失败,请重试");              //这是由于缺失token和token过期造成的提示
				refreshTokenForExpired();
				break;
			case 2:
				CustomToast.show(activity, "提示", "网络异常,请刷新后重试");
				break;
			case 5:
				if("1".equals(secretary_size)){
					v.have_information.setVisibility(View.VISIBLE);
				}else{
					v.have_information.setVisibility(View.GONE);
				}
				break;
			}
		}

		
	};
	private SecretaryFragmentBroadCastReceiver broadCastReceiver;
	private IntentFilter filter;
	private View rootView;
	private void setSecretaryInformation(){
		v.tv_secretary_name.setText(GlobalValue.mySecretary.getName());
		loadPhoto(GlobalValue.mySecretary.getAvatar(),iv_secretary_head);
	};
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		rootView = inflater.inflate(R.layout.activity_secretary, container,false);
		Handler_Inject.injectFragment(this, rootView);
		
		
		
		v.iv_secretary_icon.setPressed(true);
	    initView(rootView);
		/*setListener();*/
		return rootView;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
	}
	private void setListener() {
		iv_secretary_head.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (GlobalValue.user == null || isExperience()) {
					DialogUtil.createTipAlertDialog(activity,R.string.please_registerorlogin_title,
							new TipAlertDialogCallBack() {
						@Override
						public void onPositiveButtonClick(DialogInterface dialog, int which) {
							EventBus bus = EventBus.getDefault();
							FragmentEntity entity = new FragmentEntity();
							Fragment fragment=new RegisterFragment();
							Bundle bundle = new Bundle();
							bundle.putString("fragment_name", SecretaryFragment.class.getSimpleName());
							fragment.setArguments(bundle);
							entity.setFragment(fragment);
							bus.post(entity);
							dialog.dismiss();
						}
						@Override
						public void onNegativeButtonClick(
								DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
				} else{
					EventBus bus = EventBus.getDefault();
					FragmentEntity entity = new FragmentEntity();
					Fragment fragment = new SecretarySettingFragment();
					entity.setFragment(fragment);
					bus.post(entity);
				}

			}
		});
		ll_secretary_setting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (GlobalValue.user == null || isExperience()) {
					DialogUtil.createTipAlertDialog(activity,R.string.please_registerorlogin_title,
							new TipAlertDialogCallBack() {
						@Override
						public void onPositiveButtonClick(DialogInterface dialog, int which) {
							EventBus bus = EventBus.getDefault();
							FragmentEntity entity = new FragmentEntity();
							Fragment fragment=new RegisterFragment();
							Bundle bundle = new Bundle();
							bundle.putString("fragment_name", SecretaryFragment.class.getSimpleName());
							fragment.setArguments(bundle);
							entity.setFragment(fragment);
							bus.post(entity);
							dialog.dismiss();
						}
						@Override
						public void onNegativeButtonClick(
								DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
				} else{
					if (GlobalValue.mySecretary!=null) {
						if ("false".equals(GlobalValue.mySecretary.getHas())) {
							final Dialog dialog=new Dialog(activity, R.style.Translucent_NoTitle);
							dialog.show();
							dialog.setContentView(R.layout.dialog_setting_secretary);
							Window window = dialog.getWindow();
							window.findViewById(R.id.setting_now).setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									dialog.dismiss();
									FragmentEntity entity=new FragmentEntity();
									Fragment fragment=new SecretarySettingFragment();
									entity.setFragment(fragment);
									EventBus.getDefault().post(entity);
								}
							});
						}else {
							EventBus bus = EventBus.getDefault();
							FragmentEntity entity = new FragmentEntity();
							Fragment fragment = new MineSecretaryFragment();
							entity.setFragment(fragment);
							bus.post(entity);
						}
					}else {
						EventBus bus = EventBus.getDefault();
						FragmentEntity entity = new FragmentEntity();
						Fragment fragment = new MineSecretaryFragment();
						entity.setFragment(fragment);
						bus.post(entity);
					}
				}

			}
		});
	}

	private void initView(View view) {
		iv_secretary_head=(CircleImageView)view.findViewById(R.id.iv_secretary_head);
		ll_secretary_setting=(LinearLayout)view.findViewById(R.id.ll_secretary_setting);
		
		if (!isExperience()) {
			OkHttp.asyncGet(GlobalValue.URL_SECRETARY_SAVE, "Authorization","Bearer "+App.app.getData("access_token"), null, new Callback() {
				@Override
				public void onResponse(Response response) throws IOException {
					if (response.isSuccessful()) {
						Gson gson=new Gson();
						String json=response.body().string();
						GlobalValue.mySecretary=gson.fromJson(json,MySecretary.class);
						handleOk.sendEmptyMessage(0);
					}else {
						handleOk.sendEmptyMessage(1);
					}
				}
				@Override
				public void onFailure(Request arg0, IOException arg1) {
					handleOk.sendEmptyMessage(2);
				}
			});
			OkHttp.asyncGet(GlobalValue.URL_USER_MESSAGE, "Authorization", "Bearer "+App.app.getData("access_token"), null, new Callback() {
				@Override
				public void onResponse(Response response) throws IOException {
					if (response.isSuccessful()) {
						Gson gson=new Gson();
						String json=response.body().string();
						InformationCount iCount=gson.fromJson(json, InformationCount.class);
						secretary_size = String.valueOf(iCount.getSecretary());
						handleOk.sendEmptyMessage(5);
					}
				}
				@Override
				public void onFailure(Request arg0, IOException arg1) {

				}
			});
		}
	}

	@InjectInit
	private void init() {
		secretaryTitle = getResources().getStringArray(R.array.secretary_title);
		secretaryhint = getResources().getStringArray(R.array.secretary_hint);
		v.iv_secretary_icon.setPressed(true);
		v.tv_secretary_icon.setTextColor(getResources().getColor(
				R.color.app_green2));
		
		broadCastReceiver = new SecretaryFragmentBroadCastReceiver();
		System.out.println("私人秘书 页面 注册广播 ing");
		filter = new IntentFilter();
		filter.addAction("com.lansun.qmyo.checkMySecretary");
		filter.addAction("com.lansun.qmyo.refreshMySecretary");
		getActivity().registerReceiver(broadCastReceiver, filter);

	}

	@Override
	public void onResume() {
		v.iv_secretary_icon.setPressed(true);
		super.onResume();
	}
	@Override
	public void onPause() {
		/*	v.iv_secretary_icon.setPressed(false);
		v.tv_secretary_icon.setTextColor(getResources().getColor(
				R.color.text_nomal));*/
		justOneTimes = true;
		super.onPause();
	}

	private void click(View view) {
		EventBus bus = EventBus.getDefault();
		FragmentEntity entity = new FragmentEntity();
		Bundle args = new Bundle();
		switch (view.getId()) {
		case R.id.bottom_home:
			fragment = new HomeFragment();
			break;
		case R.id.bottom_found:
			fragment = new FoundFragment();
			break;
		case R.id.bottom_mine:
			fragment = new MineFragment();
			break;
		case R.id.ll_secretary_help:// 帮助界面
			fragment = new HelpFragment();
			break;
		case R.id.ll_shopping_carnival:// 旅游度假
			fragment = new SecretaryTravelShowFragment();
			break;
		case R.id.ll_new_shopping:// 新品购物
			fragment = new SecretaryShoppingShowFragment();
			break;
		case R.id.ll_shengyan_part:// 盛宴派对
			fragment = new SecretaryPartyShowFragment();
			break;
		case R.id.ll_gaozhi_life:// 高质生活
			fragment = new SecretaryLifeShowFragment();
			break;
		case R.id.ll_studybroad:// 留学服务
			fragment = new SecretaryStudentShowFragment();
			break;
		case R.id.ll_licai_touzi:// 理财投资
			fragment = new SecretaryInvestmentShowFragment();
			break;
		case R.id.ll_handlecard:// 办卡推荐
			fragment = new SecretaryCardShowFragment();
			break;
		}

		entity.setFragment(fragment);
		bus.post(entity);
	}
	
	private void refreshTokenForExpired() {
		InternetConfig config = new InternetConfig();
		config.setKey(0);
		//CustomToast.show(activity, "更新访问权限", "重新获取令牌中...");
		FastHttpHander.ajaxGet(GlobalValue.URL_GET_ACCESS_TOKEN + App.app.getData("secret"),config, this);
	}
	@InjectHttp
	private void result(ResponseEntity r) {
		if (r.getStatus() == FastHttp.result_ok) {
			switch (r.getKey()) {
			case 0:
				Token token = Handler_Json.JsonToBean(Token.class,r.getContentAsString());
				CustomToast.show(activity, "权限更新成功", "已获取最新令牌");
				App.app.setData("access_token", token.getToken());
				
				/*
				 出现场景： 原本存在本地的token过期，当app启动时开启服务，去拿token，但此时由于与服务器访问的交互信息的过程较漫长，
				导致出现了拿着原本存在本地的token前去访问，此时页面正在或已经生成完毕，即使更新了本地的token后，由于仍未刷新首页的数据，
				故而首页出现刷不出数据的问题
				解决方案：  再次通知首页重新获取数据，局部刷新界面
				*/
				getActivity().sendBroadcast(new Intent("com.lansun.qmyo.refreshHome"));
				LogUtils.toDebugLog("令牌更新后", "发送广播给首页刷新的推荐列表内容");
				break;
			}
		}
	}
	
	
	 class SecretaryFragmentBroadCastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context ctx, Intent intent) {
			
//			if(justOneTimes){
				if(intent.getAction().equals("com.lansun.qmyo.checkMySecretary")){
					System.out.println("秘书页收到来自MainFragment的提示检测私人秘书信息的广播了");
					
					clickOpen = true;//由点击   进入此页面的  标签
					initView(rootView);
					setListener();
					justOneTimes = false;
				}
//			}
			if(intent.getAction().equals("com.lansun.qmyo.refreshMySecretary")){
				
				setSecretaryInformation();


			}
		}
	 }
	 
	 @Override
	public void onDestroy() {
		 activity.unregisterReceiver(broadCastReceiver);
		super.onDestroy();
	}
}
