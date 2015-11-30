package com.lansun.qmyo.fragment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.android.pc.ioc.event.EventBus;
import com.android.pc.ioc.image.RecyclingImageView;
import com.android.pc.ioc.inject.InjectAll;
import com.android.pc.ioc.inject.InjectBinder;
import com.android.pc.ioc.inject.InjectHttp;
import com.android.pc.ioc.inject.InjectInit;
import com.android.pc.ioc.internet.FastHttp;
import com.android.pc.ioc.internet.ResponseEntity;
import com.android.pc.ioc.view.PullToRefreshManager;
import com.android.pc.ioc.view.listener.OnClick;
import com.android.pc.util.Handler_Inject;
import com.android.pc.util.Handler_Json;
import com.android.pc.util.Handler_Time;
import com.google.gson.Gson;
import com.lansun.qmyo.R;
import com.lansun.qmyo.adapter.MessageAdapter;
import com.lansun.qmyo.app.App;
import com.lansun.qmyo.domain.MessageData;
import com.lansun.qmyo.domain.MessageList;
import com.lansun.qmyo.domain.MySecretary;
import com.lansun.qmyo.domain.information.InformationCount;
import com.lansun.qmyo.event.entity.FragmentEntity;
import com.lansun.qmyo.listener.RequestCallBack;
import com.lansun.qmyo.net.OkHttp;
import com.lansun.qmyo.override.CircleImageView;
import com.lansun.qmyo.utils.DialogUtil;
import com.lansun.qmyo.utils.LogUtils;
import com.lansun.qmyo.utils.DialogUtil.TipAlertDialogCallBack;
import com.lansun.qmyo.utils.GlobalValue;
import com.lansun.qmyo.view.CircularImage;
import com.lansun.qmyo.view.CustomToast;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
public class MineFragment extends BaseFragment implements RequestCallBack{
	@InjectAll
	Views v;

	class Views {
		private TextView tv_mine_nickname, tv_mine_icon,have_secretary,have_information;
		@InjectBinder(listeners = { OnClick.class }, method = "click")
		private View bottom_home, bottom_secretary, bottom_found,
		ll_mine_register_login, rl_mine_secretary, rl_mine_comments,
		rl_mine_history, ll_mine_xy_card, ll_mine_yhq, ll_mine_message,
		rl_mine_about, rl_mine_shared, sc_mine;
		private RecyclingImageView iv_mine_icon;
		private CircleImageView iv_mine_head;
	}
	private Handler handlerOk=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				if (count.getActivity()==1||count.getMaijie()==1) {
					v.have_information.setVisibility(View.VISIBLE);
					v.have_secretary.setVisibility(View.VISIBLE);
				}else {
					v.have_information.setVisibility(View.GONE);
					v.have_secretary.setVisibility(View.GONE);
				}
				break;
			}
		};
	};
	private InformationCount count;
	public  ArrayList<HashMap<String,String>> dataList = new ArrayList<HashMap<String,String>>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		//App.app.setData("firstEnterBankcardAndAddAnotherBankcard","");
		super.onCreate(savedInstanceState);
		
		initMySecretary();
		initInformation();
	}
	private void initInformation() {
//		InformationBiz biz=new InformationBiz();
//		biz.getInformation(this);
	}
	/**
	 * 此处请求网络目的是当我进入私人秘书的时候要有个私人秘书的名字。
	 */
	private void initMySecretary() {
		if (!isExperience()) {
			OkHttp.asyncGet(GlobalValue.URL_SECRETARY_SAVE, "Authorization","Bearer "+App.app.getData("access_token"), null, new Callback() {
				@Override
				public void onResponse(Response response) throws IOException {
					if (response.isSuccessful()) {
						Gson gson=new Gson();
						String json=response.body().string();
						GlobalValue.mySecretary=gson.fromJson(json,MySecretary.class);
					}
				}
				@Override
				public void onFailure(Request arg0, IOException arg1) {
				}
			});
		}
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.activity_mine,container,false);
		Handler_Inject.injectFragment(this, rootView);
		v.iv_mine_icon.setPressed(true);
		
		/**
		 * 保证从后台重新进入当前fragment时，头像部分可以重新加载上去
		 */
		if (GlobalValue.user != null) {
			String avatar = GlobalValue.user.getAvatar();
			if (!TextUtils.isEmpty(avatar)) {
				//加载头像上去
				loadPhoto(avatar, v.iv_mine_head);
			}
			
			if (!TextUtils.isEmpty(GlobalValue.user.getNickname())) {
				
				if(GlobalValue.user.getNickname() == null||GlobalValue.user.getNickname() =="null"||GlobalValue.user.getNickname().contains("null")){
					v.tv_mine_nickname.setText("请设置昵称");
					Log.i("Tag：nickName","NickName应该为设置昵称");
					
				}else{
					Log.i("Tag：nickName","NickName有值："+GlobalValue.user.getNickname());
					v.tv_mine_nickname.setText(GlobalValue.user.getNickname());
				}
			}else{
				v.tv_mine_nickname.setText("请注册或登陆");
			}
		}else{    //--- >GlobalValue.user == null
			
			LogUtils.toDebugLog("userinfo", "onCreatView()方法里： GlobalValue.user为空");
			//CustomToast.show(activity, "onCreatView()方法里： GlobalValue.user为空", "同上");
			
			if(!TextUtils.isEmpty(App.app.getData("user_avatar"))&&!TextUtils.isEmpty(App.app.getData("user_nickname"))){
				loadPhoto(App.app.getData("user_avatar"), v.iv_mine_head);
				v.tv_mine_nickname.setText(App.app.getData("user_nickname"));
			}else{
				
			}
		}
		
		
		//v.tv_mine_icon.setTextColor(getResources().getColor(R.color.app_green2));
		return rootView;
	}

	@InjectInit
	private void init() {
		
		//1.使用消息中心的网络接口前去访问 活动 和 迈界 消息，当返回的列表中的某一项的data.getIs_read()有值时 ，
		//即可将通知首页小圆点的显示
	
		String activityMessageUrl = GlobalValue.URL_USER_MESSAGE_LIST+ GlobalValue.MESSAGE.activity;
		String    majieMessageUrl = GlobalValue.URL_USER_MESSAGE_LIST+ GlobalValue.MESSAGE.maijie;
		
//		refreshCurrentList(activityMessageUrl, null, 0, null);
//		LogUtils.toDebugLog("infos", "去拿关于活动的信息");
//		refreshCurrentList(majieMessageUrl, null, 1, null);
//		LogUtils.toDebugLog("infos", "去拿关于迈界的信息");
		
		
		
		v.iv_mine_icon.setPressed(true);
		v.tv_mine_icon.setTextColor(getResources().getColor(R.color.app_green2));

		if(App.app.getData("isExperience").equals("true")){ //此处的isExperience是无用
			v.tv_mine_nickname.setText("请注册或登陆");
			
		}else{
			if (GlobalValue.user != null) {
				String avatar = GlobalValue.user.getAvatar();
				if (!TextUtils.isEmpty(avatar)) {
					//加载头像上去
					loadPhoto(avatar, v.iv_mine_head);
				}
				
				if (!TextUtils.isEmpty(GlobalValue.user.getNickname())) {
					
					if(GlobalValue.user.getNickname() == null||GlobalValue.user.getNickname() =="null"||GlobalValue.user.getNickname().contains("null")){
						v.tv_mine_nickname.setText("请设置昵称");
						Log.i("Tag：nickName","NickName应该为设置昵称");
						
					}else{
						Log.i("Tag：nickName","NickName有值："+GlobalValue.user.getNickname());
						v.tv_mine_nickname.setText(GlobalValue.user.getNickname());
					}
				}else{
					v.tv_mine_nickname.setText("请注册或登陆");
				}
			}else{   		 //--- >GlobalValue.user == null
				
				LogUtils.toDebugLog("userinfo", "init()方法里： GlobalValue.user为空");
				//CustomToast.show(activity, "init()方法里： GlobalValue.user为空", "同上");
				
				if(!TextUtils.isEmpty(App.app.getData("user_avatar"))&&!TextUtils.isEmpty(App.app.getData("user_nickname"))){
					loadPhoto(App.app.getData("user_avatar"), v.iv_mine_head);
					v.tv_mine_nickname.setText(App.app.getData("user_nickname"));
				}else{
					
				}
			}
		}
		
		//refreshCurrentList(GlobalValue.URL_USER_MESSAGE, null, 0, null);//去刷新消息
	}

	@Override
	public void onResume() {
		v.iv_mine_icon.setPressed(true);
		
		/*if (GlobalValue.user != null) {
			String avatar = GlobalValue.user.getAvatar();
			if (!TextUtils.isEmpty(avatar)) {
				//加载头像上去
				loadPhoto(avatar, v.iv_mine_head);
			}
			if (!TextUtils.isEmpty(GlobalValue.user.getNickname())) {
				v.tv_mine_nickname.setText(GlobalValue.user.getNickname());
				Log.i("Tag：nickName","在init之后起作用");

				if(GlobalValue.user.getNickname() == null||GlobalValue.user.getNickname() =="null"||GlobalValue.user.getNickname().contains("null")){
					v.tv_mine_nickname.setText("请设置昵称");
				}else{
					v.tv_mine_nickname.setText(GlobalValue.user.getNickname());
				}
			}else{//GlobalValue.user.getNickname()
				v.tv_mine_nickname.setText("请注册或登陆");
			}
		}*/
		super.onResume();
	}
	@Override
	public void onPause() {
		/*v.iv_mine_icon.setPressed(false);
		v.tv_mine_icon.setTextColor(getResources().getColor(R.color.text_nomal));*/
		super.onPause();
	}

	private void click(View view) {
		EventBus bus = EventBus.getDefault();
		FragmentEntity entity = new FragmentEntity();
		Fragment fragment = null;
		switch (view.getId()) {
		// case R.id.iv_mine_setting:// 设置界面
		// fragment = new SettingFragment();
		// break;
		case R.id.ll_mine_register_login:// 注册登录
			if (!TextUtils.isEmpty(App.app.getData("secret"))) {
				fragment = new PersonCenterFragment();
			} else {
				fragment = new RegisterFragment();
				Bundle bundle=new Bundle();
				bundle.putString("fragment_name",MineFragment.class.getSimpleName());
				fragment.setArguments(bundle);
			}
			break;
		case R.id.rl_mine_secretary:// 我的私人秘书
			if (GlobalValue.user == null || isExperience()) {
				DialogUtil.createTipAlertDialog(getActivity(),
						R.string.login_to_enjoysecretaryyourself, new TipAlertDialogCallBack() {
					@Override
					public void onPositiveButtonClick(
							DialogInterface dialog, int which) {
						dialog.dismiss();
						RegisterFragment fragment = new RegisterFragment();
						FragmentEntity event = new FragmentEntity();
						Bundle bundle = new Bundle();
						bundle.putString("fragment_name", MineFragment.class.getSimpleName());
						fragment.setArguments(bundle);
						event.setFragment(fragment);
						EventBus.getDefault().post(event);
					}
					@Override
					public void onNegativeButtonClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			}else
				fragment = new MineSecretaryFragment();
			break;
		case R.id.rl_mine_comments:// 我的评论
			if (GlobalValue.user == null || isExperience()) {
				fragment = new RegisterFragment();
				Bundle bundle = new Bundle();
				bundle.putString("fragment_name", MineFragment.class.getSimpleName());
				fragment.setArguments(bundle);
			} else {
				fragment = new MineCommentsFragment();
			}
			break;
		case R.id.rl_mine_history:// 最近浏览
			if (GlobalValue.user == null || isExperience()) {
				/*fragment = new RegisterFragment();*///体验用户也可以进入到我的历史浏览页
				fragment = new MineHistoryFragment();
			} else {
				fragment = new MineHistoryFragment();
			}
			break;
		case R.id.ll_mine_xy_card:// 信用卡
			/*	if (GlobalValue.user == null) {
				return;
			}
			if (isExperience()) {
				fragment = new RegisterFragment();
			} else
			 */
			fragment = new MineBankcardFragment();
			break;
		case R.id.ll_mine_yhq:// TODO 优惠券
			if (GlobalValue.user == null || isExperience()) {
				/*fragment = new RegisterFragment();*/
				fragment = new MineCouponsFragment();
			} else
				fragment = new MineCouponsFragment();
			break;
		case R.id.ll_mine_message:// TODO 消息中心
			if(App.app.getData("isExperience").equals("true")){
				DialogUtil.createTipAlertDialog(getActivity(),
						R.string.login_to_getmessage, new TipAlertDialogCallBack() {
					@Override
					public void onPositiveButtonClick(
							DialogInterface dialog, int which) {
						dialog.dismiss();
						RegisterFragment fragment = new RegisterFragment();
						FragmentEntity event = new FragmentEntity();
						Bundle bundle = new Bundle();
						bundle.putString("fragment_name", MineFragment.class.getSimpleName());
						fragment.setArguments(bundle);
						event.setFragment(fragment);
						EventBus.getDefault().post(event);
					}
					@Override
					public void onNegativeButtonClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
			}else{
				fragment = new MessageCenterFragment();
			}


			/*fragment = new MessageCenterFragment();*/

			break;
		case R.id.rl_mine_about:
			fragment = new AboutFragment();
			break;
		case R.id.rl_mine_shared://分享APP
			fragment = new SharedFragment();
			//activity.sendBroadcast(new Intent("com.lansun.qmyo.ChangeTheLGPStatus"));
			break;

		case R.id.bottom_home:
			fragment = new HomeFragment();
			break;
		case R.id.bottom_secretary:
			fragment = new SecretaryFragment();
			break;
		case R.id.bottom_found:
			fragment = new FoundFragment();
			break;
		}
		entity.setFragment(fragment);
		bus.post(entity);
	}

	
	@InjectHttp
	private void result(ResponseEntity r) {
		
		if (r.getStatus() == FastHttp.result_ok) {
			endProgress();
			
			switch (r.getKey()) {
			case 0:
				int is_readForActivity = Integer.MAX_VALUE;
				MessageList activityMessList = Handler_Json.JsonToBean(MessageList.class,
						r.getContentAsString());
				
				LogUtils.toDebugLog("infos", "拿到关于活动的信息");
				LogUtils.toDebugLog("infos", "拿到关于活动的信息"+ activityMessList.toString());
				
				HashMap<String, String> messMap = new HashMap<String, String>();
				if (activityMessList.getData() != null) {
					
					for (MessageData data : activityMessList.getData()) {
						messMap = new HashMap<String, String>();
						messMap.put("tv_message_item_count", data.getIs_read() + "");
						dataList.add(messMap);
					}
					for(HashMap data:dataList){
						LogUtils.toDebugLog("infos", "活动：  dataList中的每个信息的已读与未读标签为： "+data.get("tv_message_item_count")+"");
						LogUtils.toDebugLog("infos", "活动 ：  is_readForActivity ： "+is_readForActivity);
						if(is_readForActivity != 0){
							//is_read为0时 ，表明其未读，小绿点可见
							is_readForActivity= Integer.valueOf((String) data.get("tv_message_item_count"));
							LogUtils.toDebugLog("infos", "关于活动的消息--》已读");
						}else{
							activity.sendBroadcast(new Intent("com.lansun.qmyo.ChangeTheLGPStatus"));
							LogUtils.toDebugLog("infos", "活动： 发送广播");
							break;
						}
					}
				} 
				break;
			case 1:
				int is_readForMaijie = Integer.MAX_VALUE;
				MessageList maijieMessList = Handler_Json.JsonToBean(MessageList.class,
						r.getContentAsString());
				
				LogUtils.toDebugLog("infos", "拿到关于迈界的信息");
				LogUtils.toDebugLog("infos", "拿到关于迈界的信息"+ maijieMessList.toString());
				
				if (maijieMessList.getData() != null) {
					
					HashMap<String, String> maijieMap = new HashMap<String, String>();
					
					for (MessageData data : maijieMessList.getData()) {
						maijieMap.put("tv_message_item_count", data.getIs_read() + "");
						dataList.add(maijieMap);
					}
					for(HashMap<String, String> data:dataList){
						LogUtils.toDebugLog("infos", "迈界 ：  dataList中的每个信息的已读与未读标签为： "+data.get("tv_message_item_count")+"");
						LogUtils.toDebugLog("infos", "迈界 ：  is_readForMaijie ： "+is_readForMaijie);
						
						if(is_readForMaijie != 0){//一旦有一个消息未读，我们都需要将首页的小绿点展示出来
							//is_read为0时 ，表明其未读，小绿点可见
							is_readForMaijie= Integer.valueOf(data.get("tv_message_item_count"));
							LogUtils.toDebugLog("infos", "关于迈界的消息--》已读");
						}else{
							activity.sendBroadcast(new Intent("com.lansun.qmyo.ChangeTheLGPStatus"));//LGP:Little Green Point
							LogUtils.toDebugLog("infos", "迈界： 发送广播");
							break;
						}
					}
				} 
				break;
			}
		}
		PullToRefreshManager.getInstance().onHeaderRefreshComplete();
		PullToRefreshManager.getInstance().onFooterRefreshComplete();
	}

	
	@Override
	public void onResponse(Response response) throws IOException {
		if (response.isSuccessful()) {
			String json=response.body().string();
			Gson gson=new Gson();
			count=gson.fromJson(json, InformationCount.class);
			handlerOk.sendEmptyMessage(0);
		}
	}
	@Override
	public void onFailure(Request request, IOException exception) {

	}
}
